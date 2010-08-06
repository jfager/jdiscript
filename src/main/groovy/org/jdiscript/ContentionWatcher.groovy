package org.jdiscript

import org.jdiscript.handlers.*
import org.jdiscript.util.VMSocketAttacher 
import com.sun.jdi.*

VirtualMachine vm = new VMSocketAttacher(12345).attach()
JDIScript j = new JDIScript(vm)

j.monitorContendedEnterRequest({
	long timestamp = System.currentTimeMillis()
	println "${timestamp}: Contended enter for ${it.monitor()} by ${it.thread()}"
	it.thread().frames().each { println "   " + it }
} as OnMonitorContendedEnter).enable()

j.monitorContendedEnteredRequest({
	long timestamp = System.currentTimeMillis()
	println "${timestamp}: Contended entered for ${it.monitor()} by ${it.thread()}"
} as OnMonitorContendedEntered).enable()

j.run({ println "Got StartEvent" } as OnVMStart)
	
println "Shutting down"


