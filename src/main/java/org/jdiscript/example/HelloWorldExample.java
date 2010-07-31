package org.jdiscript.example;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnAccessWatchpoint;
import org.jdiscript.handlers.OnClassPrepare;
import org.jdiscript.handlers.OnStep;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.StepRequest;

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
		j.run(new OnVMStart() {
			public void exec(VMStartEvent e) {
				j.classPrepareRequest()
				 .addClassFilter("org.jdiscript.example.HelloWorld")
				 .addHandler(new OnClassPrepare() {
					 public void exec(ClassPrepareEvent e) {
						 Field field = e.referenceType().fieldByName("helloTo");
						 j.accessWatchpointRequest(field)
						  .addHandler(new OnAccessWatchpoint() {
							  public void exec(AccessWatchpointEvent e) {
								  j.stepRequest(e.thread(), 
								            	StepRequest.STEP_MIN, 
								            	StepRequest.STEP_OVER)
								   .putProperty("field", e.field())
								   .putProperty("object", e.object())
								   .addHandler(new OnStep() {
									   public void exec(StepEvent se) {
										   StringReference alttobe = se.virtualMachine().mirrorOf("Groovy");
										   StepRequest req = (StepRequest)se.request();
										   req.disable();
										   try {
											   ((ObjectReference)req.getProperty("object")).setValue(
													   (Field)req.getProperty("field"), alttobe);
										   } catch(Exception e) {
											   throw new RuntimeException(e);
										   }
									   }
								   }).enable();
							  }
						  }).enable();
					 }
				 }).enable();
			}
		});
	}	

}
