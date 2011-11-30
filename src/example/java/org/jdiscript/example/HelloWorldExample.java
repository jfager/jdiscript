package org.jdiscript.example;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnAccessWatchpoint;
import org.jdiscript.handlers.OnStep;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.StepEvent;

public class HelloWorldExample {
    public static void main(final String[] args) {
        String OPTIONS = "-cp /Users/jason/projects/jdiscript/target/classes/";
        String MAIN = "org.jdiscript.example.HelloWorld";

        VirtualMachine vm = null;
        try {
            vm = new VMLauncher(OPTIONS, MAIN).start();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        final JDIScript j = new JDIScript(vm);
        
        final StringReference alt = j.vm().mirrorOf("JDIScript!");
        
        j.onFieldAccess("org.jdiscript.example.HelloWorld", "helloTo",
        	new OnAccessWatchpoint() {
        		public void accessWatchpoint(AccessWatchpointEvent e) {
        			final ObjectReference obj = e.object();
        			final Field field = e.field();
        			j.onStepInto(e.thread(), j.once(new OnStep() {
        				public void step(StepEvent e) {
        					try {
        						obj.setValue(field, alt);
        					} catch(Exception ex) {
        						throw new RuntimeException(ex);
        					}
        				}
        			}));
        		}
        	});

        j.run();
    }

}
