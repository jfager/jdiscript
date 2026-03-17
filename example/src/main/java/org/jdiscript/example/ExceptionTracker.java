package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.Location;

/**
 * Tracks all exceptions thrown during execution of a target program.
 *
 * Reports exception type, whether each was caught or uncaught, the throw
 * location, and the catch location (if caught). Produces a summary histogram
 * at the end showing exception frequency by type and throw site.
 *
 * This demonstrates the use of {@code exceptionRequest} to observe both
 * caught and uncaught exceptions without modifying the target program.
 */
public class ExceptionTracker {

    String OPTIONS = "-cp ./build/classes/java/example";
    String MAIN = "org.jdiscript.example.ExceptionExample";

    JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());

    final Map<String, AtomicLong> exceptionCounts = new HashMap<>();

    OnVMStart start = se -> {
        j.exceptionRequest(null, true, true, event -> {
            String typeName = event.exception().referenceType().name();
            Location throwLoc = event.location();
            Location catchLoc = event.catchLocation();

            String caught = (catchLoc != null) ? "caught at " + catchLoc : "uncaught";
            println(typeName + " thrown at " + throwLoc + " (" + caught + ")");

            String key = typeName + " @ " + throwLoc;
            exceptionCounts.computeIfAbsent(key, k -> new AtomicLong(0))
                           .incrementAndGet();
        }).addClassExclusionFilter("java.*")
          .addClassExclusionFilter("sun.*")
          .addClassExclusionFilter("jdk.*")
          .enable();
    };

    public static void main(String[] args) {
        ExceptionTracker t = new ExceptionTracker();
        t.j.run(t.start);

        println("\nException Summary:");
        t.exceptionCounts.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
            .forEach(e -> println("  " + e.getValue() + "x " + e.getKey()));
    }
}
