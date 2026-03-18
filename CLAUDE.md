# jdiscript — Development Guide

jdiscript is a fluent Java wrapper around the Java Debug Interface (JDI).
This file is for working **on** the library. For writing debugger scripts
**with** jdiscript, see `.claude/skills/jdi-debug/SKILL.md`.

## Building

```bash
./gradlew build                              # everything
./gradlew :jdiscript:compileJava             # fast compile check
./gradlew :example:compileJava :log-config-debugger:compileJava  # verify examples
```

Requires Java 17+ (JDI is in `jdk.jdi` module). No test suite — the examples
are integration tests. Always compile both example modules after library changes.

## Project Layout

- `jdiscript/src/main/java/org/jdiscript/` — the library
  - `JDIScript.java` — main entry point, all convenience methods
  - `handlers/` — `@FunctionalInterface` event handlers (one per JDI event type)
  - `requests/` — chainable request builders (one per JDI request type)
  - `events/DebugEventDispatcher.java` — routes events to handlers
  - `util/RemoteObject.java` — safe remote object inspection
  - `util/VMSocketAttacher.java`, `VMLauncher.java`, etc. — VM connectors
- `example/` — basic single-file examples
- `log-config-debugger/` — full production example with test apps

## Where to Make Changes

| Goal | File(s) |
|------|---------|
| New convenience method | `JDIScript.java` |
| New JDI event type | `handlers/` + `requests/` + `DebugEventDispatcher` + `JDIScript` |
| Remote object inspection | `RemoteObject.java` |
| Event dispatching | `DebugEventDispatcher.java` / `EventThread.java` |
| New connection method | New class in `util/` |

## Design Principles

1. **Examples first.** Write a real script, notice what's painful, upstream
   the fix. `log-config-debugger` surfaced `RemoteObject`, predicate-based
   `nearestCaller`, and the null-location bug.
2. **Convenience methods absorb boilerplate.** If it takes 3+ lines of JDI
   ceremony, it belongs in `JDIScript` or `RemoteObject`.
3. **Lambda ergonomics.** Handlers stay as single-method `@FunctionalInterface`s.
4. **Null-safety at the boundary.** `Method.location()` is null for
   abstract/native methods — convenience methods guard internally.
5. **`RemoteObject` is static and stateless.**

## Pitfalls

- **Null locations**: Always check `m.location() != null` before `breakpointRequest()`.
- **Thread suspension**: `RemoteObject` methods need a suspended thread (automatic inside handlers).
- **IPv6**: Use `127.0.0.1` not `*` in JDWP addresses.
- **Hot paths**: Breakpoints pause the target. Read `StringReference` directly instead of remote `toString()`.
- **Module system**: Scripts need `--add-modules jdk.jdi`.

## Running the Full Example

```bash
# Terminal 1: start test app with JDWP
./gradlew :log-config-debugger:runMultiFrameworkApp -Pdebug
# Terminal 2: attach debugger
./gradlew :log-config-debugger:run --args="5005"
```

## Adding an Example Module

```groovy
plugins { id 'java'; id 'application' }
dependencies { implementation project(':jdiscript') }
java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }
application {
    mainClass = 'com.example.MyDebugger'
    applicationDefaultJvmArgs = ['--add-modules', 'jdk.jdi']
}
```

Add to `settings.gradle`. See `log-config-debugger/` as reference.
