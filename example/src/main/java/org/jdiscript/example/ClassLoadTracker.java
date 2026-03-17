package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;

import java.util.ArrayList;
import java.util.List;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

/**
 * Tracks the order and timing of class loading in a target program.
 *
 * Records each class prepare event with a timestamp, then produces a
 * chronological report of all classes loaded during execution. Filters
 * to show only application classes by default (those in the
 * {@code org.jdiscript} package), though this can be easily adjusted.
 *
 * This demonstrates the use of {@code classPrepareRequest} with class
 * filters to monitor class loading behavior.
 */
public class ClassLoadTracker {

    String OPTIONS = "-cp ./build/classes/java/example";
    String MAIN = "org.jdiscript.example.HelloWorld";

    JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());

    static class ClassLoadEvent {
        final long timestamp;
        final String className;
        final String classLoader;

        ClassLoadEvent(long timestamp, String className, String classLoader) {
            this.timestamp = timestamp;
            this.className = className;
            this.classLoader = classLoader;
        }
    }

    final List<ClassLoadEvent> events = new ArrayList<>();
    long startTime;

    OnVMStart start = se -> {
        startTime = System.currentTimeMillis();

        j.classPrepareRequest(event -> {
            long elapsed = System.currentTimeMillis() - startTime;
            String className = event.referenceType().name();
            String loader = String.valueOf(event.referenceType().classLoader());
            events.add(new ClassLoadEvent(elapsed, className, loader));
            println("+" + elapsed + "ms: Loaded " + className);
        }).addClassFilter("org.jdiscript.*")
          .enable();
    };

    public static void main(String[] args) {
        ClassLoadTracker t = new ClassLoadTracker();
        t.j.run(t.start);

        println("\nClass Loading Summary:");
        println("Total classes loaded: " + t.events.size());
        if (!t.events.isEmpty()) {
            long first = t.events.get(0).timestamp;
            long last = t.events.get(t.events.size() - 1).timestamp;
            println("Time span: " + first + "ms to +" + last + "ms");
        }
        println("\nLoad Order:");
        for (int i = 0; i < t.events.size(); i++) {
            ClassLoadEvent e = t.events.get(i);
            println("  " + (i + 1) + ". +" + e.timestamp + "ms " + e.className
                    + " [" + e.classLoader + "]");
        }
    }
}
