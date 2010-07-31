package org.jdiscript.handlers;

import java.util.ArrayList;
import java.util.List;

import org.jdiscript.events.UnhandledEventException;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.MonitorContendedEnterEvent;
import com.sun.jdi.event.MonitorContendedEnteredEvent;
import com.sun.jdi.event.MonitorWaitEvent;
import com.sun.jdi.event.MonitorWaitedEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.MonitorContendedEnterRequest;
import com.sun.jdi.request.MonitorContendedEnteredRequest;
import com.sun.jdi.request.MonitorWaitRequest;
import com.sun.jdi.request.MonitorWaitedRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import com.sun.jdi.request.VMDeathRequest;

/**
 * Each handler method for requested events delegates to unhandledEvent(), 
 * which throws an UnhandledEventException.  Handler methods for unrequested
 * events do nothing.  
 * 
 * This class is intended to be subclassed.
 *  
 * @author jfager
 */
public class BaseEventHandler 
	implements OnAccessWatchpoint, OnBreakpoint, OnClassPrepare, 
	           OnClassUnload, OnEvent, OnException, OnLocatable, 
	           OnMethodEntry, OnMethodExit, OnModificationWatchpoint, 
	           OnMonitorContendedEnter, OnMonitorContendedEntered, 
	           OnMonitorWait, OnMonitorWaited, OnStep, OnThreadDeath, 
	           OnThreadStart, OnVMDeath, OnVMDisconnect, OnVMStart,
	           OnWatchpoint
{
	
	private VirtualMachine vm;
	private EventRequestManager erm;

	public void setVM(VirtualMachine vm) {
		this.vm = vm;
		this.erm = vm.eventRequestManager();
	}
	
	public VirtualMachine vm() {
		return vm;
	}
	
	public void notifySuspendPolicy(int suspendPolicy) {
		// do nothing
	}
	
	public void exec(AccessWatchpointEvent e) {
		unhandledEvent(e);
	}

	public void exec(BreakpointEvent e) {
		unhandledEvent(e);
	}

	public void exec(ClassPrepareEvent e) {
		unhandledEvent(e);
	}

	public void exec(ClassUnloadEvent e) {
		unhandledEvent(e);
	}
	
	public void exec(Event e) {
		unhandledEvent(e);
	}

	public void exec(ExceptionEvent e) {
		unhandledEvent(e);
	}
	
	public void exec(LocatableEvent e) {
		unhandledEvent(e);
	}

	public void exec(MethodEntryEvent e) {
		unhandledEvent(e);
	}

	public void exec(MethodExitEvent e) {
		unhandledEvent(e);
	}

	public void exec(ModificationWatchpointEvent e) {
		unhandledEvent(e);
	}

	public void exec(MonitorContendedEnterEvent e) {
		unhandledEvent(e);
	}

	public void exec(MonitorContendedEnteredEvent e){
		unhandledEvent(e);
	}

	public void exec(MonitorWaitEvent e) {
		unhandledEvent(e);
	}

	public void exec(MonitorWaitedEvent e) {
		unhandledEvent(e);
	}

	public void exec(StepEvent e) {
		unhandledEvent(e);
	}

	public void exec(ThreadDeathEvent e) {
		unhandledEvent(e);
	}

	public void exec(ThreadStartEvent e) {
		unhandledEvent(e);
	}

	public void exec(VMDeathEvent e) {
		//do nothing
	}

	public void exec(VMDisconnectEvent e) {
		//do nothing
	}

	public void exec(VMStartEvent e) {
		//do nothing
	}

	public void exec(WatchpointEvent e) {
		unhandledEvent(e);
	}
	
	public void unhandledEvent( Event e ) {
		throw new UnhandledEventException(e);
	}
}
