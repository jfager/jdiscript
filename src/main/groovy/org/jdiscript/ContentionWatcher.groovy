package org.jdiscript

import org.jdiscript.handlers.*
import org.jdiscript.util.VMSocketAttacher 
import com.sun.jdi.*

VirtualMachine vm = new VMSocketAttacher(12345).attach()
JDIScript j = new JDIScript(vm)

def contendedEnter = {
	long timestamp = System.currentTimeMillis()
	println "${timestamp}: Contended enter for ${it.monitor()} by ${it.thread()}"
	it.thread().frames().each { println "   " + it }
} as OnMonitorContendedEnter

def contendedEntered = {
	long timestamp = System.currentTimeMillis()
	println "${timestamp}: Contended entered for ${it.monitor()} by ${it.thread()}"
} as OnMonitorContendedEntered

def start = {
	println "Got StartEvent"
} as OnVMStart

j.monitorContendedEnterRequest(contendedEnter).enable()
j.monitorContendedEnteredRequest(contendedEntered).enable()
j.run(start)
	
println "Shutting down"


