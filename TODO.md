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

**5. Log Configuration Debugger (Dataproc / Spark Streaming)** — Attach to a
Spark driver or executor running on Dataproc. The goal is to answer "where are
my log settings actually coming from?" by intercepting the logging framework as
it initializes and configures itself. This is a common pain point because
Dataproc, Spark, YARN, and the application all compete to configure logging.

Concrete things to intercept:

- **Log4j 1.x** (still common on Dataproc):
  - `PropertyConfigurator.doConfigure()` / `DOMConfigurator.doConfigure()` —
    break here and inspect the argument to see which file/URL is being loaded.
  - `LogManager.getRootLogger()` first call — dump all appenders and their
    levels to show the effective configuration.
  - `Category.setLevel()` / `Category.setPriority()` — catch runtime level
    changes (Spark's `Utils.setLogLevel()`, Dataproc init scripts, etc.).
  - `FileAppender.setFile()` — reveals where log files actually end up.

- **Log4j 2.x** (newer Dataproc images):
  - `ConfigurationFactory.getConfiguration()` — shows which configuration
    source won (file, classpath resource, programmatic).
  - `AbstractConfiguration.start()` — dump all loggers and appenders after
    the configuration is built.
  - `LoggerConfig.setLevel()` — catch runtime reconfiguration.

- **SLF4J bridge detection**:
  - `StaticLoggerBinder.getSingleton()` — shows which SLF4J binding is active
    (logback, log4j-slf4j-impl, etc.). Multiple bindings on the classpath is a
    common Spark/Dataproc problem.

- **Spark-specific**:
  - `org.apache.spark.internal.Logging.initializeLogging()` — this is where
    Spark overrides your log config. Break here and inspect `log4j.properties`
    resolution via `Utils.getSparkClassLoader().getResource("log4j.properties")`.
  - Watch for `spark.driver.extraJavaOptions` and `spark.executor.extraJavaOptions`
    containing `-Dlog4j.configuration=` — inspect system properties at startup.

- **YARN / Dataproc layer**:
  - `System.setProperty()` calls where the key starts with `log4j` — catches
    YARN container launch scripts injecting log config overrides.
  - `ClassLoader.getResource("log4j.properties")` — log every resolution
    attempt to show classpath ordering issues (your jar's log4j.properties vs
    Spark's vs Dataproc's).

The script should produce a timeline report:
1. Which config files/resources were found on the classpath (and in what order).
2. Which one was actually loaded by the logging framework.
3. Every subsequent level change, appender addition, or reconfiguration.
4. The final effective configuration (all loggers, their levels, and appenders).

This directly addresses the "I have no idea how my logs are getting configured"
problem. Demonstrates `onClassPrep()` for late-loaded logging classes +
`onMethodInvocation()` + argument/return value inspection + system property
monitoring. Attaching to Dataproc requires SSH tunneling to the JDWP port
(`gcloud compute ssh -- -L 5005:localhost:5005`).

**6. Shuffle Spill Monitor** — Watch for disk spill events by breaking on
`ExternalSorter.spill()` or `UnsafeExternalSorter.spill()`. Log memory
pressure, partition counts, and stack traces when spills occur. Useful for
tuning `spark.shuffle.spill.numElementsForceSpillThreshold` and partition
counts. Demonstrates `onClassPrep()` to handle late-loaded classes +
`stacktraceKey()` histograms.

### Apache Flink

**7. Checkpoint Stall Debugger** — Attach to a Flink TaskManager. Monitor
`org.apache.flink.runtime.checkpoint.CheckpointBarrierHandler` to track
barrier alignment timing. Flag when a subtask takes too long to process a
barrier (a common cause of checkpoint timeouts). Demonstrates
`onMethodInvocation()` with timing + thread-scoped state tracking.

**8. Backpressure Source Locator** — Break on credit-based flow control methods
in Flink's network stack (`CreditBasedPartitionWriter`,
`InputChannelRecoveredStateHandler`). Track which operators are consuming
credits slowly and correlate with operator stack traces. Demonstrates
`monitorContendedEnterRequest()` + `stacktraceKey()`.

### Apache Kafka (Broker or Client)

**9. Consumer Rebalance Tracer** — Attach to a Kafka consumer application.
Break on `ConsumerCoordinator.onJoinComplete()` and
`ConsumerRebalanceListener.onPartitionsRevoked()`. Log partition assignments,
rebalance duration, and the stack trace triggering each rebalance. Useful for
diagnosing frequent rebalances caused by slow processing or session timeouts.
Demonstrates `onClassPrep()` + `onMethodInvocation()` + argument value
extraction.

### Spring Boot / Spring Framework

**10. Bean Initialization Profiler** — Attach during application startup. Break
on `AbstractAutowireCapableBeanFactory.createBean()` and measure time to
completion for each bean. Produce a ranked list of the slowest beans to
initialize. Useful for diagnosing slow Spring Boot startup. Demonstrates
`onMethodInvocation()` + `onCurrentMethodExit()` + return value inspection.

**11. Transaction Boundary Visualizer** — Monitor
`AbstractPlatformTransactionManager.getTransaction()` and `.commit()`/`.rollback()`.
For each transaction, log its duration, the initiating call site (via
`nearestCaller()`), and whether it committed or rolled back. Helps identify
long-running or unexpectedly-rolled-back transactions.

### Gradle / Maven (Build Tool Debugging)

**12. Slow Build Task Profiler** — Attach to a Gradle daemon. Break on
`org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute()`
and time each task. Produce a ranked report of the slowest tasks. Useful
alternative to `--profile` that can also capture stack traces of what each
task is doing when slow. Demonstrates attach-to-daemon workflow + method timing.

### HikariCP / JDBC Connection Pools

**13. Connection Leak Detector** — Attach to any app using HikariCP. Break on
`HikariPool.getConnection()` and `PoolEntry.recycle()`. Track which call sites
borrow connections and how long they hold them. Flag connections held longer
than a threshold (potential leaks) with full stack traces. Demonstrates
`onMethodInvocation()` + per-thread state tracking + `nearestCaller()`.

### Implementation Notes

- Start with examples **1** (Slow Request Tracer), **5** (Log Configuration
  Debugger), **10** (Bean Initialization Profiler), and **13** (Connection Leak
  Detector) as they cover the most common debugging scenarios and require the
  simplest setup.
- Use embedded servers (embedded Tomcat/Jetty, Spring Boot jars) rather than
  full installations to keep examples self-contained.
- Each example should handle `VMDisconnectedException` gracefully and print a
  summary report on disconnect.
- Consider a shared `ExampleUtils` class for common patterns: threshold-based
  reporting, histogram formatting, CSV output, and graceful shutdown.
