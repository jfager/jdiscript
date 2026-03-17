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

## Add Tests

There are currently zero tests in the project. Useful starting points:
- Unit tests for `Utils`, `VMLauncher`, `VMSocketAttacher`.
- Integration tests that launch a simple target VM and verify event dispatch
  (breakpoints, class prepare, method entry/exit).
- Tests for the `DebugEventDispatcher` routing logic.
