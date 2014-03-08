package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;
import static org.jdiscript.util.Utils.unsafe;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnMonitorContendedEnter;
import org.jdiscript.util.VMSocketAttacher;

import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

class ContentionReporter {

    public static void main(String[] args) {
        VirtualMachine vm = new VMSocketAttacher(12345).attach();
        JDIScript j = new JDIScript(vm);
        BiFunction<Integer, Integer, Integer> add = (v1,v2) -> v1+v2;

        class ContentionTracker {
            int counter = 0;
            Map<Long, Integer> monitorIds = new HashMap<>();
            Map<String, Integer> callers = new HashMap<>();
            Map<String, Integer> threads = new HashMap<>();
        }

        Map<String, ContentionTracker> contended = new HashMap<>();

        OnMonitorContendedEnter monitorContendedEnter = e -> {
            ThreadReference tref = e.thread();
            ObjectReference mref = e.monitor();

            ContentionTracker t = contended.computeIfAbsent(e.location().toString(),
                                                            k -> new ContentionTracker());

            unsafe(() -> {
                t.counter += 1;
                t.monitorIds.merge(mref.uniqueID(), 1, add);
                t.callers.merge(tref.frame(1).location().toString(), 1, add);
                t.threads.merge(tref.name() + tref.uniqueID(), 1, add);
            });
        };

        j.monitorContendedEnterRequest().addHandler(monitorContendedEnter).enable();
        j.run();

        println("Contention info:");
        contended.forEach((k,v) -> {
            println("Location: "+k+", Hits: "+v.counter);
            println("\tCallers: "+v.callers);
            println("\tThreads: "+v.threads);
            println("\tMonitorIds: "+v.monitorIds);
        });
    }
}
