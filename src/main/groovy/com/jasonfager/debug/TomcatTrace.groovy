package com.jasonfager.debug

import com.jasonfager.debug.util.DebugRunner
import com.jasonfager.debug.example.TraceExampleHandler
import com.sun.jdi.VirtualMachine

public class TomcatTrace {
	
	static void main(args) {
		VirtualMachine vm = TomcatStarter.vm() 
		new DebugRunner(vm, new TraceExampleHandler(vm)).run(10 * 1000)
		
		println "Shutting down"
		vm.process().destroy()
	}
}