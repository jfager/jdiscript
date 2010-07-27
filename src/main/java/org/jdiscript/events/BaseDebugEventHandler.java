package org.jdiscript.events;

import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
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
import com.sun.jdi.request.EventRequest;

/**
 * For each method of DebugEventHandler, delegate to unhandledEvent(), which
 * throws an UnhandledEventException.  This class is intended to be subclassed.
 *  
 * @author jfager
 */
public class BaseDebugEventHandler implements DebugEventHandler
{
	private int currentSuspendPolicy = EventRequest.SUSPEND_NONE;
	
	public int getSuspendPolicy() {
		return currentSuspendPolicy;
	}
		
	public void setSuspendPolicy(int suspendPolicy) {
		currentSuspendPolicy = suspendPolicy;
	}
	
	public void startEventSet() {} //do nothing
	public void endEventSet() {} //do nothing

	public boolean wantsEventSet() { return false; }	
	public void handleEventSet(EventSet es) {} //do nothing
	
	@Override
	public void accessWatchpoint(AccessWatchpointEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void breakpoint(BreakpointEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void classPrepare(ClassPrepareEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void classUnload(ClassUnloadEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void exception(ExceptionEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void locatable(LocatableEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void methodEntry(MethodEntryEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void methodExit(MethodExitEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void modificationWatchpoint(ModificationWatchpointEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void monitorContendedEnter(MonitorContendedEnterEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void monitorContendedEntered(MonitorContendedEnteredEvent e){
		unhandledEvent(e);
	}

	@Override
	public void monitorWait(MonitorWaitEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void monitorWaited(MonitorWaitedEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void step(StepEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void threadDeath(ThreadDeathEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void threadStart(ThreadStartEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void vmDeath(VMDeathEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void vmDisconnect(VMDisconnectEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void vmStart(VMStartEvent e) {
		unhandledEvent(e);
	}

	@Override
	public void watchpoint(WatchpointEvent e) {
		unhandledEvent(e);
	}
	
	@Override
	public void unhandledEvent( Event e ) {
		throw new UnhandledEventException(e);
	}
}
