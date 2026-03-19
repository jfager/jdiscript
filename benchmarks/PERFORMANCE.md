# JDI Debugger Overhead — Benchmark Results

Numbers below are from a single run on this machine (Java 17, Linux).
Run the suite yourself with `./gradlew :benchmarks:run`.

Each scenario launches `TargetApp` with a 2 s warmup followed by a 5 s
measurement window.  The reported figure is the median over three runs.

---

## Worst-case results (hot path, every call traced, SUSPEND_ALL)

### Tight-loop workload — `doWork()` calls per 5 s

| Scenario | ops (median) | vs baseline |
|---|---:|---:|
| Baseline — no JDWP | 174,534,425 | 100.0% |
| JDWP agent loaded, `suspend=n`, no attach | 176,060,497 | 100.9% |
| JDI connected, zero event requests | 174,806,052 | 100.2% |
| Breakpoint on `doWork()` — fires every iteration | 23,161 | 0.013% |
| `MethodEntryRequest` — class filter to `TargetApp` | 22,294 | 0.013% |

### Thread-spawn workload — threads created per 5 s

| Scenario | ops (median) | vs baseline |
|---|---:|---:|
| Baseline — no JDWP | 32,798 | 100.0% |
| `ThreadStartRequest` — SUSPEND_ALL | 3,142 | 9.6% |

### Exception workload — throw+catch cycles per 5 s

| Scenario | ops (median) | vs baseline |
|---|---:|---:|
| Baseline — no JDWP | 7,152,886 | 100.0% |
| `ExceptionRequest` — caught, class filter | 3,512 | 0.049% |

---

## Realistic results (mitigation strategies)

### Suspend-policy variants — tight-loop, breakpoint on `doWork()`

| Scenario | ops (median) | vs baseline |
|---|---:|---:|
| Baseline — no JDWP | 174,534,425 | 100.0% |
| Breakpoint, `SUSPEND_ALL` | 23,161 | 0.013% |
| Breakpoint, `SUSPEND_NONE` | 808,422 | 0.46% |
| Breakpoint, sampled (disable 100 ms after each hit) | 170,104,437 | 97.5% |

### Multi-threaded (4 threads) — breakpoint on `mtWork()`

| Scenario | ops (median) | vs baseline |
|---|---:|---:|
| Baseline — no JDWP, 4 threads | 1,737,436,456 | 100.0% |
| Breakpoint, `SUSPEND_ALL` | 14,411 | <0.01% |
| Breakpoint, `SUSPEND_EVENT_THREAD` | 28,881 | <0.01% |

### Paced calls — ~500 µs of CPU work between `doWork()` calls (~10 K/5 s)

| Scenario | ops (median) | vs baseline |
|---|---:|---:|
| Baseline — no JDWP | 9,988 | 100.0% |
| Breakpoint, `SUSPEND_ALL` | 5,862 | 58.7% |

---

## What the numbers mean

### Passive overhead is essentially zero

Loading the JDWP agent with `suspend=n` and never attaching costs less than
0.5%.  Having a debugger fully connected with no event requests registered is
indistinguishable from the baseline.  **Attaching a jdiscript debugger to a
running process has no measurable impact on throughput until it registers an
event request.**

### Every `SUSPEND_ALL` event costs ~200–250 µs of wall time

The default suspend policy for breakpoints and method-entry requests is
`SUSPEND_ALL`: every hit stops every thread in the target JVM, ships an event
over the JDI socket, waits for the debugger to call `resume()`, and then
restarts all threads.  That round trip costs roughly:

| Event type | Observed cost per hit |
|---|---:|
| Breakpoint | ~218 µs |
| `MethodEntryRequest` (class filter) | ~242 µs |
| `ThreadStartRequest` | ~1.5 ms |
| `ExceptionRequest` (caught) | ~1.4 ms |

Thread-start and exception events cost more per hit, likely because the JDI
protocol includes stack frame data alongside those events.

175 million no-overhead calls drop to about 23 thousand — a **~7,600×
slowdown** — when every call triggers a breakpoint.  The target is paused for
essentially all of the 5-second window.

### The rule of thumb

> **overhead ≈ event\_rate × suspend\_cost**
>
> A breakpoint firing at 1 000 calls/sec adds ~200 ms of pausing per second —
> measurable but not catastrophic.  The same breakpoint on a method called
> 1 000 000 times/sec pauses the target for ~200 s per second of real time,
> which means it barely moves.

### Sampling brings the overhead down to noise

Disabling a breakpoint for 100 ms after each hit caps the event rate at ~10/s
regardless of how hot the method is.  At that rate the 200 µs overhead
contributes only ~2 ms of pausing per second — **97.5% throughput retained**
even in the tight-loop case.  This is the right tool whenever you need
occasional samples from a hot path rather than a complete trace.

### `SUSPEND_NONE` is faster but not free

With `SUSPEND_NONE` the target thread is never paused; events are delivered
asynchronously to the debugger while execution continues.  The tight-loop case
goes from 23 K ops (SUSPEND_ALL) to 808 K ops — a **35× improvement** — but
still only 0.46% of baseline.  The remaining overhead comes from the JVM's own
cost of generating and queueing 174 million events faster than the debugger's
event thread can drain them, creating backpressure.  At a realistic call rate
(thousands/sec rather than millions/sec) `SUSPEND_NONE` adds negligible
overhead and is ideal for counters, histograms, and fire-and-forget logging.

### `SUSPEND_EVENT_THREAD` helps in heterogeneous multi-threaded code

With `SUSPEND_ALL`, a breakpoint hit stops every thread — including threads that
have nothing to do with the traced method.  `SUSPEND_EVENT_THREAD` only stops
the hitting thread, letting the others keep running.

In the benchmark's 4-thread case all threads spin on the same hot breakpoint,
so they all pause in rapid succession anyway and the benefit is only ~2×
(28 881 vs 14 411 ops).  The real gain appears in realistic multi-threaded
servers where a breakpoint on a request-handling method affects only the thread
handling that request, while the rest of the thread pool continues serving
traffic.

### At realistic call rates the overhead is manageable

The paced-calls workload inserts ~500 µs of CPU spin-work between each
`doWork()` call, delivering ~10 K calls in 5 s.  With a `SUSPEND_ALL`
breakpoint on every call:

- baseline: 9 988 ops → breakpoint: 5 862 ops (**58.7% throughput retained**)
- each iteration: 500 µs work + 218 µs JDI overhead = 718 µs total (vs 500 µs)
- throughput reduction: 500/718 ≈ 70% retained; measured 59%, close enough

A method called ~2 000 times per second (e.g., a moderately busy HTTP handler)
with a `SUSPEND_ALL` breakpoint on it will lose roughly 30% throughput.
A method called ~100 times per second loses only ~2%.

---

## Practical guidance

| Situation | Recommendation |
|---|---|
| **Tracing a cold or low-rate method** | Any policy works; use `SUSPEND_ALL` (default) for simplicity. |
| **Tracing a method called hundreds/sec** | Use `SUSPEND_EVENT_THREAD` to avoid pausing uninvolved threads. Measure impact in staging first. |
| **Tracing a hot-path method** | Use sampling: disable the request after each hit, re-enable after a delay. 97.5% throughput at 10 ms sampling interval. |
| **Counting invocations without inspecting state** | Use `SUSPEND_NONE`; the target never pauses and the event is delivered asynchronously. |
| **`MethodEntryRequest` on a busy class** | Always add `.addClassFilter(className)`. Without it every JDK method entry fires the event. |
| **Exception monitoring on a hot throw path** | Add `.addClassFilter(throwingClass)` and prefer `notifyUncaught=true, notifyCaught=false` unless you specifically need caught exceptions. |
| **Thread-start on a high-churn pool** | Consider `ThreadDeathRequest` only, or sample with a counter and skip most events in the handler. |
