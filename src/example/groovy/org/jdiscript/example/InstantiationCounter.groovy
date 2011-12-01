package org.jdiscript.example

import org.jdiscript.JDIScript
import org.jdiscript.handlers.OnBreakpoint
import org.jdiscript.handlers.OnClassPrepare
import org.jdiscript.handlers.OnMethodExit
import org.jdiscript.handlers.OnVMStart
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.Location
import com.sun.jdi.Method
import com.sun.jdi.ReferenceType
import com.sun.jdi.VirtualMachine

OPTIONS = """
	-cp build/jdiscript.jar
"""
MAIN = "org.jdiscript.example.HelloWorld"

VirtualMachine vm = new VMLauncher(OPTIONS, MAIN).start()
JDIScript j = new JDIScript(vm)

Stack stack = new Stack<Location>()
class Node {
	Node parent
	int count
	Map<String, Node> children = [:].withDefault { new Node() }
}

root = new Node()
curr = root

start = {
	vm.allClasses().each { constructors it }
	j.classPrepareRequest({ constructors it.referenceType() } as OnClassPrepare)
	 .enable()
} as OnVMStart

constructors = { ReferenceType refType ->
	refType.methodsByName('<init>').each {
		if(it.location().declaringType().name() != 'java.lang.Object') {
			def br = j.breakpointRequest(it.location(), breakpoint)
			if(refType.name().startsWith('org.jdiscript')) {
				br.enable()
			}
		}
	}
}

breakpoint = {
	ReferenceType refType = it.location().declaringType()
	Method method = it.location().method()
	nxt = curr.children[j.fullName(method)]
	nxt.count += 1
	nxt.parent = curr
	curr = nxt
	stack.push method

	j.breakpointRequests(breakpoint).each { it.enable() }

	j.methodExitRequest(methodExit)
	 .addInstanceFilter( it.thread().frame(0).thisObject() )
	 .enable()
} as OnBreakpoint

methodExit = {
	Method targetMethod = stack.peek()
	Method eventMethod = it.method()

	if(targetMethod.equals(eventMethod)) {
		j.deleteEventRequest( it.request() )
		stack.pop()
		curr = curr.parent
		if(stack.isEmpty()) {
			j.breakpointRequests(breakpoint).each {
				if(!it.location().declaringType().name().startsWith('org.jdiscript')) {
					it.disable()
				}
			}
		}
	}
} as OnMethodExit


j.run(start)

printNode = { Node node, int indent ->
	node.children.each { k, v ->
		println(('  ' * indent) + k + ': ' + v.count)
		printNode(v, indent+1)
	}
}

printNode(root, 0)


