package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;
import static org.jdiscript.util.Utils.unchecked;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

/**
 * One of the easiest ways to leech performance is by creating Strings 
 * inefficiently. One example of this is creating multiple instances of the 
 * same String, which you might do either because you're dealing with the
 * same input frequently (so caching might make sense) or because you're 
 * creating a larger String from parts that get generated repeatedly (so
 * switching to StringBuilder might make sense).  
 * 
 * A profiler can easily show you that you're spending a lot of time building 
 * Strings, but it can't show you what those Strings are.  A heap dump can show 
 * you Strings that are resident in memory, but it's only a snapshot and may 
 * miss Strings that are created and then quickly garbage collected, plus you
 * still have to write a script to analyze the heap dump. 
 * 
 * This example demonstrates how you can use jdiscript to track the counts
 * and partial call path for all the distinct Strings your program creates.
 * It also demonstrates a technique for limiting tracking scope to calls 
 * from packages you control. 
 */
class StringReporter {   
    
    String OPTIONS = "-cp ./target/classes/";
    String MAIN = "org.jdiscript.example.HelloWorld";
    
    JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());
    BiFunction<Integer, Integer, Integer> add = (v1,v2) -> v1+v2;
        
    public Location nearestCaller(String pkg, ThreadReference t) 
            throws IncompatibleThreadStateException {
        for(StackFrame f: t.frames()) {
            Location loc = f.location(); 
            if(loc.declaringType().name().startsWith(pkg)) {
                return loc;
            }
        }
        return null;
    }
    
    class StringStats {
        int counter = 0;
        Map<String, Integer> callers = new HashMap<>();
    }

    final Map<String, StringStats> strings = new HashMap<>();
    final Set<Long> seenIds = new HashSet<>();

    OnBreakpoint breakpoint; { breakpoint = be -> {
        ThreadReference tref = be.thread();
        unchecked(() -> {
            Location pkgCaller = nearestCaller("org.jdiscript", tref);
            if(pkgCaller == null) {
                return;
            }
            final ObjectReference oref = tref.frame(0).thisObject();
            //Some String constructors just delegate to another constructor. We don't 
            //want to count that as two separate instances of the same String, so we 
            //check that the ObjectRef's unique id hasn't been seen yet.
            if(seenIds.add(oref.uniqueID())) {
                //We broke on the String constructor, so the actual contents of 
                //the String aren't set yet (if we called oref.toString() right now 
                //we'd just get "").  We defer the stat collecting to the 
                //constructor's exit so that we'll get the right value.  
                //
                //A more efficient alternative might be to just set the initial 
                //breakpoint on the constructor's exit, but in the case of delegating
                //constructors, that means the inner constructor would be the one 
                //that 'wins', making the stack trace less informative. Tradeoffs.
                j.onCurrentMethodExit(tref, ee -> {
                    StringStats stats = strings.computeIfAbsent(oref.toString(),
                                                                k -> new StringStats());
                    unchecked(() -> {
                        stats.counter += 1;
                        stats.callers.merge(
                            pkgCaller.method()+"..."+tref.frame(1).location().method(),
                            1, add);
                    });
                });
            } 
        });
    };}

    OnVMStart start = se -> {
        j.vm().classesByName("java.lang.String").forEach(rt -> 
            rt.methodsByName("<init>").forEach(m -> 
                j.breakpointRequest(m.location(), breakpoint).enable()
            )
        );
    };         

    public static void main(String[] args) {
        StringReporter c = new StringReporter();
        c.j.run(c.start);
        
        println("Strings:");
        c.strings.forEach((k,v) -> {
            println("String: "+k+", Times created: "+v.counter);
            println("\tCallers: "+v.callers);
        });
    }
}

