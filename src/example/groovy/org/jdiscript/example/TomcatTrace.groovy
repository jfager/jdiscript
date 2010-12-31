package org.jdiscript

import org.jdiscript.example.TraceExampleHandler
import com.sun.jdi.VirtualMachine

VirtualMachine vm = TomcatStarter.vm()
JDIScript jdi = new JDIScript(vm)
jdi.run(new TraceExampleHandler(jdi), 10 * 1000)

println "Shutting down"
vm.process().destroy()

