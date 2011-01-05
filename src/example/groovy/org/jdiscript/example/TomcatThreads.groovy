package org.jdiscript.example

import org.jdiscript.handlers.OnMonitorContendedEnter
import org.jdiscript.handlers.OnVMStart
import org.jdiscript.JDIScript

import com.sun.jdi.ObjectReference
import com.sun.jdi.ThreadReference
import com.sun.jdi.VirtualMachine

VirtualMachine vm = TomcatStarter.vm()
JDIScript j = new JDIScript(vm)

class ContentionTracker {
    int counter = 0
    Set locations = new HashSet()
    Set sourceNames = new HashSet()
    def threads = [:]
}

def contended = [:]

def monitorContendedEnter = {
    ThreadReference tref = it.thread()
    ObjectReference mref = it.monitor()

    ContentionTracker t = contended.get(mref.uniqueID(), new ContentionTracker())
    t.counter += 1
    t.locations.add(it.location())
    t.sourceNames.addAll(mref.referenceType().sourcePaths(null))

    String threadKey = tref.name() + tref.uniqueID()
    int threadCount = t.threads.get(threadKey, 0)
    t.threads[threadKey] = threadCount + 1
} as OnMonitorContendedEnter

j.monitorContendedEnterRequest().addHandler(monitorContendedEnter).enable()
j.run(10 * 1000)

println "Shutting down"
vm.process().destroy()

println "Contention info:"
contended.each { k,v ->
    println "MonitorID: ${k}, Hits: ${v.counter}, Locations: ${v.locations}"
    println "\tMonitor source: ${v.sourceNames}"
    println "\tThreads: ${v.threads}"
}

