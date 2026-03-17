# jdiscript — Development Guide

jdiscript is a fluent Java wrapper around the Java Debug Interface (JDI).
This file is for working **on** the library itself. For writing debugger
scripts **with** jdiscript, see `.claude/skills/jdi-debug/SKILL.md`.

## Building and Testing

```bash
./gradlew build                    # build everything (library + examples)
./gradlew :jdiscript:build         # build just the library
./gradlew :jdiscript:compileJava   # fast compile check (no jar/tests)
./gradlew :example:compileJava     # compile basic examples
./gradlew :log-config-debugger:compileJava  # compile the logging tracer
```

Requires Java 17+ and Gradle 8.x (wrapper included). The JDI API is in the
`jdk.jdi` module — all script modules need `--add-modules jdk.jdi`.

There is no test suite yet. The examples serve as integration tests: compile
them to verify API changes don't break downstream usage. When modifying the
library, always compile both `:example` and `:log-config-debugger` to catch
regressions.

## Project Structure

```
jdiscript/                          # The library (org.jdiscript)
  src/main/java/org/jdiscript/
    JDIScript.java                  # Main entry point — all convenience methods live here
    handlers/                       # @FunctionalInterface event handlers (one per JDI event type)
    requests/                       # Chainable request builders (generated, one per JDI request type)
    events/
      DebugEventDispatcher.java     # Routes JDI events to registered handlers
    util/
      VMSocketAttacher.java         # Attach to JVM via socket
      VMProcessAttacher.java        # Attach to JVM via PID
      VMLauncher.java               # Launch a new debuggee
      VMListener.java               # Listen for debug connections
      RemoteObject.java             # Safe remote object inspection utilities
      Utils.java                    # println(), unchecked()

example/                            # Basic examples (one file each, simple patterns)
log-config-debugger/                # Full production-quality example
  src/main/java/                    #   The debugger script itself
  src/testapp/java/                 #   Target apps that exercise 5 logging frameworks
  build.gradle                      #   Custom source sets + JavaExec tasks

.claude/skills/jdi-debug/           # AI agent skill spec for writing scripts with jdiscript
```

## Architecture

### How the library works

1. User creates a `JDIScript` wrapping a JDI `VirtualMachine`
2. Convenience methods (`onMethodInvocation`, `onClassPrep`, etc.) create JDI
   event requests and register lambda handlers on them via `DebugEventDispatcher`
3. `JDIScript.run()` starts an `EventThread` that pulls events from the VM's
   event queue and dispatches them to registered handlers
4. Handlers run on the event thread with the target thread suspended

### Key design decisions

- **Handlers are `@FunctionalInterface`** — one per JDI event type, enabling
  lambda usage. They live in `org.jdiscript.handlers`.
- **Request builders are chainable** — `ChainingBreakpointRequest` etc. wrap
  JDI requests, add `addHandler()`, and return `this` for fluent configuration.
  Call `.enable()` to activate.
- **Convenience methods on `JDIScript`** handle common boilerplate:
  `onClassPrep` + `breakpointRequest` composition, null-location guards,
  class filter setup. These are the preferred API surface for users.
- **`RemoteObject`** centralizes the complexity of invoking methods on objects
  in the debugged VM (type hierarchy resolution, exception handling, fallbacks).

### Where to make changes

| Want to... | Modify... |
|------------|-----------|
| Add a new convenience method | `JDIScript.java` — add near similar methods |
| Support a new JDI event type | Add handler in `handlers/`, request builder in `requests/`, dispatch case in `DebugEventDispatcher`, convenience method in `JDIScript` |
| Improve remote object inspection | `RemoteObject.java` |
| Fix event dispatching | `DebugEventDispatcher.java` or `EventThread.java` |
| Add a new connection method | New class in `util/` following `VMSocketAttacher` pattern |

## Design Principles

1. **Write examples first.** The best way to find missing API is to write a
   real debugger script and notice what's painful. The `log-config-debugger`
   was built this way, and it surfaced the `RemoteObject` utility, predicate-based
   `nearestCaller`, and the null-location bug fix.

2. **Convenience methods should handle the boilerplate.** If a pattern requires
   3+ lines of JDI ceremony (null checks, class loading, type casting), it
   belongs in a convenience method on `JDIScript` or `RemoteObject`.

3. **Don't break the lambda ergonomics.** Handlers should stay as single-method
   `@FunctionalInterface`s. Convenience methods should accept handlers directly
   rather than requiring intermediate builder objects.

4. **Null-safety at the API boundary.** `Method.location()` returns null for
   abstract/native methods. Any convenience method that accepts a method name
   must guard against this internally. Users shouldn't need to remember.

5. **Keep `RemoteObject` static and stateless.** It's a utility class with
   static methods — no instances, no state. Each method handles its own error
   cases and returns a sensible fallback.

## Common Pitfalls When Developing

1. **Null locations**: `Method.location()` returns `null` for abstract and native
   methods. Any code that calls `breakpointRequest(m.location(), ...)` must check
   for null first. The `onMethodInvocation` convenience methods do this; raw
   `breakpointRequest` does not.

2. **Thread suspension**: `RemoteObject.invokeRemote()` and similar methods
   require the thread to be suspended. Inside event handlers this is automatic.
   Use `ObjectReference.INVOKE_SINGLE_THREADED` to avoid resuming other threads.

3. **IPv6**: `address=*:5005` fails in some container runtimes. Always use
   `address=127.0.0.1:5005` in docs and examples.

4. **Hot-path performance**: Every breakpoint pauses the target thread. For
   frequently-called methods, reading `StringReference` directly is much faster
   than invoking `toString()` via `RemoteObject`.

5. **The `jdk.jdi` module**: The JDI API isn't on the default module path.
   Script modules must include `--add-modules jdk.jdi` in their JVM args.

## Example Scripts as Test Cases

The examples double as integration tests for the library API. When making
changes to `JDIScript` or `RemoteObject`, verify that all examples still
compile:

```bash
./gradlew :example:compileJava :log-config-debugger:compileJava
```

To run the log-config-debugger end-to-end:

```bash
# Terminal 1: start the test app with JDWP
./gradlew :log-config-debugger:runMultiFrameworkApp -Pdebug

# Terminal 2: attach the debugger
./gradlew :log-config-debugger:run --args="5005"
```

### Writing a new example

New examples can go in `example/` (single-file scripts) or as their own
Gradle module (for complex scripts with dependencies). A new module needs:

```groovy
// build.gradle
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

Add the module to `settings.gradle`. The `log-config-debugger` module is the
reference for how to structure a complex example with its own test applications.
