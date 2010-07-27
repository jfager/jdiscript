
package org.jdiscript

import com.jasonfager.debug.util.DebugRunner;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.MonitorContendedEnterEvent;
import com.sun.jdi.event.VMStartEvent;

public class TomcatThreads extends BaseDebugEventHandler {
	
	def contended = [:] 
	
	void vmStart( VMStartEvent e ) {
		def erm = e.virtualMachine().eventRequestManager()
		erm.createMonitorContendedEnterRequest().enable()
	}
	
	void monitorContendedEnter( MonitorContendedEnterEvent e ) {
		ThreadReference tref = e.thread()
		ObjectReference mref = e.monitor()
		
		ContentionTracker t = contended.get(mref.uniqueID(), new ContentionTracker())
		t.counter += 1
		t.locations.add(e.location())
		t.sourceNames.addAll(mref.referenceType().sourcePaths(null))
		
		String threadKey = tref.name() + tref.uniqueID()
		int threadCount = t.threads.get(threadKey, 0)
		t.threads[threadKey] = threadCount + 1
	}
	
	class ContentionTracker {
		int counter = 0
		Set locations = new HashSet()
		Set sourceNames = new HashSet()
		def threads = [:]
	}
	
	static void main(args) {
		TomcatThreads tt = new TomcatThreads()
		VirtualMachine vm = TomcatStarter.vm() 
		new DebugRunner(vm, tt).run(10 * 1000)
		
		println "Shutting down"
		vm.process().destroy()
		println "Contention info:"
		tt.contended.each { k,v ->
			println "${k} @ ${v.locations} : ${v.counter} "
			println "\tMonitor source: ${v.sourceNames}"
			println "\tThreads: ${v.threads}"
		}
	}
}