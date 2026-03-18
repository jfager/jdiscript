# Future Improvements

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

Tests exist (24 tests across 3 files: `UtilsTest`, `DebugEventDispatcherTest`,
`JDIScriptIntegrationTest`) but coverage is thin.

Areas that could use additional coverage:
- `RemoteObject` — mock-based tests for `remoteToString`, `invokeRemote`, error paths.
- `VMLauncher` and `VMSocketAttacher` — startup and connection logic.
- Watchpoint events (`AccessWatchpointEvent`, `ModificationWatchpointEvent`).
- `MonitorContendedEnterEvent`, `MonitorWaitEvent`, and other less common events.
- Error paths in `JDIScript.run()` (e.g. `VMDisconnectedException` handling).
- The new return values from convenience methods (`onClassPrep`, `onMethodInvocation`,
  etc.) — verify the returned request can be disabled/deleted.

## Production Debugging Examples

The `log-config-debugger` module demonstrates a realistic production debugging
scenario. More examples in the same vein would be valuable:

- **Slow Request Tracer** — Attach to embedded Tomcat/Jetty, time
  `HttpServlet.service()` calls, report requests exceeding a threshold.
- **Bean Initialization Profiler** — Attach during Spring Boot startup, time
  `createBean()` calls, rank slowest beans.
- **Connection Leak Detector** — Attach to HikariCP, track `getConnection()` /
  `recycle()` pairs, flag long-held connections with stack traces.
- **Consumer Rebalance Tracer** — Attach to Kafka consumer, log partition
  assignments and rebalance timing.

Each should include a self-contained test app (like `log-config-debugger`'s
`testapp` source set) and a README with the scenario, setup, and expected output.
