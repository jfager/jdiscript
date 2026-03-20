---
name: jdi-debug
description: >
  Write JVM debugger scripts using jdiscript to inspect a running JVM.
  Use when: debugging a Java process at runtime, tracing method calls,
  inspecting variables, monitoring threads/exceptions, profiling methods,
  or understanding framework behavior without modifying the target app.
---

# Writing JVM Debugger Scripts with jdiscript

For detailed API, see [api-reference.md](api-reference.md).
For recipe patterns, see [examples.md](examples.md).

## How it works

1. **Attach** to a target JVM (must have JDWP enabled)
2. **Register event handlers** (breakpoints, class loads, exceptions, etc.)
3. **Run the event loop** (blocks until VM exits)

Target JVM flag: `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:5005`
Use `suspend=y` to catch early init. Use `127.0.0.1` not `*` (IPv6 issues).

## Script skeleton

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

        j.run(List.of(
            (OnVMDeath) e -> { /* summary */ },
            (OnVMDisconnect) e -> { /* summary */ }));
    }
}
```

Module `build.gradle`:

```groovy
plugins { id 'java'; id 'application' }
dependencies { implementation project(':jdiscript') }
java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }
application {
    mainClass = 'com.example.MyDebugger'
    applicationDefaultJvmArgs = ['--add-modules', 'jdk.jdi']
}
```

## Core patterns

### Breakpoints

```java
// Simple (handles null locations for abstract/native automatically)
j.onMethodInvocation("com.example.Foo", "bar", e -> { ... });

// With JNI signature for overloaded methods
j.onMethodInvocation("com.example.Foo", "bar", "(Ljava/lang/String;)V", e -> { ... });

// Manual: defer until class loads, guard null locations
j.onClassPrep("com.example.Foo", cp -> {
    cp.referenceType().methodsByName("bar").forEach(m -> {
        if (m.location() != null) {
            j.breakpointRequest(m.location(), (OnBreakpoint) e -> { ... }).enable();
        }
    });
});
```

### Inspecting values

```java
String arg = RemoteObject.argToString(thread, 0);          // argument by index
ObjectReference thisObj = RemoteObject.thisObject(thread);  // 'this' reference
String repr = RemoteObject.remoteToString(obj, thread);     // invoke toString()
Optional<Value> v = RemoteObject.invokeRemote(obj,          // any no-arg method
    "getName", "()Ljava/lang/String;", thread);
```

### Call stack analysis

```java
Location caller = j.nearestCaller("com.myapp", thread);          // by package
Location caller = j.nearestCaller(loc ->                          // by predicate
    !loc.declaringType().name().startsWith("org.springframework."), thread);
String key = j.stacktraceKey(thread);                             // for histograms
```

## Critical rules

1. **Guard null locations.** `Method.location()` returns null for abstract/native
   methods. `onMethodInvocation()` handles this; raw `breakpointRequest` does not.
2. **Use `onClassPrep` for lazy setup.** Can't breakpoint unloaded classes.
3. **Remote invocations need a suspended thread.** Automatic inside handlers.
4. **Avoid hot-path breakpoints.** Read `StringReference` directly instead of
   remote `toString()` for high-frequency methods.
5. **Deduplicate high-volume events.** Track last event, collapse repeats.
6. **Compile with `--add-modules jdk.jdi`.**

## Choosing an approach

| Goal | Approach |
|------|----------|
| Break on method | `onMethodInvocation(class, method, handler)` |
| Fine-grained method filtering | `onClassPrep` + `methodsByName` + `breakpointRequest` |
| Watch field reads/writes | `onFieldAccess` / `onFieldModification` |
| Track exceptions | `onException(caught, uncaught, handler)` |
| Thread lifecycle | `onThreadStart` / `onThreadDeath` |
| Lock contention | `monitorContendedEnterRequest` |
| Method timing (instance methods) | `onMethodTimed(class, method, (entry, ms) -> ...)` |
| Method timing (manual / static) | breakpoint + `onCurrentMethodExitUnchecked` |
| Call path histogram | breakpoint + `stacktraceKey(thread)` |
| Count live instances | `instanceCount(className)` |
| Inspect live objects | `findInstances(className, maxCount)` |
| Read local variables | `RemoteObject.localVar(thread, name)` / `RemoteObject.locals(thread)` |
| Interactive / REPL / agent loop | `JDISession.start(vm)` |
| Inspect state without breakpoint | `session.withSuspend(() -> ...)` |
| Await one specific event | `session.awaitMethodInvocation(class, method)` |

## Reference

Full example: `log-config-debugger/src/main/java/org/jdiscript/example/LogConfigDebugger.java`
