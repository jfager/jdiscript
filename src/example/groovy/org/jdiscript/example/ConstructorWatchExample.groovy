package org.jdiscript.example

import org.jdiscript.JDIScript
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
    jdi.classPrepareRequest({ constructors it.referenceType() } as OnClassPrepare)
       .enable()
} as OnVMStart

constructors = { ReferenceType refType ->
    refType.methodsByName('<init>').each {
        if(it.location().declaringType().name() != 'java.lang.Object') {
            def br = jdi.breakpointRequest(it.location(), breakpoint)
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
	currthread = it.thread()

	//We've set breakpoints on every constructor.  Now that we're inside
	//a method we care about, turn them all on for the current thread
	//so that they get tripped when called.
    jdi.breakpointRequests(breakpoint).each {
		if(!it.enabled) {
			it.addThreadFilter(currthread)
			it.enable()
		}
	}

	//Make sure that when we leave the current method call, we turn
	//all of the constructor breakpoints back off.
    jdi.methodExitRequest(methodExit)
       .addInstanceFilter( currthread.frame(0).thisObject() )
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

