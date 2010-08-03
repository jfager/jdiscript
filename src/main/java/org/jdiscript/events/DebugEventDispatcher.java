package org.jdiscript.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.DebugEventHandler;
import org.jdiscript.handlers.OnAccessWatchpoint;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnClassPrepare;
import org.jdiscript.handlers.OnClassUnload;
import org.jdiscript.handlers.OnEvent;
import org.jdiscript.handlers.OnException;
import org.jdiscript.handlers.OnLocatable;
import org.jdiscript.handlers.OnMethodEntry;
import org.jdiscript.handlers.OnMethodExit;
import org.jdiscript.handlers.OnModificationWatchpoint;
import org.jdiscript.handlers.OnMonitorContendedEnter;
import org.jdiscript.handlers.OnMonitorContendedEntered;
import org.jdiscript.handlers.OnMonitorWait;
import org.jdiscript.handlers.OnMonitorWaited;
import org.jdiscript.handlers.OnStep;
import org.jdiscript.handlers.OnThreadDeath;
import org.jdiscript.handlers.OnThreadStart;
import org.jdiscript.handlers.OnVMDeath;
import org.jdiscript.handlers.OnVMDisconnect;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.handlers.OnWatchpoint;

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
import com.sun.jdi.request.EventRequest;


public class DebugEventDispatcher {
	
	private List<DebugEventHandler> handlers 
		= new ArrayList<DebugEventHandler>();

	public void addHandler(DebugEventHandler handler) {
		handlers.add(handler);
	}
	
	public void addHandlers(Collection<DebugEventHandler> handlers) {
		this.handlers.addAll(handlers);
	}
	
	/**
	 * Dispatch incoming events
	 */
	public void dispatch(VirtualMachine vm, Event event, int suspendPolicy) {
		final EventRequest request = event.request();
		if (request == null) {
	    	for(DebugEventHandler handler: handlers) {
	    		//handler.notifySuspendPolicy(suspendPolicy);
	    		//VM Events
	    		if(event instanceof VMStartEvent && 
	    		   handler instanceof OnVMStart) {
		    		((OnVMStart)handler).exec((VMStartEvent)event);
		        } else if(event instanceof VMDisconnectEvent &&
		        		  handler instanceof OnVMDisconnect) {
		    		((OnVMDisconnect)handler).exec((VMDisconnectEvent)event);
		    	} else if(event instanceof VMDeathEvent &&
		    			  handler instanceof OnVMDeath) {
		    		((OnVMDeath)handler).exec((VMDeathEvent)event);
		    	} 
	    	}
	    	return;
		}
		
		final Set<DebugEventHandler> handlers = JDIScript.getHandlers(request);
		
		if(handlers == null) {
			throw new RuntimeException("No handlers specified for event " + event);
		}
		
		for(DebugEventHandler handler: handlers) {
	        if(event instanceof BreakpointEvent) {
	            ((OnBreakpoint)handler).exec((BreakpointEvent)event);
	        }
	
	        else if(event instanceof StepEvent) {
	            ((OnStep)handler).exec((StepEvent)event);
	        }
	
	        else if(event instanceof ExceptionEvent) {
	            ((OnException)handler).exec((ExceptionEvent)event);
	        }
	
	        //Method Events
	        else if(event instanceof MethodEntryEvent) {
	            ((OnMethodEntry)handler).exec((MethodEntryEvent)event);
	        } else if(event instanceof MethodExitEvent) {
	            ((OnMethodExit)handler).exec((MethodExitEvent)event);
	        }
	
	        //Monitor Events
	        else if(event instanceof MonitorWaitEvent) {
	            ((OnMonitorWait)handler).exec((MonitorWaitEvent)event);
	        } else if(event instanceof MonitorWaitedEvent) {
	            ((OnMonitorWaited)handler).exec((MonitorWaitedEvent)event);
	        } else if(event instanceof MonitorContendedEnterEvent) {
	            ((OnMonitorContendedEnter)handler).exec((MonitorContendedEnterEvent)event);
	        } else if(event instanceof MonitorContendedEnteredEvent) {
	            ((OnMonitorContendedEntered)handler).exec((MonitorContendedEnteredEvent)event);
	        }
	
	        //Watchpoint Events
	        else if(event instanceof AccessWatchpointEvent) {
	        	((OnAccessWatchpoint)handler).exec((AccessWatchpointEvent)event);
	        } else if(event instanceof ModificationWatchpointEvent) {
	            ((OnModificationWatchpoint)handler).exec((ModificationWatchpointEvent)event);
	        }
	
			//Threading Events
	        else if(event instanceof ThreadStartEvent) {
	            ((OnThreadStart)handler).exec((ThreadStartEvent)event);
	        } else if(event instanceof ThreadDeathEvent) {
	            ((OnThreadDeath)handler).exec((ThreadDeathEvent)event);
	        }
	
			//Class Events
	        else if(event instanceof ClassPrepareEvent) {
	            ((OnClassPrepare)handler).exec((ClassPrepareEvent)event);
	        } else if(event instanceof ClassUnloadEvent) {
	            ((OnClassUnload)handler).exec((ClassUnloadEvent)event);
	        }
	
			//VM Events
	        else if(event instanceof VMStartEvent) {
	            ((OnVMStart)handler).exec((VMStartEvent)event);
	        } else if(event instanceof VMDisconnectEvent) {
	            ((OnVMDisconnect)handler).exec((VMDisconnectEvent)event);
	        } else if(event instanceof VMDeathEvent) {
	            ((OnVMDeath)handler).exec((VMDeathEvent)event);
	        }
	
	        //Catchalls
	        else if(event instanceof WatchpointEvent) {
	            ((OnWatchpoint)handler).exec((WatchpointEvent)event);
	        } else if(event instanceof LocatableEvent) {
	            ((OnLocatable)handler).exec((LocatableEvent)event);
	        } else {
				((OnEvent)handler).exec(event);
			}
		}
	}
}
