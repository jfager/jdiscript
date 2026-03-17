package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

public class StacktraceHistogram {

    String OPTIONS = "-cp ./build/classes/java/example";
    String MAIN = "org.jdiscript.example.HelloWorld";

    JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());

    final Map<String, AtomicLong> stacktraces = new HashMap<>();

    OnBreakpoint breakpoint = be -> {
        String trace = j.stacktraceKey(be.thread());
        stacktraces.computeIfAbsent(trace, k -> new AtomicLong(0))
                   .incrementAndGet();
    };

    OnVMStart start = se -> {
        j.onClassPrep(p -> {
            if(p.referenceType().name().equals("org.jdiscript.example.HelloWorld")) {
                p.referenceType().methodsByName("sayHello").forEach(m -> {
                    j.breakpointRequest(m.location(), breakpoint).enable();
                });                
            }
        });
    };         

    public static void main(String[] args) {
        StacktraceHistogram s = new StacktraceHistogram();
        s.j.run(s.start);
        
        println("Histogram:");
        s.stacktraces.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
            .forEach(e -> {
                println("Count: " + e.getValue() + ", Stacktrace: " + e.getKey());
            });
    }    

}
