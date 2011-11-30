package org.jdiscript.example

import org.jdiscript.JDIScript

import com.sun.jdi.VirtualMachine

VirtualMachine vm = TomcatStarter.vm()
JDIScript jdi = new JDIScript(vm)
jdi.run(new TraceExampleHandler(jdi), 10 * 1000)

println "Shutting down"
vm.process().destroy()

