package org.jdiscript.benchmark.targetapp;

/**
 * Target application used by BenchmarkRunner.
 *
 * Runs one of three workloads for a fixed wall-clock window (2s warmup + 5s
 * measurement) and prints "ops=N" so the runner can parse throughput.
 *
 * Workloads:
 *   tight-loop    — calls doWork() as fast as possible; breakpoint / method-entry target
 *   thread-spawn  — creates and joins short-lived threads; thread-start target
 *   exception     — throws and catches exceptions in a loop; exception-event target
 *
 * Usage: TargetApp <workload>
 */
public class TargetApp {

    static final long WARMUP_MS  = 2_000;
    static final long MEASURE_MS = 5_000;

    // Accumulates doWork() calls during the measurement window.
    // Static so the JIT sees a real side-effect and won't eliminate the loop.
    static volatile long counter = 0;

    /** Breakpoint / method-entry target — do not inline. */
    public static void doWork() {
        counter++;
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

    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        String workload = args.length > 0 ? args[0] : "tight-loop";
        long ops = switch (workload) {
            case "tight-loop"   -> tightLoop();
            case "thread-spawn" -> threadSpawn();
            case "exception"    -> exceptionThrowing();
            default -> throw new IllegalArgumentException("Unknown workload: " + workload);
        };
        System.out.println("ops=" + ops);
    }
}
