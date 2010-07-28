package org.jdiscript.util;

import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.events.EventThread;

import com.sun.jdi.VirtualMachine;

public class DebugRunner {
	private Thread errThread;
	private Thread outThread;
	
	private final VirtualMachine vm;
	private final DebugEventDispatcher dispatcher;
	
	public DebugRunner(VirtualMachine vm, DebugEventDispatcher dispatcher) {
		this.vm = vm;
		this.dispatcher = dispatcher;
	}
	
	public void run() {
		run(0);
	}
	
	public void run(long millis) {
		redirectOutput();
		EventThread eventThread = new EventThread(vm, dispatcher);
		eventThread.start();
		
		try {
			eventThread.join(millis);
		} catch(InterruptedException exc) {}		
	}
	
	void redirectOutput() {
		Process process = vm.process();
		errThread = new StreamRedirectThread(	"error reader",
												process.getErrorStream(),
												System.err);
		outThread = new StreamRedirectThread(	"output reader",
												process.getInputStream(),
												System.out);
		errThread.setDaemon(true);
		errThread.start();
		outThread.setDaemon(true);
		outThread.start();
	}
}
