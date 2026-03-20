# jdiscript Example Patterns

Practical patterns extracted from real debugger scripts in this repository.
Each section is a self-contained recipe.

## 1. Trace method calls with arguments

The simplest pattern: break on a method and print its arguments.

```java
VirtualMachine vm = new VMSocketAttacher(5005).attach();
JDIScript j = new JDIScript(vm);

j.onMethodInvocation("com.example.UserService", "createUser", e -> {
    String name = RemoteObject.argToString(e.thread(), 0);
    String email = RemoteObject.argToString(e.thread(), 1);
    System.out.println("createUser(" + name + ", " + email + ")");
    j.printTrace(e);  // print full stack trace
});

j.run();
```

## 2. Monitor lock contention

Identify which threads are fighting over which monitors.

```java
j.monitorContendedEnterRequest(e -> {
    j.printTrace(e, "Waiting for " + e.monitor());
}).enable();

j.monitorContendedEnteredRequest(e -> {
    j.printTrace(e, "Acquired " + e.monitor());
}).enable();

j.run();
```

See `example/src/main/java/org/jdiscript/example/ContentionPrinter.java`.

## 3. Track exceptions with histogramming

Count exception types by throw location.

```java
Map<String, Integer> exceptionCounts = new LinkedHashMap<>();

j.onException(true, true, e -> {
    String type = e.exception().referenceType().name();
    String location = e.location().toString();
    String key = type + " at " + location;
    exceptionCounts.merge(key, 1, Integer::sum);
});

// Print summary on VM death
j.run(List.of(
    (OnVMDeath) e -> exceptionCounts.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .forEach(entry -> System.out.println(entry.getValue() + "x " + entry.getKey()))
));
```

See `example/src/main/java/org/jdiscript/example/ExceptionTracker.java`.

## 4. Profile method timing

Measure wall-clock time for methods in a class.

```java
Map<String, long[]> timings = new ConcurrentHashMap<>(); // [totalNs, count]

j.onClassPrep("com.example.SlowService", cp -> {
    cp.referenceType().methods().forEach(m -> {
        if (m.location() != null && !m.isAbstract() && !m.isNative()) {
            j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                long start = System.nanoTime();
                String name = j.fullName(m);
                j.onCurrentMethodExitUnchecked(e.thread(), exit -> {
                    long elapsed = System.nanoTime() - start;
                    timings.computeIfAbsent(name, k -> new long[2]);
                    long[] stats = timings.get(name);
                    stats[0] += elapsed;
                    stats[1]++;
                });
            }).enable();
        }
    });
});
```

See `example/src/main/java/org/jdiscript/example/MethodTimer.java`.

## 5. Stacktrace histogram

Find the most common call paths to a method.

```java
Map<String, Integer> callPaths = new LinkedHashMap<>();

j.onMethodInvocation("java.lang.String", "<init>", e -> {
    String key = j.stacktraceKey(e.thread());
    callPaths.merge(key, 1, Integer::sum);
});
```

See `example/src/main/java/org/jdiscript/example/StacktraceHistogram.java`.

## 6. Thread lifecycle monitoring

Track thread creation and death with timing.

```java
Map<Long, long[]> threads = new ConcurrentHashMap<>(); // [startTime]

j.onThreadStart(e -> {
    long id = e.thread().uniqueID();
    threads.put(id, new long[]{System.currentTimeMillis()});
    System.out.println("+ Thread started: " + e.thread().name());
});

j.onThreadDeath(e -> {
    long id = e.thread().uniqueID();
    long[] start = threads.get(id);
    long lifetime = start != null ? System.currentTimeMillis() - start[0] : -1;
    System.out.println("- Thread died: " + e.thread().name()
        + " (lived " + lifetime + "ms)");
});
```

See `example/src/main/java/org/jdiscript/example/ThreadMonitor.java`.

## 7. Lazy breakpoint setup with onClassPrep

Classes may not be loaded yet when your script starts. Use `onClassPrep` to
defer breakpoint registration until the class is available:

```java
j.onClassPrep("com.example.LazilyLoaded", cp -> {
    ReferenceType type = cp.referenceType();

    // Set breakpoints on specific methods
    type.methodsByName("initialize").forEach(m -> {
        if (m.location() != null) {
            j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                System.out.println("initialize() called");
            }).enable();
        }
    });

    // Watch field modifications
    type.fieldsByName("config").forEach(f -> {
        j.modificationWatchpointRequest(f, e -> {
            System.out.println("config changed to: "
                + RemoteObject.valueToString(e.valueToBe(), e.thread()));
        }).enable();
    });
});
```

## 8. Reading 'this' and invoking methods on remote objects

```java
j.onMethodInvocation("com.example.Logger", "setLevel", e -> {
    // Get 'this' reference
    ObjectReference thisObj = RemoteObject.thisObject(e.thread());

    // Call getName() on it
    String loggerName = RemoteObject.invokeRemote(thisObj, "getName",
            "()Ljava/lang/String;", e.thread())
        .filter(v -> v instanceof StringReference)
        .map(v -> ((StringReference) v).value())
        .orElse("<unknown>");

    // Read the argument
    String level = RemoteObject.argToString(e.thread(), 0);

    System.out.println(loggerName + " -> " + level);
});
```

## 9. Filtering call stacks to find application code

Skip framework internals to find who actually triggered an event:

```java
// Simple: match by package prefix
Location caller = j.nearestCaller("com.myapp", e.thread());

// Advanced: exclude known frameworks
Location caller = j.nearestCaller(loc -> {
    String name = loc.declaringType().name();
    return !name.startsWith("org.springframework.")
        && !name.startsWith("java.")
        && !name.startsWith("sun.");
}, e.thread());

if (caller != null) {
    System.out.println("  at " + caller);
}
```

## 10. Event deduplication

Collapse repeated identical events to keep output manageable:

```java
private String lastEventKey = null;
private int repeatCount = 0;

private void recordEvent(String key, String message) {
    if (key.equals(lastEventKey)) {
        repeatCount++;
        return;
    }
    if (repeatCount > 0) {
        System.out.println("  ... repeated " + repeatCount + " more time(s)");
    }
    lastEventKey = key;
    repeatCount = 0;
    System.out.println(message);
}
```

## 11. Hot-path optimization: avoid remote toString()

For methods called very frequently, avoid `RemoteObject.argToString()` which
invokes `toString()` on the remote VM. Instead read string values directly:

```java
j.breakpointRequest(location, (OnBreakpoint) e -> {
    // Fast path: read raw StringReference without remote invoke
    try {
        Value v = e.thread().frame(0).getArgumentValues().get(0);
        if (v instanceof StringReference sr) {
            String value = sr.value();
            // use value...
        }
    } catch (IncompatibleThreadStateException ex) {
        // thread not suspended
    }
}).enable();
```

## 12. Interactive / agentic session (JDISession)

Use `JDISession` when the script doesn't know all its questions upfront —
REPL sessions, agent query loops, or hybrid scripts.

```java
// Start the event loop without blocking
JDISession session = JDISession.start(new VMSocketAttacher(5005).attach());

// --- Pattern A: inspect state right now, no breakpoint needed ---
long sessions = session.withSuspend(
    () -> session.j.instanceCount("com.example.UserSession"));
System.out.println("live sessions: " + sessions);

// --- Pattern B: sequential awaiting — ask one question at a time ---
// (Works for already-loaded classes, which is typical when attaching
//  to a running server)
BreakpointEvent e = session
    .awaitMethodInvocation("com.example.UserService", "findUser")
    .orTimeout(30, TimeUnit.SECONDS)
    .join();
System.out.println("findUser arg0=" + RemoteObject.argToString(e.thread(), 0));
System.out.println("locals: " + RemoteObject.locals(e.thread()));

// --- Pattern C: agent loop ---
while (!done) {
    String target = agent.nextMethodToWatch();  // AI decides
    BreakpointEvent hit = session
        .awaitMethodInvocation("com.example.Foo", target)
        .orTimeout(10, TimeUnit.SECONDS)
        .join();
    agent.process(hit);  // AI analyzes and decides next step
}

// Disconnect without killing the target
session.close();
```

See `example/src/main/java/org/jdiscript/example/InteractiveExample.java`.

## 13. JShell / REPL integration

`JDISession` is designed to be used from JShell without any boilerplate.
Start a JShell with jdiscript on the classpath, then:

```
jshell --add-modules jdk.jdi --class-path path/to/jdiscript.jar

jshell> import org.jdiscript.util.*; import com.sun.jdi.*;
jshell> var s = JDISession.start(new VMSocketAttacher(5005).attach())
jshell> s.withSuspend(() -> s.j.instanceCount("com.example.Foo"))
==> 42
jshell> s.j.onException(true, true, e -> System.out.println(e.exception()))
jshell> s.close()
```

No script file required. `session.j` is a public field specifically for this
ergonomic access pattern.

## 15. Time method calls (slow-call detection)

Detect calls that exceed a latency threshold without modifying the target:

```java
j.onMethodTimed("com.example.UserService", "findUser",
    (entry, durationMs) -> {
        if (durationMs > 200) {
            System.out.printf("SLOW findUser: %dms  caller=%s%n",
                durationMs,
                j.nearestCaller("com.example", entry.thread()));
        }
    });
```

Works for instance methods. Duration is in wall-clock milliseconds.
See `example/src/main/java/org/jdiscript/example/HeapInspector.java`.

## 16. Count and inspect live instances

Detect accumulation and inspect state of live objects without a heap dump:

```java
// Snapshot instance count at a known point (e.g. after each request)
j.onMethodInvocation("com.example.RequestHandler", "handleRequest", e -> {
    long sessionCount  = j.instanceCount("com.example.UserSession");
    long connectionCount = j.instanceCount("com.example.DBConnection");
    System.out.println("sessions=" + sessionCount + " connections=" + connectionCount);
});

// Inspect what the live Connection objects actually look like
j.onMethodInvocation("com.example.RequestHandler", "handleRequest", e -> {
    j.findInstances("com.example.DBConnection", 20).forEach(ref -> {
        String state = RemoteObject.invokeRemote(ref, "getState",
            "()Ljava/lang/String;", e.thread())
            .map(v -> RemoteObject.valueToString(v, e.thread()))
            .orElse("?");
        long age = /* read createdAt field... */ 0;
        System.out.println("  conn@" + ref.uniqueID() + " state=" + state);
    });
});
```

See `example/src/main/java/org/jdiscript/example/HeapInspector.java`.

## 17. Inspect local variables

Read arbitrary locals from the current frame (requires `-g` debug info, the
default for most build tools):

```java
j.onMethodInvocation("com.example.OrderService", "processOrder", e -> {
    // Read a specific local by name
    String orderId = RemoteObject.localVar(e.thread(), "orderId")
        .map(v -> RemoteObject.valueToString(v, e.thread()))
        .orElse("<not yet assigned>");
    System.out.println("processOrder orderId=" + orderId);

    // Or dump all locals at once
    Map<String, String> locals = RemoteObject.locals(e.thread());
    locals.forEach((name, value) -> System.out.println("  " + name + " = " + value));
});
```

## Full reference example

See `log-config-debugger/src/main/java/org/jdiscript/example/LogConfigDebugger.java`
for a production-quality script that combines most of these patterns: lazy
breakpoint setup, remote object inspection, call stack filtering, event
deduplication, and structured summary output.
