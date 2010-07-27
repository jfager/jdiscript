package org.jdiscript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
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
 * For each method of DebugEventHandler, throw an UnhandledEventException 
 * for events that must be requested but aren't implemented.  This class is
 * intended to be subclassed.
 *  
 * @author jfager
 */
public class BaseDebugEventHandler implements DebugEventHandler
{
	private static final Log log = LogFactory.getLog(BaseDebugEventHandler.class);
	
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
		throw new UnhandledEventException(e);
	}

	@Override
	public void breakpoint(BreakpointEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void classPrepare(ClassPrepareEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void classUnload(ClassUnloadEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void exception(ExceptionEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void locatable(LocatableEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void methodEntry(MethodEntryEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void methodExit(MethodExitEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void modificationWatchpoint(ModificationWatchpointEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void monitorContendedEnter(MonitorContendedEnterEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void monitorContendedEntered(MonitorContendedEnteredEvent e){
		throw new UnhandledEventException(e);
	}

	@Override
	public void monitorWait(MonitorWaitEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void monitorWaited(MonitorWaitedEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void step(StepEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void threadDeath(ThreadDeathEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void threadStart(ThreadStartEvent e) {
		throw new UnhandledEventException(e);
	}

	@Override
	public void vmDeath(VMDeathEvent e) {
		log.info("Received VMDeathEvent");
	}

	@Override
	public void vmDisconnect(VMDisconnectEvent e) {
		log.info("Received VMDisconnectEvent");
	}

	@Override
	public void vmStart(VMStartEvent e) {
		log.info("Received VMStartEvent");
	}

	@Override
	public void watchpoint(WatchpointEvent e) {
		throw new UnhandledEventException(e);
	}
}
