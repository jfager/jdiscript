# Future Improvements

## Resolve TODOs in JDIScript.java

There are 7 open TODO comments in `JDIScript.java`:

- **Line 192** — `InterruptedException` in `run()` is silently swallowed. Should
  either re-interrupt the thread (`Thread.currentThread().interrupt()`) or propagate.

- **Lines 672, 689, 711, 737, 773, 796** — Several convenience methods
  (`onClassPrep`, `onFieldAccess`, `onFieldModification`, `onMethodInvocation`)
  return `void` with a TODO asking what they should return. Returning the
  underlying request object would enable chaining (e.g. adding filters after
  setup). Returning a `CompletableFuture<BreakpointRequest>` was also considered
  for the deferred-breakpoint methods (`onMethodInvocation`), since those set up
  a `ClassPrepareRequest` that later creates a `BreakpointRequest`.

## Integrate the Code Generator into the Build

The `generator` subproject produces the `ChainingXxxRequest` wrapper classes in
`jdiscript/src/main/java/org/jdiscript/requests/`, but it's run manually. The
generated classes are checked into version control and can drift out of sync
with the JDI API.

Improvements:
- Add a Gradle task that runs `ChainingRequestGenerator.main()` and outputs to
  a generated-sources directory.
- Wire that task as a dependency of `:jdiscript:compileJava`.
- Fix the generator's parameter name resolution (currently produces `arg0`,
  `arg1` placeholders in javadoc).

## Add CI/CD

No GitHub Actions workflow exists. A minimal CI pipeline should:
- Run `./gradlew build` on push and PR.
- Test against Java 17 and 21.

## Expand Test Coverage

Initial tests are in place (24 tests across 3 files):
- `UtilsTest` — unit tests for the `unchecked()` wrapper.
- `DebugEventDispatcherTest` — mock-based tests for event routing, handler
  management, VM events, and edge cases.
- `JDIScriptIntegrationTest` — launches a real target VM and verifies
  breakpoints, class prepare, thread start, and `fullName()` formatting.

Areas that could use additional coverage:
- `VMLauncher` and `VMSocketAttacher` — startup and connection logic.
- Watchpoint events (`AccessWatchpointEvent`, `ModificationWatchpointEvent`).
- `MonitorContendedEnterEvent`, `MonitorWaitEvent`, and other less common events.
- Error paths in `JDIScript.run()` (e.g. `VMDisconnectedException` handling).

## Production Debugging Examples

The existing examples demonstrate jdiscript's API against toy target programs.
A new set of examples should show realistic production troubleshooting against
real-world server and data-processing frameworks. Each example should attach to
(or launch) an actual application, demonstrate a specific debugging technique,
and include a README explaining the scenario, setup, and expected output.

### Approach

Examples should use `VMSocketAttacher` to attach to a running JVM via
`-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005`. Each
example project should include:
- A small reproducer app or config that triggers the behavior being debugged.
- A jdiscript script that attaches and performs the investigation.
- A README with the scenario, how to run it, and what the output means.

Where possible, examples should use Docker or Gradle tasks to start the target
application so they are self-contained and reproducible.

### Servlet Containers (Tomcat / Jetty)

**1. Slow Request Tracer** — Attach to a Jetty or embedded Tomcat instance.
Set a breakpoint on `javax.servlet.http.HttpServlet.service()` (or the
Jakarta equivalent), measure wall-clock time to method exit, and report
requests exceeding a threshold. Demonstrates `onMethodInvocation()` +
`onCurrentMethodExit()` for latency profiling of individual HTTP requests.

**2. Thread Pool Exhaustion Detector** — Monitor thread creation and death
inside Tomcat's/Jetty's executor pool. Track active request-processing threads
over time, flag when the pool is saturated (all threads busy), and dump stack
traces of long-running request threads. Demonstrates `onThreadStart()`,
`onThreadDeath()`, and conditional `printTrace()`.

**3. Session Leak Finder** — Watch for `HttpSession.setAttribute()` calls and
track session sizes by monitoring field modifications on the session's
attribute map. Identify sessions accumulating large or unexpected objects.
Demonstrates `onMethodInvocation()` with argument inspection via
`StackFrame.getArgumentValues()`.

### Apache Spark

**4. Task Serialization Inspector** — Attach to a Spark driver. Set breakpoints
on `org.apache.spark.serializer.JavaSerializer.serialize()` and log the class
and size of objects being serialized for task shipping. Helps catch accidentally
large closures (e.g., serializing an entire SparkContext). Demonstrates
breakpoints + argument inspection + `nearestCaller()` to find the user code
responsible.

**5. Shuffle Spill Monitor** — Watch for disk spill events by breaking on
`ExternalSorter.spill()` or `UnsafeExternalSorter.spill()`. Log memory
pressure, partition counts, and stack traces when spills occur. Useful for
tuning `spark.shuffle.spill.numElementsForceSpillThreshold` and partition
counts. Demonstrates `onClassPrep()` to handle late-loaded classes +
`stacktraceKey()` histograms.

### Apache Flink

**6. Checkpoint Stall Debugger** — Attach to a Flink TaskManager. Monitor
`org.apache.flink.runtime.checkpoint.CheckpointBarrierHandler` to track
barrier alignment timing. Flag when a subtask takes too long to process a
barrier (a common cause of checkpoint timeouts). Demonstrates
`onMethodInvocation()` with timing + thread-scoped state tracking.

**7. Backpressure Source Locator** — Break on credit-based flow control methods
in Flink's network stack (`CreditBasedPartitionWriter`,
`InputChannelRecoveredStateHandler`). Track which operators are consuming
credits slowly and correlate with operator stack traces. Demonstrates
`monitorContendedEnterRequest()` + `stacktraceKey()`.

### Apache Kafka (Broker or Client)

**8. Consumer Rebalance Tracer** — Attach to a Kafka consumer application.
Break on `ConsumerCoordinator.onJoinComplete()` and
`ConsumerRebalanceListener.onPartitionsRevoked()`. Log partition assignments,
rebalance duration, and the stack trace triggering each rebalance. Useful for
diagnosing frequent rebalances caused by slow processing or session timeouts.
Demonstrates `onClassPrep()` + `onMethodInvocation()` + argument value
extraction.

### Spring Boot / Spring Framework

**9. Bean Initialization Profiler** — Attach during application startup. Break
on `AbstractAutowireCapableBeanFactory.createBean()` and measure time to
completion for each bean. Produce a ranked list of the slowest beans to
initialize. Useful for diagnosing slow Spring Boot startup. Demonstrates
`onMethodInvocation()` + `onCurrentMethodExit()` + return value inspection.

**10. Transaction Boundary Visualizer** — Monitor
`AbstractPlatformTransactionManager.getTransaction()` and `.commit()`/`.rollback()`.
For each transaction, log its duration, the initiating call site (via
`nearestCaller()`), and whether it committed or rolled back. Helps identify
long-running or unexpectedly-rolled-back transactions.

### Gradle / Maven (Build Tool Debugging)

**11. Slow Build Task Profiler** — Attach to a Gradle daemon. Break on
`org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute()`
and time each task. Produce a ranked report of the slowest tasks. Useful
alternative to `--profile` that can also capture stack traces of what each
task is doing when slow. Demonstrates attach-to-daemon workflow + method timing.

### HikariCP / JDBC Connection Pools

**12. Connection Leak Detector** — Attach to any app using HikariCP. Break on
`HikariPool.getConnection()` and `PoolEntry.recycle()`. Track which call sites
borrow connections and how long they hold them. Flag connections held longer
than a threshold (potential leaks) with full stack traces. Demonstrates
`onMethodInvocation()` + per-thread state tracking + `nearestCaller()`.

### Implementation Notes

- Start with examples **1** (Slow Request Tracer), **9** (Bean Initialization
  Profiler), and **12** (Connection Leak Detector) as they cover the most
  common debugging scenarios and require the simplest setup.
- Use embedded servers (embedded Tomcat/Jetty, Spring Boot jars) rather than
  full installations to keep examples self-contained.
- Each example should handle `VMDisconnectedException` gracefully and print a
  summary report on disconnect.
- Consider a shared `ExampleUtils` class for common patterns: threshold-based
  reporting, histogram formatting, CSV output, and graceful shutdown.
