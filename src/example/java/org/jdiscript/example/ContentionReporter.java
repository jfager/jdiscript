package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;
import static org.jdiscript.util.Utils.unsafe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

        class ContentionTracker {
            int counter = 0;
            Set<Location> locations = new HashSet<>();
            Set<String> sourceNames = new HashSet<>();
            Map<String, AtomicInteger> threads = new HashMap<>();
        }

        Map<Long, ContentionTracker> contended = new HashMap<>();

        OnMonitorContendedEnter monitorContendedEnter = e -> {
            ThreadReference tref = e.thread();
            ObjectReference mref = e.monitor();

            ContentionTracker t = contended.computeIfAbsent(mref.uniqueID(),
                                                            k -> new ContentionTracker());

            unsafe(() -> {
                t.counter += 1;
                t.locations.add(e.location());
                t.sourceNames.addAll(mref.referenceType().sourcePaths(null));
            });

            String threadKey = tref.name() + tref.uniqueID();
            AtomicInteger threadCount = t.threads.computeIfAbsent(threadKey, 
                                                                  k -> new AtomicInteger(0));
            threadCount.addAndGet(1);
        };

        j.monitorContendedEnterRequest().addHandler(monitorContendedEnter).enable();
        j.run(10 * 1000);

        println("Shutting down");
        vm.process().destroy();

        println("Contention info:");
        contended.forEach((k,v) -> {
            println("MonitorID: "+k+", Hits: "+v.counter);
            println("\tLocations: "+v.locations);
            println("\tMonitor source: "+v.sourceNames);
            println("\tThreads: "+v.threads);
        });
    }
}
