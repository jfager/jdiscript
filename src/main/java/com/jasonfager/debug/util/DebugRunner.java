package com.jasonfager.debug.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jasonfager.debug.DebugEventHandler;
import com.jasonfager.debug.EventThread;
import com.sun.jdi.VirtualMachine;

public class DebugRunner {
	private static final Log log = LogFactory.getLog(DebugRunner.class);
	private Thread errThread;
	private Thread outThread;
	
	private final VirtualMachine vm;
	private final DebugEventHandler handler;
	
	public DebugRunner(VirtualMachine vm, DebugEventHandler handler) {
		this.vm = vm;
		this.handler = handler;
	}
	
	public void run() {
		run(0);
	}
	
	public void run(long millis) {
		redirectOutput();
		EventThread eventThread = new EventThread(vm, handler);
		eventThread.start();
		
		try {
			eventThread.join(millis);
			log.info("DebugRunner complete");
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
