package org.jdiscript;

import org.jdiscript.handlers.OnAccessWatchpoint 
import org.jdiscript.handlers.OnClassPrepare 
import org.jdiscript.handlers.OnStep 
import org.jdiscript.handlers.OnVMStart 

import org.jdiscript.util.VMLauncher;
import com.sun.jdi.StringReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest 

String OPTIONS = """
	-cp ./target/classes/
"""
String MAIN = "org.jdiscript.example.HelloWorld"
VirtualMachine vm = new VMLauncher(OPTIONS, MAIN).start() 

JDIScript j = new JDIScript(vm);
j.run({
	j.classPrepareRequest()
	 .addClassFilter("org.jdiscript.example.HelloWorld")
	 .addHandler({
		 def field = it.referenceType().fieldByName("helloTo")
		 j.accessWatchpointRequest(field)
		  .addHandler({
			  def obj = it.object()
			  j.stepRequest(it.thread(), 
				            StepRequest.STEP_MIN, 
							StepRequest.STEP_OVER)
			   .addHandler({	
				   StringReference alttobe = it.virtualMachine().mirrorOf("Groovy")
				   it.request().disable()
				   obj.setValue(field, alttobe)
				} as OnStep).enable()
		  } as OnAccessWatchpoint).enable()
	 } as OnClassPrepare).enable()
} as OnVMStart)




