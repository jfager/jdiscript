package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;
import static org.jdiscript.util.Utils.repeat;
import static org.jdiscript.util.Utils.unchecked;

import java.util.Stack;
import java.util.function.Consumer;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;

class ConstructorWatchExample {   
        
    String OPTIONS = "-cp ./build/classes/java/example/";
    String MAIN = "org.jdiscript.example.HelloWorld";
    
    JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());

    Stack<Method> stack = new Stack<>();
    
    //This references itself and so the compiler complains "Cannot 
    //reference a field before it is defined" if we try to initialize directly.
    OnBreakpoint breakpoint; { breakpoint = be -> {
        println(repeat("  ", stack.size()) + "new " + be.location().declaringType().name());
        stack.push(be.location().method());
        
        //We set breakpoints on every constructor but initially only
        //enabled them for ones we care about (those in the org.jdiscript 
        //package), so we're somewhere inside the constructor call chain 
        //of a constructor we care about. If we're in that initial constructor,
        //ensure that all of the other constructor breakpoints are enabled for 
        //the current thread, and then disabled when the original method call exits.
        //Otherwise, just pop the constructor call off the stack when its done.
        if(stack.size() == 1) {
            j.breakpointRequests(breakpoint).stream()
                .filter(bpr -> !bpr.isEnabled())
                .forEach(bpr -> {
                    bpr.addThreadFilter(be.thread());
                    bpr.enable();
                });
        
            //Make sure that when we leave the interesting method call, we turn
            //all of the constructor breakpoints back off.
            unchecked(() -> j.onCurrentMethodExit(be.thread(), e -> {
                stack.pop();
                if(stack.size() == 0) { 
                    j.breakpointRequests(breakpoint).forEach(bp ->
                        bp.setEnabled(bp.location().declaringType().name().startsWith("org.jdiscript"))
                    );
                }
            }));
        } else {
            unchecked(() -> j.onCurrentMethodExit(be.thread(), e -> stack.pop()));
        }
    };}

    OnVMStart start = se -> {
        Consumer<ReferenceType> setConstructBrks = rt -> rt.methodsByName("<init>").stream()
                .filter(m -> !m.location().declaringType().name().equals("java.lang.Object"))
                .forEach(m -> j.breakpointRequest(m.location(), breakpoint)
                               .setEnabled(rt.name().startsWith("org.jdiscript")));
        
        j.vm().allClasses().forEach(setConstructBrks);
        j.onClassPrep(cp -> setConstructBrks.accept(cp.referenceType()));
    };         

    public static void main(String[] args) {
        ConstructorWatchExample c = new ConstructorWatchExample();
        c.j.run(c.start);    
    }
}

