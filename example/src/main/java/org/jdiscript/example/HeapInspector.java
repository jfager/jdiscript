package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;

import java.util.List;
import java.util.Map;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMDeath;
import org.jdiscript.util.RemoteObject;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.BreakpointEvent;

/**
 * Demonstrates heap inspection and method timing using jdiscript.
 *
 * <p>Shows three complementary techniques for understanding object lifecycle
 * at runtime without modifying the target program:
 *
 * <ol>
 *   <li><b>Instance counting</b> — how many live instances of a class exist at any
 *       given moment, useful for spotting accumulation before it becomes an OOM.</li>
 *   <li><b>Instance inspection</b> — retrieve live instances and read their state,
 *       useful for diagnosing stale objects, unclosed resources, etc.</li>
 *   <li><b>Method timing</b> — measure wall-clock duration of each call without
 *       modifying the target, useful for slow-call detection and SLA monitoring.</li>
 * </ol>
 *
 * <p>Attaches to the bundled {@link HelloWorld} target for demonstration.
 * In production, replace the class and method names with the ones you care about
 * and switch to {@code VMSocketAttacher} or {@code VMProcessAttacher}.
 */
public class HeapInspector {

    static final String TARGET_CLASS  = "org.jdiscript.example.HelloWorld";
    static final String TARGET_METHOD = "main";
    static final String TARGET_OPTS   = "-cp ./build/classes/java/example";

    public static void main(String[] args) {
        JDIScript j = new JDIScript(new VMLauncher(TARGET_OPTS, TARGET_CLASS).start());

        // ---------------------------------------------------------------
        // 1. Instance counting — snapshot the live count at method entry
        // ---------------------------------------------------------------
        j.onMethodInvocation(TARGET_CLASS, TARGET_METHOD, (BreakpointEvent e) -> {
            long count = j.instanceCount(TARGET_CLASS);
            println("[instanceCount] live " + TARGET_CLASS + " instances: " + count);
        });

        // ---------------------------------------------------------------
        // 2. Instance inspection — print state of each live object
        //    (only interesting on classes that accumulate instances; here
        //    we just demonstrate the API surface)
        // ---------------------------------------------------------------
        j.onMethodInvocation(TARGET_CLASS, TARGET_METHOD, (BreakpointEvent e) -> {
            ThreadReference thread = e.thread();
            List<ObjectReference> instances = j.findInstances(TARGET_CLASS, 10);
            if (instances.isEmpty()) {
                println("[findInstances] no live instances yet (static context)");
            } else {
                println("[findInstances] live instances:");
                instances.forEach(ref -> {
                    String repr = RemoteObject.remoteToString(ref, thread);
                    println("  @" + ref.uniqueID() + " -> " + repr);
                });
            }
        });

        // ---------------------------------------------------------------
        // 3. Method timing — log every call with its wall-clock duration
        //    and all visible local variables at entry
        // ---------------------------------------------------------------
        j.onMethodTimed(TARGET_CLASS, TARGET_METHOD, (entry, durationMs) -> {
            println("[onMethodTimed] " + j.fullName(entry.location().method())
                    + " took " + durationMs + "ms");

            // Bonus: dump locals visible at entry (requires -g debug info)
            Map<String, String> locals = RemoteObject.locals(entry.thread());
            if (!locals.isEmpty()) {
                println("  locals at entry:");
                locals.forEach((name, value) ->
                    println("    " + name + " = " + value));
            }
        });

        j.run(List.of(
            (OnVMDeath) e -> println("[done]")
        ));
    }
}
