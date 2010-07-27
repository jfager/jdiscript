package org.jdiscript

import org.jdiscript.util.DebugRunner
import org.jdiscript.example.TraceExampleHandler
import com.sun.jdi.VirtualMachine

public class TomcatTrace {
	
	static void main(args) {
		VirtualMachine vm = TomcatStarter.vm() 
		new DebugRunner(vm, new TraceExampleHandler(vm)).run(10 * 1000)
		
		println "Shutting down"
		vm.process().destroy()
	}
}