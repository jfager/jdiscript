package org.jdiscript.benchmark;

import com.sun.jdi.VirtualMachine;
import org.jdiscript.JDIScript;
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
 * during the 5-second measurement window.  Results are printed as a summary table
 * showing ops/5s and percentage relative to the no-JDWP baseline.
 *
 * Run with:
 *   ./gradlew :benchmarks:run
 *
 * Scenarios:
 *
 *   Tight-loop workload (how many doWork() calls fit in 5s):
 *     1. Baseline                 — no JDWP at all
 *     2. JDWP agent, no attach    — agent loaded, suspend=n, no debugger
 *     3. Connected, no requests   — JDI attached, zero event requests registered
 *     4. Breakpoint on doWork()   — breakpoint fires on every iteration (SUSPEND_ALL)
 *     5. Method entry             — MethodEntryRequest filtered to TargetApp class
 *
 *   Thread-spawn workload (threads created per 5s):
 *     6. Baseline                 — no JDWP
 *     7. Thread-start events      — ThreadStartRequest, SUSPEND_ALL
 *
 *   Exception workload (throw+catch cycles per 5s):
 *     8. Baseline                 — no JDWP
 *     9. Exception monitoring     — ExceptionRequest (caught, class filter), SUSPEND_ALL
 */
public class BenchmarkRunner {

    static final String JAVA       = System.getProperty("java.home") + "/bin/java";
    static final String TARGET_CP  = "./build/classes/java/targetapp";
    static final String TARGET_MAIN  = "org.jdiscript.benchmark.targetapp.TargetApp";
    static final String TARGET_CLASS = "org.jdiscript.benchmark.targetapp.TargetApp";
    static final int    RUNS       = 3;  // median of this many runs per scenario

    @FunctionalInterface interface Scenario   { long   run()         throws Exception; }
    @FunctionalInterface interface JdiSetup   { void   setup(JDIScript j) throws Exception; }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        System.out.printf(
            "Debugger overhead benchmarks — %d runs per scenario, ~7s each%n%n", RUNS);

        // --- Tight-loop group ---
        long baseTight   = go("baseline (no JDWP)",                () -> processRun("tight-loop"));
        long jdwpOnly    = go("JDWP agent, no attach (suspend=n)", () -> jdwpNoAttach("tight-loop"));
        long noRequests  = go("connected, no event requests",       () -> jdiRun("tight-loop", j -> {}));
        long bpLoop      = go("breakpoint on doWork() [SUSPEND_ALL]", BenchmarkRunner::breakpointInLoop);
        long methEntry   = go("method entry (class filter)",        () -> methodEntry("tight-loop"));

        System.out.println();

        // --- Thread-spawn group ---
        long baseThread  = go("baseline (no JDWP, thread-spawn)",  () -> processRun("thread-spawn"));
        long threadStart = go("thread-start events [SUSPEND_ALL]", BenchmarkRunner::threadStartEvents);

        System.out.println();

        // --- Exception group ---
        long baseExc     = go("baseline (no JDWP, exception)",     () -> processRun("exception"));
        long excMon      = go("exception monitoring (caught)",      BenchmarkRunner::exceptionMonitor);

        // --- Summary table ---
        printSummary(baseTight, jdwpOnly, noRequests, bpLoop, methEntry,
                     baseThread, threadStart,
                     baseExc, excMon);
    }

    // -------------------------------------------------------------------------
    // Scenario implementations
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

    /**
     * Breakpoint at the entry of doWork(), fires on every tight-loop iteration.
     * Uses onMethodInvocation which waits for ClassPrepare before setting the
     * breakpoint, guarding against null locations on abstract/native methods.
     */
    static long breakpointInLoop() throws Exception {
        return jdiRun("tight-loop", j ->
            j.onMethodInvocation(TARGET_CLASS, "doWork", e -> {}));
    }

    /**
     * MethodEntryRequest filtered to TargetApp, applied to the tight-loop workload.
     * Fires on every method entry in the class (doWork, tightLoop, etc.).
     */
    static long methodEntry(String workload) throws Exception {
        return jdiRun(workload, j ->
            j.methodEntryRequest(e -> {}).addClassFilter(TARGET_CLASS).enable());
    }

    /**
     * ThreadStartRequest (SUSPEND_ALL) against the thread-spawn workload.
     * Each new thread triggers a JDI notification and VM suspension.
     */
    static long threadStartEvents() throws Exception {
        return jdiRun("thread-spawn", j ->
            j.threadStartRequest(e -> {}).enable());
    }

    /**
     * ExceptionRequest monitoring caught exceptions thrown from TargetApp.
     * Class filter limits noise from JVM-internal exceptions during startup.
     */
    static long exceptionMonitor() throws Exception {
        return jdiRun("exception", j ->
            j.exceptionRequest(null, /*notifyCaught=*/ true, /*notifyUncaught=*/ false, e -> {})
             .addClassFilter(TARGET_CLASS)
             .enable());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Run the scenario RUNS times, print progress, return the median. */
    static long go(String label, Scenario s) throws Exception {
        System.out.printf("  %-44s", label + " ...");
        System.out.flush();
        List<Long> samples = new ArrayList<>();
        for (int i = 0; i < RUNS; i++) {
            samples.add(s.run());
        }
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
            if (t.startsWith("ops=")) {
                return Long.parseLong(t.substring(4));
            }
        }
        throw new IllegalStateException("No 'ops=N' in target output:\n" + out);
    }

    static int freePort() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) {
            return ss.getLocalPort();
        }
    }

    // -------------------------------------------------------------------------
    // Summary table
    // -------------------------------------------------------------------------

    static void printSummary(
        long baseTight, long jdwpOnly, long noRequests, long bpLoop, long methEntry,
        long baseThread, long threadStart,
        long baseExc,   long excMon)
    {
        System.out.println();
        System.out.println("=".repeat(68));
        System.out.println("  SUMMARY  (median ops over 5-second measurement window)");
        System.out.println("=".repeat(68));

        tableHeader("Tight-loop workload  (doWork() calls per 5s)");
        tableRow("Baseline — no JDWP",                baseTight,   baseTight);
        tableRow("JDWP agent, no attach (suspend=n)",  jdwpOnly,    baseTight);
        tableRow("Connected, no event requests",        noRequests,  baseTight);
        tableRow("Breakpoint on doWork() [SUSPEND_ALL]", bpLoop,    baseTight);
        tableRow("Method entry (class filter)",          methEntry,  baseTight);

        System.out.println();
        tableHeader("Thread-spawn workload  (threads created per 5s)");
        tableRow("Baseline — no JDWP",                 baseThread,  baseThread);
        tableRow("Thread-start events [SUSPEND_ALL]",   threadStart, baseThread);

        System.out.println();
        tableHeader("Exception workload  (throw+catch cycles per 5s)");
        tableRow("Baseline — no JDWP",                  baseExc,    baseExc);
        tableRow("Exception monitoring (caught)",         excMon,     baseExc);

        System.out.println();
    }

    static void tableHeader(String title) {
        System.out.println("  " + title);
        System.out.printf("  %-40s %14s %9s%n", "Scenario", "ops (median)", "vs base");
        System.out.println("  " + "-".repeat(66));
    }

    static void tableRow(String name, long ops, long base) {
        double pct = base == 0 ? 0 : ops * 100.0 / base;
        System.out.printf("  %-40s %,14d %8.1f%%%n", name, ops, pct);
    }
}
