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
