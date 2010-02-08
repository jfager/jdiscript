package com.jasonfager.debug;

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

public interface DebugEventHandler {
	/**
	 * Called by EventThread before an EventSet is processed.
	 */
	void startEventSet();
	
	/**
	 * Called by EventThread right after startEventSet()
	 * 
	 * @param suspendPolicy The result of EventSet.suspendPolicy()
	 */
	void setSuspendPolicy(int suspendPolicy);
	
	/**
	 * Called by EventThread after an EventSet is processed and before 
	 * EventSet.resume() is called.
	 */
	void endEventSet();
	
	/**
	 * If this returns true when called by EventThread, EventThread will call
	 * handleEventSet rather than specific event handlers.
	 */
	boolean wantsEventSet();
	
	/**
	 * Called by EventThread when wantsEventSet() returns true
	 * @param es
	 */
	void handleEventSet(EventSet es);
	
    void accessWatchpoint( AccessWatchpointEvent e );
    void breakpoint( BreakpointEvent e );
    void classPrepare( ClassPrepareEvent e );
    void classUnload( ClassUnloadEvent e );
    void exception( ExceptionEvent e );
    void locatable( LocatableEvent e );
    void methodEntry( MethodEntryEvent e );
    void methodExit( MethodExitEvent e );
    void modificationWatchpoint( ModificationWatchpointEvent e );
    void monitorContendedEntered( MonitorContendedEnteredEvent e );
    void monitorContendedEnter( MonitorContendedEnterEvent e );
    void monitorWaited( MonitorWaitedEvent e );
    void monitorWait( MonitorWaitEvent e );
    void step( StepEvent e );
    void threadDeath( ThreadDeathEvent e );
    void threadStart( ThreadStartEvent e );
    void vmDeath( VMDeathEvent e );
    void vmDisconnect( VMDisconnectEvent e );
    void vmStart( VMStartEvent e );
    void watchpoint( WatchpointEvent e );
}
