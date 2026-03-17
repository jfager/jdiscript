package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.ThreadReference;

/**
 * Profiles method execution time for a target class.
 *
 * Sets breakpoints at method entry points and uses
 * {@code onCurrentMethodExit} to measure wall-clock time spent in each
 * method invocation. Produces a summary with total time, call count,
 * and average time per call for each method.
 *
 * This demonstrates combining {@code onMethodInvocation} with
 * {@code onCurrentMethodExit} to build a lightweight method profiler
 * without modifying the target program.
 */
public class MethodTimer {

    String OPTIONS = "-cp ./build/classes/java/example";
    String MAIN = "org.jdiscript.example.HelloWorld";
    String TARGET_CLASS = "org.jdiscript.example.HelloWorld";

    JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());

    static class MethodStats {
        final List<Long> durations = new ArrayList<>();

        void record(long nanos) {
            durations.add(nanos);
        }

        int count() {
            return durations.size();
        }

        long totalNanos() {
            long sum = 0;
            for (long d : durations) sum += d;
            return sum;
        }

        long avgNanos() {
            return count() > 0 ? totalNanos() / count() : 0;
        }
    }

    final Map<String, MethodStats> stats = new HashMap<>();

    OnBreakpoint breakpoint = be -> {
        ThreadReference thread = be.thread();
        String methodName = j.fullName(be.location().method());
        long entryTime = System.nanoTime();

        j.onCurrentMethodExitUnchecked(thread, exit -> {
            long elapsed = System.nanoTime() - entryTime;
            stats.computeIfAbsent(methodName, k -> new MethodStats())
                 .record(elapsed);
        });
    };

    OnVMStart start = se -> {
        j.onClassPrep(TARGET_CLASS, cp -> {
            cp.referenceType().methods().stream()
                .filter(m -> !m.isAbstract() && !m.isNative())
                .filter(m -> m.location() != null)
                .forEach(m -> j.breakpointRequest(m.location(), breakpoint).enable());
        });
    };

    public static void main(String[] args) {
        MethodTimer t = new MethodTimer();
        t.j.run(t.start);

        println("\nMethod Timing Summary:");
        t.stats.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().totalNanos(), a.getValue().totalNanos()))
            .forEach(e -> {
                MethodStats s = e.getValue();
                println(String.format("  %-60s calls=%-4d total=%-10s avg=%s",
                    e.getKey(),
                    s.count(),
                    formatNanos(s.totalNanos()),
                    formatNanos(s.avgNanos())));
            });
    }

    static String formatNanos(long nanos) {
        if (nanos < 1_000) return nanos + "ns";
        if (nanos < 1_000_000) return String.format("%.1fus", nanos / 1_000.0);
        if (nanos < 1_000_000_000) return String.format("%.1fms", nanos / 1_000_000.0);
        return String.format("%.2fs", nanos / 1_000_000_000.0);
    }
}
