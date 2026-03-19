package org.jdiscript.benchmark;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import org.jdiscript.JDIScript;
import org.jdiscript.requests.ChainingBreakpointRequest;
import org.jdiscript.util.VMLauncher;

import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Measures the throughput overhead imposed by various JDI debugger configurations.
 *
 * Each scenario launches {@code TargetApp} with a specific workload and debugging
 * setup, then reads the "ops=N" output to get the number of operations completed
 * during the 5-second measurement window.  Results are printed as summary tables
 * showing ops/5s and percentage relative to the no-JDWP baseline.
 *
 * Run with:
 *   ./gradlew :benchmarks:run
 *
 * === Worst-case scenarios (hot path, every call traced) ===
 *
 *   Tight-loop workload (doWork() calls per 5s):
 *     1. Baseline                 — no JDWP
 *     2. JDWP agent, no attach    — agent loaded, suspend=n, no debugger
 *     3. Connected, no requests   — JDI attached, zero event requests
 *     4. Breakpoint on doWork()   — SUSPEND_ALL, fires every iteration
 *     5. Method entry             — MethodEntryRequest, class filter, SUSPEND_ALL
 *
 *   Thread-spawn workload (threads created per 5s):
 *     6. Baseline                 — no JDWP
 *     7. Thread-start events      — ThreadStartRequest, SUSPEND_ALL
 *
 *   Exception workload (throw+catch cycles per 5s):
 *     8. Baseline                 — no JDWP
 *     9. Exception monitoring     — ExceptionRequest, caught, class filter, SUSPEND_ALL
 *
 * === Realistic scenarios (mitigation strategies) ===
 *
 *   Suspend-policy variants (tight-loop workload):
 *    10. Breakpoint, SUSPEND_NONE           — no VM pause, async delivery
 *    11. Breakpoint, sampled (100 ms gap)   — disable after each hit, re-enable after 100 ms
 *
 *   Multi-threaded (4 threads × tight-loop):
 *    12. Baseline                           — 4 threads, no JDWP
 *    13. Breakpoint on mtWork() SUSPEND_ALL — all 4 threads pause on every hit
 *    14. Breakpoint on mtWork() SUSPEND_EVENT_THREAD — only hitting thread pauses
 *
 *   Paced calls (~500 µs of CPU work between calls, ~10K calls/5s at baseline):
 *    15. Baseline                           — no JDWP
 *    16. Breakpoint on doWork() SUSPEND_ALL — shows cost at a realistic call rate
 */
public class BenchmarkRunner {

    static final String JAVA         = System.getProperty("java.home") + "/bin/java";
    static final String TARGET_CP    = "./build/classes/java/targetapp";
    static final String TARGET_MAIN  = "org.jdiscript.benchmark.targetapp.TargetApp";
    static final String TARGET_CLASS = "org.jdiscript.benchmark.targetapp.TargetApp";
    static final int    RUNS         = 3;  // median of this many runs per scenario

    @FunctionalInterface interface Scenario { long run()              throws Exception; }
    @FunctionalInterface interface JdiSetup { void setup(JDIScript j) throws Exception; }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        System.out.printf(
            "Debugger overhead benchmarks — %d runs per scenario, ~7s each%n%n", RUNS);

        // --- Worst-case: tight-loop ---
        System.out.println("  -- worst case: single-threaded tight loop --");
        long baseTight  = go("baseline (no JDWP)",                () -> processRun("tight-loop"));
        long jdwpOnly   = go("JDWP agent, no attach (suspend=n)", () -> jdwpNoAttach("tight-loop"));
        long noRequests = go("connected, no event requests",       () -> jdiRun("tight-loop", j -> {}));
        long bpLoop     = go("breakpoint on doWork() [SUSPEND_ALL]", BenchmarkRunner::breakpointInLoop);
        long methEntry  = go("method entry (class filter)",        () -> methodEntry("tight-loop"));

        System.out.println();

        // --- Worst-case: thread-spawn ---
        System.out.println("  -- worst case: thread-spawn --");
        long baseThread  = go("baseline (no JDWP, thread-spawn)",  () -> processRun("thread-spawn"));
        long threadStart = go("thread-start events [SUSPEND_ALL]", BenchmarkRunner::threadStartEvents);

        System.out.println();

        // --- Worst-case: exception ---
        System.out.println("  -- worst case: exception --");
        long baseExc = go("baseline (no JDWP, exception)",  () -> processRun("exception"));
        long excMon  = go("exception monitoring (caught)",   BenchmarkRunner::exceptionMonitor);

        System.out.println();

        // --- Realistic: suspend-policy variants ---
        System.out.println("  -- realistic: suspend policy variants (tight-loop) --");
        long bpSuspendNone = go("breakpoint, SUSPEND_NONE",          BenchmarkRunner::breakpointSuspendNone);
        long bpSampled     = go("breakpoint, sampled (100 ms gap)",  BenchmarkRunner::breakpointSampled);

        System.out.println();

        // --- Realistic: multi-threaded ---
        System.out.println("  -- realistic: multi-threaded (4 threads) --");
        long baseMulti    = go("baseline (no JDWP, 4 threads)",             () -> processRun("multithreaded-tight"));
        long multiSuspAll = go("breakpoint SUSPEND_ALL, 4 threads",          BenchmarkRunner::multiThreadedSuspendAll);
        long multiSuspET  = go("breakpoint SUSPEND_EVENT_THREAD, 4 threads", BenchmarkRunner::multiThreadedSuspendEventThread);

        System.out.println();

        // --- Realistic: paced calls ---
        System.out.println("  -- realistic: paced calls (~500 µs work between calls) --");
        long basePaced = go("baseline (no JDWP, paced)",          () -> processRun("paced-calls"));
        long pacedBP   = go("breakpoint on doWork() SUSPEND_ALL", () -> breakpointOnDoWork("paced-calls"));

        // --- Summary tables ---
        printWorstCase(baseTight, jdwpOnly, noRequests, bpLoop, methEntry,
                       baseThread, threadStart,
                       baseExc, excMon);
        printRealistic(baseTight, bpLoop, bpSuspendNone, bpSampled,
                       baseMulti, multiSuspAll, multiSuspET,
                       basePaced, pacedBP);
    }

    // -------------------------------------------------------------------------
    // Scenario implementations — worst-case
    // -------------------------------------------------------------------------

    /** Plain process, no JDWP agent. */
    static long processRun(String workload) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(JAVA, "-cp", TARGET_CP, TARGET_MAIN, workload);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process proc = pb.start();
        String out = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        proc.waitFor();
        return parseOps(out);
    }

    /** JDWP agent loaded with suspend=n, no debugger ever attaches. */
    static long jdwpNoAttach(String workload) throws Exception {
        int port = freePort();
        ProcessBuilder pb = new ProcessBuilder(
            JAVA,
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:" + port,
            "-cp", TARGET_CP, TARGET_MAIN, workload
        );
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process proc = pb.start();
        String out = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        proc.waitFor();
        return parseOps(out);
    }

    /**
     * JDI attached via LaunchingConnector; the setup lambda registers event requests.
     * Target stdout is captured via a ByteArrayOutputStream passed to VMLauncher.
     */
    static long jdiRun(String workload, JdiSetup setup) throws Exception {
        ByteArrayOutputStream cap = new ByteArrayOutputStream();
        VirtualMachine vm = new VMLauncher(
            "-cp " + TARGET_CP,
            TARGET_MAIN + " " + workload,
            /*killOnShutdown=*/ false,
            cap,
            OutputStream.nullOutputStream()
        ).safeStart();
        JDIScript j = new JDIScript(vm);
        setup.setup(j);
        j.run();
        vm.process().waitFor();
        // The VMLauncher redirect thread is a daemon; give it a moment to flush.
        Thread.sleep(150);
        return parseOps(cap.toString(StandardCharsets.UTF_8));
    }

    /** Breakpoint at the entry of doWork(), SUSPEND_ALL, fires on every tight-loop iteration. */
    static long breakpointInLoop() throws Exception {
        return jdiRun("tight-loop", j ->
            j.onMethodInvocation(TARGET_CLASS, "doWork", e -> {}));
    }

    /** MethodEntryRequest with class filter, SUSPEND_ALL. */
    static long methodEntry(String workload) throws Exception {
        return jdiRun(workload, j ->
            j.methodEntryRequest(e -> {}).addClassFilter(TARGET_CLASS).enable());
    }

    /** ThreadStartRequest, SUSPEND_ALL. */
    static long threadStartEvents() throws Exception {
        return jdiRun("thread-spawn", j ->
            j.threadStartRequest(e -> {}).enable());
    }

    /** ExceptionRequest (caught, class filter), SUSPEND_ALL. */
    static long exceptionMonitor() throws Exception {
        return jdiRun("exception", j ->
            j.exceptionRequest(null, /*notifyCaught=*/ true, /*notifyUncaught=*/ false, e -> {})
             .addClassFilter(TARGET_CLASS)
             .enable());
    }

    // -------------------------------------------------------------------------
    // Scenario implementations — realistic
    // -------------------------------------------------------------------------

    /**
     * Breakpoint on doWork() with SUSPEND_NONE: the VM is never paused.
     * Events are delivered asynchronously to the debugger while the target
     * keeps running.  Useful for counting or logging invocations without
     * caring about inspecting state.
     */
    static long breakpointSuspendNone() throws Exception {
        return jdiRun("tight-loop", j ->
            j.onClassPrep(TARGET_CLASS, ev ->
                ev.referenceType().methodsByName("doWork").forEach(m -> {
                    if (m.location() != null)
                        j.breakpointRequest(m.location(), e -> {})
                         .setSuspendPolicy(EventRequest.SUSPEND_NONE)
                         .enable();
                })
            )
        );
    }

    /**
     * Sampled breakpoint: after each hit the request is disabled for 100 ms,
     * capping the event rate at ~10/s regardless of how fast doWork() runs.
     * The rest of the time the target runs at full speed.
     */
    static long breakpointSampled() throws Exception {
        return jdiRun("tight-loop", j ->
            j.onClassPrep(TARGET_CLASS, ev ->
                ev.referenceType().methodsByName("doWork").forEach(m -> {
                    if (m.location() == null) return;
                    // Array trick for effectively-final capture in the lambda.
                    ChainingBreakpointRequest[] holder = new ChainingBreakpointRequest[1];
                    holder[0] = j.breakpointRequest(m.location(), e -> {
                        holder[0].disable();
                        Thread.ofVirtual().start(() -> {
                            try { Thread.sleep(100); }
                            catch (InterruptedException ignored) {}
                            try { holder[0].enable(); }
                            catch (Exception ignored) {} // VM may have exited by the time we re-enable
                        });
                    });
                    holder[0].enable();
                })
            )
        );
    }

    /**
     * Breakpoint on mtWork() (the multi-threaded workload's call target) with
     * SUSPEND_ALL: every hit stops all 4 threads while the event is processed.
     */
    static long multiThreadedSuspendAll() throws Exception {
        return jdiRun("multithreaded-tight", j ->
            j.onMethodInvocation(TARGET_CLASS, "mtWork", e -> {}));
    }

    /**
     * Same as above but with SUSPEND_EVENT_THREAD: only the thread that hit the
     * breakpoint pauses; the other 3 threads keep running and accumulating ops.
     */
    static long multiThreadedSuspendEventThread() throws Exception {
        return jdiRun("multithreaded-tight", j ->
            j.onClassPrep(TARGET_CLASS, ev ->
                ev.referenceType().methodsByName("mtWork").forEach(m -> {
                    if (m.location() != null)
                        j.breakpointRequest(m.location(), e -> {})
                         .setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD)
                         .enable();
                })
            )
        );
    }

    /**
     * Breakpoint on doWork() with SUSPEND_ALL for the paced-calls workload.
     * At ~10K doWork() calls/5s the 200 µs overhead per hit is much more
     * manageable than in the tight-loop case.
     */
    static long breakpointOnDoWork(String workload) throws Exception {
        return jdiRun(workload, j ->
            j.onMethodInvocation(TARGET_CLASS, "doWork", e -> {}));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Run the scenario RUNS times, print progress, return the median. */
    static long go(String label, Scenario s) throws Exception {
        System.out.printf("  %-48s", label + " ...");
        System.out.flush();
        List<Long> samples = new ArrayList<>();
        for (int i = 0; i < RUNS; i++) samples.add(s.run());
        long med = median(samples);
        System.out.printf("%,14d ops%n", med);
        return med;
    }

    static long median(List<Long> vals) {
        List<Long> s = new ArrayList<>(vals);
        Collections.sort(s);
        return s.get(s.size() / 2);
    }

    static long parseOps(String out) {
        for (String line : out.split("\\R")) {
            String t = line.trim();
            if (t.startsWith("ops=")) return Long.parseLong(t.substring(4));
        }
        throw new IllegalStateException("No 'ops=N' in target output:\n" + out);
    }

    static int freePort() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) { return ss.getLocalPort(); }
    }

    // -------------------------------------------------------------------------
    // Summary tables
    // -------------------------------------------------------------------------

    static void printWorstCase(
        long baseTight, long jdwpOnly, long noRequests, long bpLoop, long methEntry,
        long baseThread, long threadStart,
        long baseExc,   long excMon)
    {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("  WORST-CASE SUMMARY  (every call traced, SUSPEND_ALL)");
        System.out.println("=".repeat(70));

        tableHeader("Tight-loop workload  (doWork() calls per 5s)");
        tableRow("Baseline — no JDWP",                 baseTight,  baseTight);
        tableRow("JDWP agent, no attach (suspend=n)",   jdwpOnly,   baseTight);
        tableRow("Connected, no event requests",         noRequests, baseTight);
        tableRow("Breakpoint on doWork() [SUSPEND_ALL]", bpLoop,    baseTight);
        tableRow("Method entry (class filter)",           methEntry, baseTight);

        System.out.println();
        tableHeader("Thread-spawn workload  (threads created per 5s)");
        tableRow("Baseline — no JDWP",               baseThread,  baseThread);
        tableRow("Thread-start events [SUSPEND_ALL]", threadStart, baseThread);

        System.out.println();
        tableHeader("Exception workload  (throw+catch cycles per 5s)");
        tableRow("Baseline — no JDWP",           baseExc, baseExc);
        tableRow("Exception monitoring (caught)", excMon,  baseExc);

        System.out.println();
    }

    static void printRealistic(
        long baseTight, long bpSuspAll, long bpSuspendNone, long bpSampled,
        long baseMulti, long multiSuspAll,  long multiSuspET,
        long basePaced, long pacedBP)
    {
        System.out.println("=".repeat(70));
        System.out.println("  REALISTIC SUMMARY  (mitigation strategies)");
        System.out.println("=".repeat(70));

        tableHeader("Suspend-policy variants — tight-loop, breakpoint on doWork()");
        tableRow("Baseline — no JDWP",              baseTight,    baseTight);
        tableRow("Breakpoint, SUSPEND_ALL",          bpSuspAll,    baseTight);
        tableRow("Breakpoint, SUSPEND_NONE",         bpSuspendNone, baseTight);
        tableRow("Breakpoint, sampled (100 ms gap)", bpSampled,    baseTight);

        System.out.println();
        tableHeader("Multi-threaded (4 threads) — breakpoint on mtWork()");
        tableRow("Baseline — no JDWP, 4 threads",        baseMulti,   baseMulti);
        tableRow("Breakpoint, SUSPEND_ALL",               multiSuspAll, baseMulti);
        tableRow("Breakpoint, SUSPEND_EVENT_THREAD",      multiSuspET,  baseMulti);

        System.out.println();
        tableHeader("Paced calls (~500 µs work between calls) — breakpoint on doWork()");
        tableRow("Baseline — no JDWP",              basePaced, basePaced);
        tableRow("Breakpoint, SUSPEND_ALL",          pacedBP,   basePaced);

        System.out.println();
    }

    static void tableHeader(String title) {
        System.out.println("  " + title);
        System.out.printf("  %-42s %14s %9s%n", "Scenario", "ops (median)", "vs base");
        System.out.println("  " + "-".repeat(68));
    }

    static void tableRow(String name, long ops, long base) {
        double pct = base == 0 ? 0 : ops * 100.0 / base;
        System.out.printf("  %-42s %,14d %8.1f%%%n", name, ops, pct);
    }
}
