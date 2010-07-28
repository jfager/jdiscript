package org.jdiscript.util;

import java.io.OutputStream;

import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.events.DebugEventHandler;
import org.jdiscript.events.EventThread;

import com.sun.jdi.VirtualMachine;

public class DebugRunner {
	
	private final VirtualMachine vm;
	private final DebugEventDispatcher dispatcher;
	
	private final OutputStream out;
	private final OutputStream err;
	
	public DebugRunner(VirtualMachine vm) {
		this(vm, System.out, System.err);
	}
	
	public DebugRunner(VirtualMachine vm, OutputStream out, OutputStream err) {
		this.vm = vm;
		this.out = out;
		this.err = err;
		this.dispatcher = new DebugEventDispatcher();
	}
	
	public void addHandler(DebugEventHandler handler) {
		dispatcher.addHandler(handler);
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
	
	private void redirectOutput() {
		Process process = vm.process();

		if(out != null) {
			Thread outThread = new StreamRedirectThread("output reader",
														process.getInputStream(),
														out);
			outThread.setDaemon(true);
			outThread.start();
		}
		
		if(err != null) {
			Thread errThread = new StreamRedirectThread("error reader",
														process.getErrorStream(),
														err);
			errThread.setDaemon(true);
			errThread.start();
		}
	}
}
