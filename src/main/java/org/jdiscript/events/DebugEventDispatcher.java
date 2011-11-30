package org.jdiscript.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

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

    public static final String PROP_KEY = "JDISCRIPT_HANDLER";

    /**
     * Get the handlers associated with the given EventRequest.
     */
    @SuppressWarnings("unchecked")
    public static Set<DebugEventHandler> getHandlers(EventRequest er) {
        return (Set<DebugEventHandler>)(er.getProperty(PROP_KEY));
    }

    /**
     * Attach a DebugEventHandler to an EventRequest.
     * <p>
     * Using this method ensures that a DebugEventDispatcher will be able to
     * use to dispatch an event for the request to the given handler.
     */
    public static boolean addHandler(EventRequest er, DebugEventHandler handler) {
        Set<DebugEventHandler> handlers = getHandlers(er);
        if(handlers == null) {
            handlers = new HashSet<DebugEventHandler>();
            er.putProperty(PROP_KEY, handlers);
        }
        return handlers.add(handler);
    }

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
    public void dispatch(final Event event) {
        final EventRequest request = event.request();
        if (request == null) {
            for(DebugEventHandler handler: handlers) {
                doVMEventDispatch(event, handler);
            }
            return;
        }

        final Set<DebugEventHandler> requestHandlers = getHandlers(request);

        if(requestHandlers == null) {
            throw new RuntimeException("No request handlers specified for event "
                                       + event);
        }

        for(DebugEventHandler handler: requestHandlers) {
            doFullDispatch(event, handler);
        }
    }

    // Events that have no corresponding request must be one of a smaller set
    // of VM events, handled here.
    private void doVMEventDispatch(Event event, DebugEventHandler handler) {
        if(event instanceof VMStartEvent &&
           handler instanceof OnVMStart) {
            ((OnVMStart)handler).vmStart((VMStartEvent)event);
        } else if(event instanceof VMDisconnectEvent &&
                  handler instanceof OnVMDisconnect) {
            ((OnVMDisconnect)handler).vmDisconnect((VMDisconnectEvent)event);
        } else if(event instanceof VMDeathEvent &&
                  handler instanceof OnVMDeath) {
            ((OnVMDeath)handler).vmDeath((VMDeathEvent)event);
        }
    }

    // Dispatching for the full set of possible events.
    public static void doFullDispatch(Event event, DebugEventHandler handler) {
        if(event instanceof BreakpointEvent &&
           handler instanceof OnBreakpoint) {
            ((OnBreakpoint)handler).breakpoint((BreakpointEvent)event);
        }

        else if(event instanceof StepEvent &&
                handler instanceof OnStep) {
            ((OnStep)handler).step((StepEvent)event);
        }

        else if(event instanceof ExceptionEvent &&
                handler instanceof OnException) {
            ((OnException)handler).exception((ExceptionEvent)event);
        }

        //Method Events
        else if(event instanceof MethodEntryEvent &&
                handler instanceof OnMethodEntry) {
            ((OnMethodEntry)handler).methodEntry((MethodEntryEvent)event);
        } else if(event instanceof MethodExitEvent &&
                  handler instanceof OnMethodExit) {
            ((OnMethodExit)handler).methodExit((MethodExitEvent)event);
        }

        //Monitor Events
        else if(event instanceof MonitorWaitEvent &&
                handler instanceof OnMonitorWait) {
            ((OnMonitorWait)handler).monitorWait((MonitorWaitEvent)event);
        } else if(event instanceof MonitorWaitedEvent &&
                  handler instanceof OnMonitorWaited) {
            ((OnMonitorWaited)handler).monitorWaited((MonitorWaitedEvent)event);
        } else if(event instanceof MonitorContendedEnterEvent &&
                  handler instanceof OnMonitorContendedEnter) {
            ((OnMonitorContendedEnter)handler).monitorContendedEnter(
                (MonitorContendedEnterEvent)event);
        } else if(event instanceof MonitorContendedEnteredEvent &&
                  handler instanceof OnMonitorContendedEntered) {
            ((OnMonitorContendedEntered)handler).monitorContendedEntered(
                (MonitorContendedEnteredEvent)event);
        }

        //Watchpoint Events
        else if(event instanceof AccessWatchpointEvent &&
                handler instanceof OnAccessWatchpoint) {
            ((OnAccessWatchpoint)handler).accessWatchpoint(
                (AccessWatchpointEvent)event);
        } else if(event instanceof ModificationWatchpointEvent &&
                  handler instanceof OnModificationWatchpoint) {
            ((OnModificationWatchpoint)handler).modificationWatchpoint(
                (ModificationWatchpointEvent)event);
        }

        //Threading Events
        else if(event instanceof ThreadStartEvent &&
                handler instanceof OnThreadStart) {
            ((OnThreadStart)handler).threadStart((ThreadStartEvent)event);
        } else if(event instanceof ThreadDeathEvent &&
                  handler instanceof OnThreadDeath) {
            ((OnThreadDeath)handler).threadDeath((ThreadDeathEvent)event);
        }

        //Class Events
        else if(event instanceof ClassPrepareEvent &&
                handler instanceof OnClassPrepare) {
            ((OnClassPrepare)handler).classPrepare((ClassPrepareEvent)event);
        } else if(event instanceof ClassUnloadEvent &&
                  handler instanceof OnClassUnload) {
            ((OnClassUnload)handler).classUnload((ClassUnloadEvent)event);
        }

        //VM Events
        else if(event instanceof VMStartEvent &&
                handler instanceof OnVMStart) {
            ((OnVMStart)handler).vmStart((VMStartEvent)event);
        } else if(event instanceof VMDisconnectEvent &&
                  handler instanceof OnVMDisconnect) {
            ((OnVMDisconnect)handler).vmDisconnect((VMDisconnectEvent)event);
        } else if(event instanceof VMDeathEvent &&
                  handler instanceof OnVMDeath) {
            ((OnVMDeath)handler).vmDeath((VMDeathEvent)event);
        }

        //Catchalls
        else if(event instanceof WatchpointEvent &&
                handler instanceof OnWatchpoint) {
            ((OnWatchpoint)handler).watchpoint((WatchpointEvent)event);
        } else if(event instanceof LocatableEvent &&
                  handler instanceof OnLocatable) {
            ((OnLocatable)handler).locatable((LocatableEvent)event);
        } else {
            ((OnEvent)handler).event(event);
        }
    }
}
