package org.jdiscript

import org.jdiscript.handlers.OnBreakpoint 
import org.jdiscript.handlers.OnClassPrepare 
import org.jdiscript.handlers.OnMethodExit 
import org.jdiscript.handlers.OnVMStart 
import org.jdiscript.requests.ChainingBreakpointRequest 
import org.jdiscript.util.VMLauncher
import com.sun.jdi.Location 
import com.sun.jdi.Method 
import com.sun.jdi.ReferenceType 
import com.sun.jdi.VirtualMachine 

OPTIONS = """
	-Dlog4j.configuration=file:/Users/Jason/java_shite/log4j.properties
	-cp /Users/jason/projects/jdiscript/target/classes/
"""
MAIN = "org.jdiscript.example.HelloWorld"

VirtualMachine vm = new VMLauncher(OPTIONS, MAIN).start()
JDIScript jdi = new JDIScript(vm)

Stack stack = new Stack<Location>()
		
def methodExit = {
	Method targetMethod = stack.peek()
	Method eventMethod = it.method()
	
	if(targetMethod.equals(eventMethod)) {
		jdi.deleteEventRequest( it.request() )
		stack.pop()
		if(stack.isEmpty()) {
			jdi.breakpointRequests().each {
				if(!it.getProperty('refType').name().startsWith('org.jdiscript')) {
					it.disable()
				}
			}
		}
	}
} as OnMethodExit
	
def breakpoint = {
	String prefix = '  ' * stack.size()
	ReferenceType refType = it.request().getProperty('refType')
	println prefix + 'new ' + refType.name()

	stack.push it.location().method()
		
	jdi.breakpointRequests().each { it.enable() }
			
	jdi.methodExitRequest()
	   .putProperty('refType', refType)
	   .addInstanceFilter( it.thread().frame(0).thisObject() )
	   .addHandler(methodExit)
	   .enable()
} as OnBreakpoint
	
def instrumentConstructors = { ReferenceType refType ->
	refType.methodsByName('<init>').each {
		if(it.location().declaringType().name() != 'java.lang.Object') {
			def br = jdi.breakpointRequest(it.location())
			            .putProperty('refType', refType)
			if(refType.name().startsWith('org.jdiscript')) {
				br.addHandler(breakpoint).enable()
			}
		}
	}
}

def classPrepare = {
	instrumentConstructors it.referenceType()
} as OnClassPrepare
	
def onStart = {
	vm.allClasses().each { instrumentConstructors it }
	jdi.classPrepareRequest().addHandler(classPrepare).enable()
} as OnVMStart
		
jdi.run(onStart) 


		
		