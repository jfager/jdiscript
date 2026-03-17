---
name: jdi-debug
description: >
  Write and run JVM debugger scripts using jdiscript to inspect a running JVM.
  Use when the user needs to debug a JVM application at runtime, trace method
  calls, inspect variables, monitor threads or exceptions, profile method
  timing, or understand framework behavior — all without modifying the target
  application. Activate when: a user asks to debug a running Java process,
  trace what a JVM app is doing, understand how a framework initializes, find
  the source of a runtime issue, or write a JDI script.
---

# Writing JVM Debugger Scripts with jdiscript

You are writing a debugger script that attaches to a running JVM and
inspects it using the Java Debug Interface (JDI) via the jdiscript library.

## How jdiscript works

jdiscript wraps the JDI to provide a fluent, lambda-friendly API. Every
script follows three steps:

1. **Attach** to the target JVM (it must have JDWP enabled)
2. **Register event handlers** (breakpoints, class loads, exceptions, etc.)
3. **Run the event loop** (blocks until the target exits)

The target JVM must be started with:

```
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:5005
```

Use `suspend=y` if you need to catch early initialization events.

## Skeleton for a new script

```java
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.*;
import org.jdiscript.util.*;
import com.sun.jdi.*;

public class MyDebugger {
    public static void main(String[] args) {
        VirtualMachine vm = new VMSocketAttacher(5005).attach();
        JDIScript j = new JDIScript(vm);

        // Register handlers here...

        OnVMDeath death = e -> { /* print summary */ };
        OnVMDisconnect disconnect = e -> { /* print summary */ };
        j.run(List.of(death, disconnect));
    }
}
```

The `build.gradle` for a script module:

```groovy
plugins {
    id 'java'
    id 'application'
}
dependencies {
    implementation project(':jdiscript')
}
java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}
application {
    mainClass = 'com.example.MyDebugger'
    applicationDefaultJvmArgs = ['--add-modules', 'jdk.jdi']
}
```

## Core API patterns

For detailed API reference, see [api-reference.md](api-reference.md).

### Setting breakpoints on methods

Use `onMethodInvocation` for the simplest case — it handles class loading
and null locations (abstract/native methods) automatically:

```java
j.onMethodInvocation("com.example.Foo", "bar", e -> {
    String arg = RemoteObject.argToString(e.thread(), 0);
    System.out.println("bar(" + arg + ")");
});
```

For more control, use `onClassPrep` to defer breakpoint setup until the
class is loaded, then set breakpoints on specific methods:

```java
j.onClassPrep("com.example.Foo", cp -> {
    cp.referenceType().methodsByName("bar").forEach(m -> {
        if (m.location() != null) {  // ALWAYS check — null for abstract/native
            j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                // handler runs when method is entered
            }).enable();
        }
    });
});
```

### Inspecting remote values

Use `RemoteObject` to safely read values from the debugged JVM:

```java
// Read method arguments (invokes toString() on objects)
String arg = RemoteObject.argToString(thread, 0);

// Get 'this' reference
ObjectReference thisObj = RemoteObject.thisObject(thread);

// Invoke toString() on any remote object
String repr = RemoteObject.remoteToString(obj, thread);

// Invoke any no-arg method
Optional<Value> name = RemoteObject.invokeRemote(obj, "getName",
    "()Ljava/lang/String;", thread);
```

### Finding application code in the call stack

```java
// Skip framework internals, find application code
Location caller = j.nearestCaller("com.myapp", thread);

// Or use a predicate for exclusion-based filtering
Location caller = j.nearestCaller(
    loc -> !loc.declaringType().name().startsWith("org.springframework."),
    thread);
```

## Critical rules

1. **Always guard null locations.** `Method.location()` returns `null` for
   abstract and native methods. Passing `null` to `breakpointRequest()` causes
   an NPE. `onMethodInvocation()` handles this for you; raw `breakpointRequest`
   does not.

2. **Use `onClassPrep` for lazy breakpoint setup.** You cannot set breakpoints
   on classes that haven't been loaded yet. `onClassPrep` fires when a class
   is first loaded, giving you the `ReferenceType` to set breakpoints on.

3. **Remote invocations need a suspended thread.** Inside breakpoint handlers,
   the thread is already suspended. Outside handlers, you must suspend it first.

4. **Avoid high-frequency breakpoints.** Each breakpoint hit pauses the target
   thread. For methods called millions of times, read `StringReference` values
   directly instead of invoking `toString()` remotely:
   ```java
   Value v = thread.frame(0).getArgumentValues().get(0);
   if (v instanceof StringReference sr) return sr.value();
   ```

5. **Deduplicate high-volume events.** Framework init can fire thousands of
   identical events. Track the last event and collapse repeats.

6. **Use `127.0.0.1` not `*`** for the JDWP address to avoid IPv6 issues in
   containers.

7. **Compile with `--add-modules jdk.jdi`.** The JDI API is in the `jdk.jdi`
   module, not available by default.

## Choosing between approaches

| Goal | Approach |
|------|----------|
| Break on a specific method | `onMethodInvocation(class, method, handler)` |
| Break on overloaded method | `onMethodInvocation(class, method, sig, handler)` |
| Fine-grained method filtering | `onClassPrep` + `methodsByName` + `breakpointRequest` |
| Watch field reads/writes | `onFieldAccess` / `onFieldModification` |
| Track all exceptions | `onException(caught, uncaught, handler)` |
| Monitor thread lifecycle | `onThreadStart` / `onThreadDeath` |
| Monitor lock contention | `monitorContendedEnterRequest` |
| Profile method timing | `onClassPrep` + breakpoint on entry + `onCurrentMethodExitUnchecked` |
| Histogram call stacks | breakpoint + `j.stacktraceKey(thread)` |

## Reference example

See `log-config-debugger/` for a production-quality example and
[examples.md](examples.md) for smaller, focused patterns.
