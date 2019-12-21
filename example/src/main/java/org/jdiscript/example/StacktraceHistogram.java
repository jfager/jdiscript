package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;
import static org.jdiscript.util.Utils.unchecked;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

public class StacktraceHistogram {
    
    String OPTIONS = "-cp ./build/classes/java/example";
    String MAIN = "org.jdiscript.example.HelloWorld";
    
    JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());

    public String stacktraceKey(ThreadReference t) 
            throws IncompatibleThreadStateException {
        StringBuilder sb = new StringBuilder();
        for(StackFrame f: t.frames()) {
            Location loc = f.location(); 
            sb.append(loc.declaringType().name())
              .append(".").append(loc.method().name())
              .append(loc.lineNumber())
              .append(":");
        }
        return sb.substring(0, sb.length() - 1);
    }
    
    final Map<String, AtomicLong> stacktraces = new HashMap<>();

    OnBreakpoint breakpoint = be -> {
        ThreadReference tref = be.thread();
        unchecked(() -> {
            String trace = stacktraceKey(tref);
            stacktraces.computeIfAbsent(trace, k -> new AtomicLong(0))
                       .incrementAndGet();
        });
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
