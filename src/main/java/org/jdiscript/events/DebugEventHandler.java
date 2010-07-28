package org.jdiscript.events;

import java.util.ArrayList;
import java.util.List;

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
 * For each method of DebugEventHandler, delegate to unhandledEvent(), which
 * throws an UnhandledEventException.  This class is intended to be subclassed.
 *  
 * @author jfager
 */
public abstract class DebugEventHandler 
{
	public static final String PROP_KEY = "HANDLER";
	
	private final VirtualMachine vm;
	private final EventRequestManager erm;
	
	public DebugEventHandler(VirtualMachine vm) {
		this.vm = vm;
		this.erm = vm.eventRequestManager();
	}
	
	public VirtualMachine getVM() {
		return vm;
	}
	
	public void notifySuspendPolicy(int suspendPolicy) {
		// do nothing
	}
	
	public void accessWatchpoint(AccessWatchpointEvent e) {
		unhandledEvent(e);
	}

	public void breakpoint(BreakpointEvent e) {
		unhandledEvent(e);
	}

	public void classPrepare(ClassPrepareEvent e) {
		unhandledEvent(e);
	}

	public void classUnload(ClassUnloadEvent e) {
		unhandledEvent(e);
	}

	public void exception(ExceptionEvent e) {
		unhandledEvent(e);
	}

	public void locatable(LocatableEvent e) {
		unhandledEvent(e);
	}

	public void methodEntry(MethodEntryEvent e) {
		unhandledEvent(e);
	}

	public void methodExit(MethodExitEvent e) {
		unhandledEvent(e);
	}

	public void modificationWatchpoint(ModificationWatchpointEvent e) {
		unhandledEvent(e);
	}

	public void monitorContendedEnter(MonitorContendedEnterEvent e) {
		unhandledEvent(e);
	}

	public void monitorContendedEntered(MonitorContendedEnteredEvent e){
		unhandledEvent(e);
	}

	public void monitorWait(MonitorWaitEvent e) {
		unhandledEvent(e);
	}

	public void monitorWaited(MonitorWaitedEvent e) {
		unhandledEvent(e);
	}

	public void step(StepEvent e) {
		unhandledEvent(e);
	}

	public void threadDeath(ThreadDeathEvent e) {
		unhandledEvent(e);
	}

	public void threadStart(ThreadStartEvent e) {
		unhandledEvent(e);
	}

	public void vmDeath(VMDeathEvent e) {
		unhandledEvent(e);
	}

	public void vmDisconnect(VMDisconnectEvent e) {
		unhandledEvent(e);
	}

	public void vmStart(VMStartEvent e) {
		unhandledEvent(e);
	}

	public void watchpoint(WatchpointEvent e) {
		unhandledEvent(e);
	}
	
	public void unhandledEvent( Event e ) {
		throw new UnhandledEventException(e);
	}
	
	// Convenience methods for creating EventRequests, that will automatically
	// set the handler as a property so that the Dispatcher works correctly.
    public AccessWatchpointRequest accessWatchpointRequest(Field field) {
        AccessWatchpointRequest out = erm.createAccessWatchpointRequest(field);
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public BreakpointRequest breakpointRequest(Location location) {
        BreakpointRequest out = erm.createBreakpointRequest(location);
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public ClassPrepareRequest classPrepareRequest() {
        ClassPrepareRequest out = erm.createClassPrepareRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public ClassUnloadRequest classUnloadRequest() {
        ClassUnloadRequest out = erm.createClassUnloadRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }    
    
    public ExceptionRequest exceptionRequest(ReferenceType refType, 
    										 boolean notifyCaught, 
    										 boolean notifyUncaught) {
        ExceptionRequest out = erm.createExceptionRequest(refType, 
        									              notifyCaught, 
        											      notifyUncaught);
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public MethodEntryRequest methodEntryRequest() {
        MethodEntryRequest out = erm.createMethodEntryRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public MethodExitRequest methodExitRequest() {
        MethodExitRequest out = erm.createMethodExitRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public ModificationWatchpointRequest modificationWatchpointRequest(Field field) {
        ModificationWatchpointRequest out = erm.createModificationWatchpointRequest(field);
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public MonitorContendedEnteredRequest monitorContendedEnteredRequest() {
        MonitorContendedEnteredRequest out = erm.createMonitorContendedEnteredRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public MonitorContendedEnterRequest monitorContendedEnterRequest() {
        MonitorContendedEnterRequest out = erm.createMonitorContendedEnterRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public MonitorWaitedRequest monitorWaitedRequest() {
        MonitorWaitedRequest out = erm.createMonitorWaitedRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public MonitorWaitRequest monitorWaitRequest() {
        MonitorWaitRequest out = erm.createMonitorWaitRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public StepRequest stepRequest(ThreadReference thread, int size, int depth) {
        StepRequest out = erm.createStepRequest(thread, size, depth);
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public ThreadDeathRequest threadDeathRequest() {
        ThreadDeathRequest out = erm.createThreadDeathRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public ThreadStartRequest threadStartRequest() {
        ThreadStartRequest out = erm.createThreadStartRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    public VMDeathRequest vmDeathRequest() {
        VMDeathRequest out = erm.createVMDeathRequest();
        out.putProperty(PROP_KEY, this);
        return out;
    }
    
    // Convenience method for accessing only those EventRequests that are 
    // associated with this handler

    public List<AccessWatchpointRequest> accessWatchpointRequests() {
        return filter(erm.accessWatchpointRequests());
    }
    
    public List<BreakpointRequest> breakpointRequests() {
        return filter(erm.breakpointRequests());
    }
    
    public List<ClassPrepareRequest> classPrepareRequests() {
        return filter(erm.classPrepareRequests());
    }
    
    public List<ClassUnloadRequest> classUnloadRequests() {
        return filter(erm.classUnloadRequests());
    }
    
    public List<ExceptionRequest> exceptionRequests() {
        return filter(erm.exceptionRequests());
    }
    
    public List<MethodEntryRequest> methodEntryRequests() {
        return filter(erm.methodEntryRequests());
    }
    
    public List<MethodExitRequest> methodExitRequests() {
        return filter(erm.methodExitRequests());
    }
    
    public List<ModificationWatchpointRequest> modificationWatchpointRequests() {
        return filter(erm.modificationWatchpointRequests());
    }
    
    public List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests() {
        return filter(erm.monitorContendedEnteredRequests());
    }
    
    public List<MonitorContendedEnterRequest> monitorContendedEnterRequests() {
        return filter(erm.monitorContendedEnterRequests());
    }
    
    public List<MonitorWaitedRequest> monitorWaitedRequests() {
        return filter(erm.monitorWaitedRequests());
    }
    
    public List<MonitorWaitRequest> monitorWaitRequests() {
        return filter(erm.monitorWaitRequests());
    }
    
    public List<StepRequest> stepRequests() {
        return filter(erm.stepRequests());
    }
    
    public List<ThreadDeathRequest> threadDeathRequests() {
        return filter(erm.threadDeathRequests());
    }
    
    public List<ThreadStartRequest> threadStartRequests() {
        return filter(erm.threadStartRequests());
    }
    
    public List<VMDeathRequest> vmDeathRequests() {
        return filter(erm.vmDeathRequests());
    }

    public void deleteEventRequest(EventRequest eventRequest) {
    	erm.deleteEventRequest(eventRequest);
    }
    
    public void deleteEventRequests(List<? extends EventRequest> eventRequests) {
    	erm.deleteEventRequests(eventRequests);
    }

    private <T extends EventRequest> List<T> filter(List<T> ers) {
    	List<T> out = new ArrayList<T>();
    	for(T er: ers) {
    		if(er.getProperty(PROP_KEY) == this) {
    			out.add(er);
    		}
    	}
    	return out;
    }
}
