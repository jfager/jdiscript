# JDI Debugger Overhead — Benchmark Results

Numbers below are from a single run on this machine (Java 17, Linux).
Run the suite yourself with `./gradlew :benchmarks:run`.

Each scenario launches `TargetApp` with a 2 s warmup followed by a 5 s
measurement window.  The reported figure is the median over three runs.

---

## Results

### Tight-loop workload — `doWork()` calls per 5 s

| Scenario | ops (median) | vs baseline |
|---|---:|---:|
| Baseline — no JDWP | 175,413,759 | 100.0% |
| JDWP agent loaded, `suspend=n`, no attach | 174,465,949 | 99.5% |
| JDI connected, zero event requests | 175,359,015 | 100.0% |
| Breakpoint on `doWork()` — fires every iteration | 22,964 | 0.013% |
| `MethodEntryRequest` — class filter to `TargetApp` | 20,650 | 0.012% |

### Thread-spawn workload — threads created per 5 s

| Scenario | ops (median) | vs baseline |
|---|---:|---:|
| Baseline — no JDWP | 32,611 | 100.0% |
| `ThreadStartRequest` — SUSPEND_ALL | 3,048 | 9.3% |

### Exception workload — throw+catch cycles per 5 s

| Scenario | ops (median) | vs baseline |
|---|---:|---:|
| Baseline — no JDWP | 7,329,480 | 100.0% |
| `ExceptionRequest` — caught, class filter | 3,499 | 0.048% |

---

## What the numbers mean

### Passive overhead is essentially zero

Loading the JDWP agent with `suspend=n` and never attaching costs less than
0.5%.  Having a debugger fully connected with no event requests registered is
indistinguishable from the baseline.  **Attaching a jdiscript debugger to a
running process has no measurable impact on throughput until it registers an
event request.**

### Every event with `SUSPEND_ALL` costs ~200–250 µs of wall time

The default suspend policy for breakpoints and method-entry requests is
`SUSPEND_ALL`: every hit stops every thread in the target JVM, ships an event
over the JDI socket, waits for the debugger to call `resume()`, and then
restarts all threads.  That round trip costs:

| Event type | Observed cost |
|---|---:|
| Breakpoint (tight loop) | ~218 µs per hit |
| `MethodEntryRequest` (tight loop) | ~242 µs per hit |

175 million no-overhead calls drop to about 23 thousand calls — a **~7,600×
slowdown**.  The target is paused for essentially all of the 5-second window.

Thread-start and exception events cost more per hit (~1.4–1.5 ms), probably
because the JDI protocol includes stack frame data along with those events.

### The practical breakdowns

**Breakpoints are fine on cold or infrequent paths.**  A breakpoint on a
method that is called once per request, a few times per second, is invisible
to users.  A breakpoint on a method called in a tight inner loop is not.

**`MethodEntryRequest` without a class filter is even worse** than what is
shown above: it fires on every method entry in every class in the JVM,
including JDK internals.  Always add `.addClassFilter(className)`.  Even with
a class filter the per-hit cost is the same as a breakpoint; the difference is
only in how many hits occur.

**`ThreadStartRequest`** cuts thread creation throughput by about 10×.  If the
target creates threads at a low rate (a few per second) this is invisible.  If
it creates hundreds per second (e.g., a thread-per-request server), monitoring
every thread start will noticeably impact response times.

**`ExceptionRequest` for caught exceptions** should almost always carry a class
filter.  Without one it fires on every exception thrown anywhere in the JVM
during startup and steady-state, and the per-hit cost (~1.4 ms) will tank
throughput on anything that uses exceptions for control flow.

### Rule of thumb

> The overhead of a JDI event is ~200 µs at minimum (pure suspend/resume
> round-trip).  Multiply by the rate at which that event fires in your target
> to estimate total slowdown.  If the result is more than a few percent of the
> method's budget, add a filter or use a different instrumentation strategy.

---

## Mitigation strategies

| Situation | Recommendation |
|---|---|
| Need to trace a hot method | Use `MethodExitRequest` on entry, read the return value — still slow, but avoids setting a second breakpoint. Consider sampling: set a breakpoint, record one hit, disable, re-enable after N ms. |
| Thread-start overhead too high | Filter with `.addClassFilter()` or use `ThreadDeathRequest` only (cheaper). |
| Exception events on a hot throw path | Add `.addClassFilter(throwingClass)` and consider switching to `notifyCaught=false` if you only care about uncaught exceptions. |
| Need low overhead on a busy class | `MethodExitRequest` with class filter has the same per-event cost as `MethodEntryRequest`, but fires less often for void/early-return methods. |
| Any hot-path event | Change the suspend policy to `SUSPEND_EVENT_THREAD` instead of `SUSPEND_ALL` to avoid stopping unrelated threads. Still pays the socket round-trip but reduces collateral pausing. |
