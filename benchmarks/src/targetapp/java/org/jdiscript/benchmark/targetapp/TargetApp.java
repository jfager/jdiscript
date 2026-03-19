package org.jdiscript.benchmark.targetapp;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * Target application used by BenchmarkRunner.
 *
 * Runs one of several workloads for a fixed wall-clock window (2s warmup + 5s
 * measurement) and prints "ops=N" so the runner can parse throughput.
 *
 * Workloads:
 *   tight-loop         — calls doWork() as fast as possible; breakpoint / method-entry target
 *   thread-spawn       — creates and joins short-lived threads; thread-start target
 *   exception          — throws and catches exceptions in a loop; exception-event target
 *   multithreaded-tight — 4 threads each calling mtWork() as fast as possible
 *   paced-calls        — spins ~500 µs between doWork() calls (~10K calls/5s at baseline)
 *
 * Usage: TargetApp <workload>
 */
public class TargetApp {

    static final long WARMUP_MS  = 2_000;
    static final long MEASURE_MS = 5_000;

    // Accumulates doWork() calls during the measurement window.
    // Static so the JIT sees a real side-effect and won't eliminate the loop.
    static volatile long counter = 0;

    /** Breakpoint / method-entry target for single-threaded workloads. */
    public static void doWork() {
        counter++;
    }

    // Shared counter for multi-threaded workload; LongAdder avoids false sharing.
    static final LongAdder multiCounter = new LongAdder();

    /** Breakpoint target for the multi-threaded workload. */
    public static void mtWork() {
        multiCounter.increment();
    }

    // -------------------------------------------------------------------------
    // Workloads
    // -------------------------------------------------------------------------

    static long tightLoop() {
        // Warmup: run but discard count.
        long end = System.currentTimeMillis() + WARMUP_MS;
        while (System.currentTimeMillis() < end) {
            doWork();
        }
        // Measurement.
        counter = 0;
        end = System.currentTimeMillis() + MEASURE_MS;
        while (System.currentTimeMillis() < end) {
            doWork();
        }
        return counter;
    }

    static long threadSpawn() throws InterruptedException {
        // Warmup.
        long end = System.currentTimeMillis() + WARMUP_MS;
        while (System.currentTimeMillis() < end) {
            Thread t = new Thread(() -> {});
            t.start();
            t.join();
        }
        // Measurement: count threads created.
        long count = 0;
        end = System.currentTimeMillis() + MEASURE_MS;
        while (System.currentTimeMillis() < end) {
            Thread t = new Thread(() -> {});
            t.start();
            t.join();
            count++;
        }
        return count;
    }

    static long exceptionThrowing() {
        // Warmup.
        long end = System.currentTimeMillis() + WARMUP_MS;
        while (System.currentTimeMillis() < end) {
            try { throwIt(); } catch (RuntimeException ignored) {}
        }
        // Measurement.
        long count = 0;
        end = System.currentTimeMillis() + MEASURE_MS;
        while (System.currentTimeMillis() < end) {
            try { throwIt(); } catch (RuntimeException ignored) {}
            count++;
        }
        return count;
    }

    /** Separate method so the throw site has a stable location for JDI filtering. */
    static void throwIt() {
        throw new RuntimeException("benchmark");
    }

    /**
     * Four threads each calling mtWork() as fast as possible.
     * Good for comparing SUSPEND_ALL vs SUSPEND_EVENT_THREAD: with SUSPEND_ALL
     * every breakpoint hit stops all 4 threads; with SUSPEND_EVENT_THREAD only
     * the hitting thread pauses while the other three keep running.
     */
    static long multiThreadedTight() throws InterruptedException {
        int nThreads = 4;

        // Warmup.
        runThreads(nThreads, WARMUP_MS);

        // Measurement.
        multiCounter.reset();
        runThreads(nThreads, MEASURE_MS);
        return multiCounter.sum();
    }

    private static void runThreads(int n, long durationMs) throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(true);
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            threads[i] = new Thread(() -> { while (running.get()) mtWork(); }, "bench-" + i);
            threads[i].start();
        }
        Thread.sleep(durationMs);
        running.set(false);
        for (Thread t : threads) t.join();
    }

    /**
     * Tight loop with ~500 µs of CPU spin-work between doWork() calls.
     * Baseline delivers ~10K doWork() calls in 5 s; a 200 µs breakpoint overhead
     * then costs ~28% throughput rather than 99.99%, illustrating that event
     * overhead only matters relative to the inter-event interval.
     */
    static long pacedCalls() {
        // Warmup.
        long end = System.currentTimeMillis() + WARMUP_MS;
        while (System.currentTimeMillis() < end) {
            spinWait(500);
            doWork();
        }
        // Measurement.
        counter = 0;
        end = System.currentTimeMillis() + MEASURE_MS;
        while (System.currentTimeMillis() < end) {
            spinWait(500);
            doWork();
        }
        return counter;
    }

    /** Busy-spin for approximately {@code micros} microseconds. */
    static void spinWait(long micros) {
        long until = System.nanoTime() + micros * 1_000L;
        while (System.nanoTime() < until) Thread.onSpinWait();
    }

    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        String workload = args.length > 0 ? args[0] : "tight-loop";
        long ops = switch (workload) {
            case "tight-loop"          -> tightLoop();
            case "thread-spawn"        -> threadSpawn();
            case "exception"           -> exceptionThrowing();
            case "multithreaded-tight" -> multiThreadedTight();
            case "paced-calls"         -> pacedCalls();
            default -> throw new IllegalArgumentException("Unknown workload: " + workload);
        };
        System.out.println("ops=" + ops);
    }
}
