package org.jdiscript

import org.jdiscript.handlers.*
import org.jdiscript.util.VMSocketAttacher
import com.sun.jdi.*

VirtualMachine vm = new VMSocketAttacher(12345).attach()
JDIScript j = new JDIScript(vm)

j.monitorContendedEnterRequest({
    j.printTrace(it, "ContendedEnter for ${it.monitor()}")
} as OnMonitorContendedEnter).enable()

j.monitorContendedEnteredRequest({
    long timestamp = System.currentTimeMillis()
    println "${timestamp}: ${it.thread()}: ContendedEntered for ${it.monitor()}"
} as OnMonitorContendedEntered).enable()

j.run({ println "Got StartEvent" } as OnVMStart)

println "Shutting down"


