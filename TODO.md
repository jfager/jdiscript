# Future Improvements

## Recently Added

- **`onMethodTimed(className, methodName, handler)`** — entry+exit pairing that
  delivers wall-clock duration to a single callback; eliminates the 10-line
  `onMethodInvocation` + `onCurrentMethodExitUnchecked` boilerplate.
- **`instanceCount(className)`** and **`findInstances(className, maxCount)`** —
  live heap inspection without a heap dump; enables connection-leak detection,
  object accumulation monitoring, and memory triage.
- **`RemoteObject.localVar(thread, name)`** and **`RemoteObject.locals(thread)`** —
  read named or all visible local variables from the current stack frame;
  unlocks use cases that previously required manual `StackFrame.getValues()`.

---

## Add CI/CD

No GitHub Actions workflow exists. A minimal CI pipeline should:
- Run `./gradlew build` on push and PR.
- Test against Java 17 and 21.

---

## Expand Test Coverage

Tests exist (24 tests across 3 files: `UtilsTest`, `DebugEventDispatcherTest`,
`JDIScriptIntegrationTest`) but coverage is thin.

Areas that could use additional coverage:
- `RemoteObject` — mock-based tests for `remoteToString`, `invokeRemote`,
  `localVar`, `locals`, and error paths.
- `VMLauncher` and `VMSocketAttacher` — startup and connection logic.
- Watchpoint events (`AccessWatchpointEvent`, `ModificationWatchpointEvent`).
- `MonitorContendedEnterEvent`, `MonitorWaitEvent`, and other less common events.
- Error paths in `JDIScript.run()` (e.g. `VMDisconnectedException` handling).
- The new `onMethodTimed`, `instanceCount`, `findInstances` methods.
- The return values from convenience methods (`onClassPrep`, `onMethodInvocation`,
  etc.) — verify the returned request can be disabled/deleted.

---

## Production Debugging Examples

The `log-config-debugger` module demonstrates a realistic production debugging
scenario. More examples in the same vein would be valuable. Each should include
a self-contained test app (like `log-config-debugger`'s `testapp` source set) and
a README with the scenario, setup, and expected output.

### High-priority (largest addressable audiences)

- **Slow Request Tracer** — Attach to embedded Tomcat/Jetty, use `onMethodTimed`
  on `HttpServlet.service()`, report requests exceeding a threshold (URL, duration,
  caller stack). The single most-requested production Java debugger use case.

- **Bean Initialization Profiler** — Attach during Spring Boot startup, time
  `AbstractBeanFactory.createBean()` calls, rank slowest beans. Answers "why is
  my Spring Boot startup taking 30 seconds?" without any code changes.

- **Connection Leak Detector** — Attach to HikariCP, track `getConnection()` /
  `recycle()` pairs via `instanceCount` + `findInstances`, flag connections held
  longer than a threshold with their acquisition stack trace.

### Medium-priority

- **Consumer Rebalance Tracer** — Attach to Kafka consumer, log partition
  assignments and rebalance timing.
- **Static Initializer Profiler** — Time `<clinit>` calls to find slow static
  init (a common startup surprise).
- **Virtual Thread Monitor** — Track virtual thread creation/pinning with
  `onThreadStart` filtered to `VirtualThread` subclasses.

---

## Ergonomics Gaps

### `onMethodTimed` for static methods

The current implementation uses `onCurrentMethodExitUnchecked`, which relies on
an instance filter for `thisObject()` and does not work for static methods or
constructors. A complementary implementation using `MethodExitRequest` + thread
filter would cover static methods at the cost of not handling recursion correctly:

```java
// MethodExitRequest + thread filter variant (static-safe, not recursion-safe)
methodExitRequest(exit -> {
    if (!exit.method().name().equals(methodName)) return;
    handler.accept(entryEvent, (System.nanoTime() - start) / 1_000_000);
    deleteEventRequest(exit.request());
}).addClassFilter(className).addThreadFilter(thread).enable();
```

A future `onMethodTimedStatic` (or a flag on `onMethodTimed`) could expose this.

### Return value + entry args in one callback

`MethodExitEvent.returnValue()` is available via `onMethodExit`, but there is no
convenience method that delivers both the entry arguments AND the return value in
a single handler.  A `onMethodCall(className, methodName, (entryArgs, returnValue,
durationMs) -> {})` style API would cover: "log every call to X with its input,
output, and duration."

### Thread and instance filtering on convenience methods

`onMethodInvocation` and friends set up class-level filtering but don't expose
per-thread or per-instance filters. Power users currently reach for the low-level
`ChainingBreakpointRequest` directly. Adding optional `.forThread(thread)` /
`.forInstance(obj)` chaining to the returned request — or overloads that accept
these filters — would avoid the boilerplate.

---

## Agentic Tooling

jdiscript already ships a Claude Code skill (`.claude/skills/jdi-debug/`), which
positions it as an *AI-operable* debugging tool. To sharpen that edge:

### Structured / machine-readable output

`println` is fine for humans but AI agents benefit from structured data they can
parse and reason about. A lightweight `DebugReport` builder that accumulates
events into JSON-serializable records, then prints a summary at VM death, would
let AI agents analyze patterns rather than parse log lines:

```java
DebugReport report = new DebugReport();
j.onMethodTimed("com.example.Foo", "bar", (e, ms) ->
    report.record("bar", ms, j.nearestCaller("com.example", e.thread())));
j.run(List.of((OnVMDeath) e -> report.printJson(System.out)));
```

### More skill examples

The more diverse the examples in `.claude/skills/jdi-debug/examples.md`, the
better AI-generated scripts become. Priority additions:
- Spring Boot bean init profiling
- HTTP slow-call detection
- Connection leak detection
- Structured JSON output
