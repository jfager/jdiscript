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
	-cp ./target/classes/
"""
MAIN = "org.jdiscript.example.HelloWorld"

VirtualMachine vm = new VMLauncher(OPTIONS, MAIN).start()
JDIScript jdi = new JDIScript(vm)

Stack stack = new Stack<Location>()

start = {
	vm.allClasses().each { constructors it }
	jdi.classPrepareRequest()
	   .addHandler({ constructors it.referenceType() } as OnClassPrepare)
	   .enable()
} as OnVMStart

constructors = { ReferenceType refType ->
	refType.methodsByName('<init>').each {
		if(it.location().declaringType().name() != 'java.lang.Object') {
			def br = jdi.breakpointRequest(it.location())
						.addHandler(breakpoint)
			if(refType.name().startsWith('org.jdiscript')) {
				br.enable()
			}
		}
	}
}

breakpoint = {
	String prefix = '  ' * stack.size()
	ReferenceType refType = it.location().declaringType()
	println prefix + 'new ' + refType.name()

	stack.push it.location().method()
		
	jdi.breakpointRequests(breakpoint).each { it.enable() }
			
	jdi.methodExitRequest()
	   .addInstanceFilter( it.thread().frame(0).thisObject() )
	   .addHandler(methodExit)
	   .enable()
} as OnBreakpoint

methodExit = {
	Method targetMethod = stack.peek()
	Method eventMethod = it.method()
	
	if(targetMethod.equals(eventMethod)) {
		jdi.deleteEventRequest( it.request() )
		stack.pop()
		if(stack.isEmpty()) {
			jdi.breakpointRequests(breakpoint).each {
				if(!it.location().declaringType().name().startsWith('org.jdiscript')) {
					it.disable()
				}
			}
		}
	}
} as OnMethodExit
	
		
jdi.run(start) 


		
		