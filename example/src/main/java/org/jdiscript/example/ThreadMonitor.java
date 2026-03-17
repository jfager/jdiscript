package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

/**
 * Monitors the lifecycle of all threads in a target program.
 *
 * Logs each thread start and death event with timestamps, and produces
 * a summary at the end showing each thread's name and approximate lifetime
 * in milliseconds.
 *
 * This demonstrates the use of {@code threadStartRequest} and
 * {@code threadDeathRequest} to observe thread lifecycle without
 * modifying the target program.
 */
public class ThreadMonitor {

    String OPTIONS = "-cp ./build/classes/java/example";
    String MAIN = "org.jdiscript.example.ThreadedExample";

    JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());

    final Map<Long, String> threadNames = new ConcurrentHashMap<>();
    final Map<Long, Long> threadStartTimes = new ConcurrentHashMap<>();
    final Map<Long, Long> threadLifetimes = new ConcurrentHashMap<>();

    OnVMStart start = se -> {
        j.threadStartRequest(event -> {
            long id = event.thread().uniqueID();
            String name = event.thread().name();
            long now = System.currentTimeMillis();
            threadNames.put(id, name);
            threadStartTimes.put(id, now);
            println("Thread started: " + name + " (id=" + id + ")");
        }).enable();

        j.threadDeathRequest(event -> {
            long id = event.thread().uniqueID();
            String name = event.thread().name();
            long now = System.currentTimeMillis();
            Long startTime = threadStartTimes.get(id);
            if (startTime != null) {
                threadLifetimes.put(id, now - startTime);
            }
            println("Thread died:    " + name + " (id=" + id + ")");
        }).enable();
    };

    public static void main(String[] args) {
        ThreadMonitor m = new ThreadMonitor();
        m.j.run(m.start);

        println("\nThread Lifetime Summary:");
        m.threadLifetimes.forEach((id, lifetime) -> {
            String name = m.threadNames.getOrDefault(id, "unknown");
            println("  " + name + " (id=" + id + "): " + lifetime + "ms");
        });
        println("Total threads observed: " + m.threadNames.size());
    }
}
