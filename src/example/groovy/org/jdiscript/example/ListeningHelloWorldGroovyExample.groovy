package org.jdiscript.example

import org.jdiscript.JDIScript
import org.jdiscript.handlers.OnAccessWatchpoint
import org.jdiscript.handlers.OnStep
import org.jdiscript.util.VMListener

import com.sun.jdi.StringReference
import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.StepRequest

// The HelloWorld java example code prints out "Hello, <someone>" twice
// for 3 different values of <someone>. This script changes the second
// printout for each to "Hello, Groovy".
String OPTIONS = """
    -cp build/jdiscript.jar
"""
String MAIN = "org.jdiscript.example.HelloWorld"
VMListener listener = new VMListener(12345)
String connectStr = listener.start()
println "Client should connect to ${connectStr}"

while(true) {
	VirtualMachine vm = listener.next()
	println("Got connection")

	JDIScript j = new JDIScript(vm);
	StringReference alt = j.vm().mirrorOf("Groovy")

	j.onFieldAccess("org.jdiscript.example.HelloWorld", "helloTo", {
		def obj = it.object()
		def field = it.field()
		j.onStepInto(it.thread(), j.once({ obj.setValue(field, alt) } as OnStep))
	} as OnAccessWatchpoint)

	j.run()
}




