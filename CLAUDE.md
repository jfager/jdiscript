# jdiscript — JVM Debugger Scripting Library

jdiscript is a fluent Java wrapper around the Java Debug Interface (JDI). It lets
you attach to any running JVM and programmatically inspect or control it: set
breakpoints, read variables, trace method calls, watch field changes, monitor
threads — all without modifying the target application.

## When to Use jdiscript

Use jdiscript when you need to understand what a JVM application is **actually
doing at runtime**, especially when:

- **Diagnosing bugs** that only reproduce in a running environment
- **Tracing framework behavior** (how does Spring/Log4j/Hibernate initialize?)
- **Inspecting state** of a live process without adding logging or redeploying
- **Monitoring** thread contention, exceptions, method timing, or resource usage
- **Auditing** what code paths execute in production

jdiscript is particularly valuable when you can't or don't want to modify the
target application's code — you attach externally and observe.

## Building

```bash
./gradlew build                    # build everything
./gradlew :jdiscript:build         # build just the library
./gradlew :example:compileJava     # compile basic examples
./gradlew :log-config-debugger:compileJava  # compile the logging tracer
```

Requires Java 17+ and Gradle 8.x (wrapper included).

## Target JVM Setup

The target JVM must enable JDWP. Add this JVM argument:

```
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:5005
```

- `suspend=y` pauses the JVM until a debugger attaches (use to catch early init)
- `suspend=n` lets the JVM run immediately (use for already-running processes)
- Use `127.0.0.1` instead of `*` to avoid IPv6 issues in containers

## Core Pattern

Every jdiscript program follows the same structure:

```java
// 1. Attach to the target JVM
VirtualMachine vm = new VMSocketAttacher(5005).attach();
JDIScript j = new JDIScript(vm);

// 2. Set up event handlers (breakpoints, watches, etc.)
j.onMethodInvocation("com.example.MyClass", "myMethod", e -> {
    // This runs when the method is entered
    String arg = RemoteObject.argToString(e.thread(), 0);
    System.out.println("myMethod called with: " + arg);
});

// 3. Run the event loop (blocks until VM exits)
j.run();
```

## Key API Methods

### Connecting to a JVM

```java
// Attach via socket (most common)
VirtualMachine vm = new VMSocketAttacher(port).attach();
VirtualMachine vm = new VMSocketAttacher(host, port).attach();

// Attach via process ID
VirtualMachine vm = new VMProcessAttacher(pid).attach();

// Launch a new debuggee process
VirtualMachine vm = new VMLauncher(options, mainClass).start();

// Listen for incoming connections
VMListener listener = new VMListener(port);
listener.start();
VirtualMachine vm = listener.next();
```

### Setting Breakpoints

```java
// Break on method entry (handles abstract/native methods safely)
j.onMethodInvocation("com.example.Foo", "bar", e -> { ... });

// With specific signature (for overloaded methods)
j.onMethodInvocation("com.example.Foo", "bar", "(Ljava/lang/String;)V", e -> { ... });

// Break when a class is first loaded
j.onClassPrep("com.example.Foo", e -> {
    // Set up more specific breakpoints here
    ReferenceType type = e.referenceType();
    type.methodsByName("bar").forEach(m -> {
        if (m.location() != null) {  // null for abstract/native methods
            j.breakpointRequest(m.location(), (OnBreakpoint) be -> { ... }).enable();
        }
    });
});

// Break on field read or write
j.onFieldAccess("com.example.Foo", "myField", e -> { ... });
j.onFieldModification("com.example.Foo", "myField", e -> { ... });
```

### Inspecting Runtime Values

Use `RemoteObject` utilities to safely read values from the debugged JVM:

```java
import org.jdiscript.util.RemoteObject;

// Read a method argument as a human-readable string
String firstArg = RemoteObject.argToString(thread, 0);
String secondArg = RemoteObject.argToString(thread, 1);

// Get 'this' reference
ObjectReference thisObj = RemoteObject.thisObject(thread);

// Invoke toString() on a remote object
String repr = RemoteObject.remoteToString(obj, thread);

// Invoke any no-arg method on a remote object
Optional<Value> name = RemoteObject.invokeRemote(obj, "getName",
    "()Ljava/lang/String;", thread);

// Convert any JDI Value to a readable string
String readable = RemoteObject.valueToString(someValue, thread);
```

### Analyzing Call Stacks

```java
// Find the nearest caller matching a package prefix
Location caller = j.nearestCaller("com.myapp", thread);

// Find the nearest caller matching a predicate (e.g., skip framework internals)
Location caller = j.nearestCaller(
    loc -> !loc.declaringType().name().startsWith("org.springframework."),
    thread);

// Generate a stacktrace key for histogramming
String key = j.stacktraceKey(thread);
```

### Monitoring Threads and Exceptions

```java
// Track thread creation/death
j.onThreadStart(e -> println("Thread started: " + e.thread().name()));
j.onThreadDeath(e -> println("Thread died: " + e.thread().name()));

// Monitor lock contention
j.monitorContendedEnterRequest(e -> {
    j.printTrace(e, "Contention for " + e.monitor());
}).enable();

// Track exceptions (excludes java.*/sun.*/jdk.* by default)
j.onException(true, true, e -> {
    println("Exception: " + e.exception().referenceType().name());
    j.printTrace(e);
});
```

### Running the Event Loop

```java
// Run until VM exits
j.run();

// Run with timeout (milliseconds)
j.run(30_000);

// Run with VM death/disconnect handlers
OnVMDeath death = e -> printSummary();
OnVMDisconnect disconnect = e -> printSummary();
j.run(List.of(death, disconnect));
```

## Common Pitfalls

1. **Null locations**: Abstract and native methods return `null` from
   `Method.location()`. Always check before passing to `breakpointRequest()`.
   The `onMethodInvocation()` convenience methods handle this automatically.

2. **Thread suspension**: Remote method invocations (like `RemoteObject.remoteToString()`)
   require the thread to be suspended. Inside breakpoint/step handlers the thread
   is already suspended. Use `ObjectReference.INVOKE_SINGLE_THREADED` to avoid
   resuming other threads during invocation.

3. **Hot-path overhead**: Every breakpoint hit pauses the target thread. For
   methods called millions of times (constructors, getters), consider using
   `firstArgAsString()` patterns that read raw `StringReference` values instead
   of invoking `toString()` remotely.

4. **Event deduplication**: High-frequency events (like `setLevel()` called for
   every logger during config) can produce thousands of nearly-identical entries.
   Consider collapsing repeated events in your output.

5. **IPv6 in containers**: Using `address=*:5005` can fail with "Address family
   not supported" in some container runtimes. Use `address=127.0.0.1:5005`.

## Project Structure

```
jdiscript/          # The library itself (org.jdiscript)
  src/main/java/org/jdiscript/
    JDIScript.java              # Main entry point — fluent API
    handlers/                   # @FunctionalInterface event handlers
      OnBreakpoint, OnStep, OnException, OnClassPrepare,
      OnMethodEntry, OnMethodExit, OnFieldAccess, ...
    requests/                   # Chainable request builders
      ChainingBreakpointRequest, ChainingStepRequest, ...
    util/
      VMSocketAttacher.java     # Attach to JVM via socket
      VMProcessAttacher.java    # Attach to JVM via PID
      VMLauncher.java           # Launch a new debuggee
      VMListener.java           # Listen for debug connections
      RemoteObject.java         # Safe remote object inspection
      Utils.java                # println(), unchecked()
    events/
      DebugEventDispatcher.java # Routes events to handlers

example/            # Basic examples (contention, exceptions, threads, etc.)
log-config-debugger/  # Full example: trace logging framework initialization
```

## Writing a New Debugger Script

A typical debugger script module needs this in `build.gradle`:

```groovy
plugins {
    id 'java'
    id 'application'
}

dependencies {
    implementation project(':jdiscript')
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = 'com.example.MyDebugger'
    applicationDefaultJvmArgs = ['--add-modules', 'jdk.jdi']
}
```

## Reference Example

The `log-config-debugger` module is a complete, production-quality example that
demonstrates all the key patterns:

- Lazy breakpoint setup via `onClassPrep` (only activates for loaded classes)
- Safe remote object inspection via `RemoteObject`
- Stack walking via `nearestCaller(Predicate, ...)` to skip framework internals
- Event deduplication to collapse repeated identical events
- Timeline recording with structured summary output
- Handling abstract/native methods (null location guards)

See `log-config-debugger/src/main/java/org/jdiscript/example/LogConfigDebugger.java`.
