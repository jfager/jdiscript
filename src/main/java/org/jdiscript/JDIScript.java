package org.jdiscript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.events.EventThread;
import org.jdiscript.handlers.DebugEventHandler;
import org.jdiscript.requests.ChainingAccessWatchpointRequest;
import org.jdiscript.requests.ChainingBreakpointRequest;
import org.jdiscript.requests.ChainingClassPrepareRequest;
import org.jdiscript.requests.ChainingClassUnloadRequest;
import org.jdiscript.requests.ChainingExceptionRequest;
import org.jdiscript.requests.ChainingMethodEntryRequest;
import org.jdiscript.requests.ChainingMethodExitRequest;
import org.jdiscript.requests.ChainingModificationWatchpointRequest;
import org.jdiscript.requests.ChainingMonitorContendedEnterRequest;
import org.jdiscript.requests.ChainingMonitorContendedEnteredRequest;
import org.jdiscript.requests.ChainingMonitorWaitRequest;
import org.jdiscript.requests.ChainingMonitorWaitedRequest;
import org.jdiscript.requests.ChainingStepRequest;
import org.jdiscript.requests.ChainingThreadDeathRequest;
import org.jdiscript.requests.ChainingThreadStartRequest;
import org.jdiscript.requests.ChainingVMDeathRequest;
import org.jdiscript.requests.EventRequestProxy;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
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

public class JDIScript {

	public static final String PROP_KEY = "JDISCRIPT_HANDLER";

	@SuppressWarnings("unchecked")
	public static Set<DebugEventHandler> getHandlers(EventRequest er) {
		return (Set<DebugEventHandler>)(er.getProperty(PROP_KEY));
	}
	
	private final VirtualMachine vm;
	private final EventRequestManager erm;
	
	public JDIScript(VirtualMachine vm) {
		this.vm = vm;
		this.erm = vm.eventRequestManager();
	}
	
	public VirtualMachine vm() {
		return vm;
	}

	public void run() {
		run(0);
	}

	public void run(long millis) {
		List<DebugEventHandler> empty = Collections.emptyList();
		run(empty, millis);
	}

	public void run(DebugEventHandler handler) {
		run(handler, 0);
	}
	
    public void run(List<DebugEventHandler> handlers) {
    	run(handlers, 0);
    }
    
    public void run(DebugEventHandler handler, long millis) {
    	run(Collections.singletonList(handler), millis);
    }
    
    public void run(List<DebugEventHandler> handlers, long millis) {
    	DebugEventDispatcher dispatcher = new DebugEventDispatcher();
    	dispatcher.addHandlers(handlers);
    	EventThread eventThread = new EventThread(vm, dispatcher);
    	eventThread.start();
    	try {
    		eventThread.join(millis);
    	} catch(InterruptedException exc) {
    		//TODO: handle this?
    	}
    	
    }
	
	// Convenience methods for creating EventRequests, that will automatically
	// set the handler as a property so that the Dispatcher works correctly.
    public ChainingAccessWatchpointRequest accessWatchpointRequest(Field field) {
        return (ChainingAccessWatchpointRequest)EventRequestProxy.proxy(
        		erm.createAccessWatchpointRequest(field),
        		ChainingAccessWatchpointRequest.class);
    }

    public ChainingBreakpointRequest breakpointRequest(Location location) {
        return (ChainingBreakpointRequest)EventRequestProxy.proxy(
        		erm.createBreakpointRequest(location),
        		ChainingBreakpointRequest.class);
    }

    public ChainingClassPrepareRequest classPrepareRequest() {
        return (ChainingClassPrepareRequest)EventRequestProxy.proxy(
        		erm.createClassPrepareRequest(),
        		ChainingClassPrepareRequest.class);
    }

    public ChainingClassUnloadRequest classUnloadRequest() {
        return (ChainingClassUnloadRequest)EventRequestProxy.proxy(
        		erm.createClassUnloadRequest(),
        		ChainingClassUnloadRequest.class);
    }
    
    public ChainingExceptionRequest exceptionRequest(ReferenceType refType, 
    										 boolean notifyCaught, 
    										 boolean notifyUncaught) {
        return (ChainingExceptionRequest)EventRequestProxy.proxy(
        		erm.createExceptionRequest(refType, 
            							   notifyCaught, 
            							   notifyUncaught),
            	ChainingExceptionRequest.class);
    }

    public ChainingMethodEntryRequest methodEntryRequest() {
        return (ChainingMethodEntryRequest)EventRequestProxy.proxy(
        		erm.createMethodEntryRequest(),
        		ChainingMethodEntryRequest.class);
    }
    
    public ChainingMethodExitRequest methodExitRequest() {
        return (ChainingMethodExitRequest)EventRequestProxy.proxy(
        		erm.createMethodExitRequest(),
        		ChainingMethodExitRequest.class);
    }
    
    public ChainingModificationWatchpointRequest modificationWatchpointRequest(Field field) {
        return (ChainingModificationWatchpointRequest)EventRequestProxy.proxy(
        		erm.createModificationWatchpointRequest(field),
        		ChainingModificationWatchpointRequest.class);
    }
    
    public ChainingMonitorContendedEnteredRequest monitorContendedEnteredRequest() {
        return (ChainingMonitorContendedEnteredRequest)EventRequestProxy.proxy(
        		erm.createMonitorContendedEnteredRequest(),
        		ChainingMonitorContendedEnteredRequest.class);
    }

    public ChainingMonitorContendedEnterRequest monitorContendedEnterRequest() {
        return (ChainingMonitorContendedEnterRequest)EventRequestProxy.proxy(
        		erm.createMonitorContendedEnterRequest(),
        		ChainingMonitorContendedEnterRequest.class);
    }
    
    public ChainingMonitorWaitedRequest monitorWaitedRequest() {
        return (ChainingMonitorWaitedRequest)EventRequestProxy.proxy(
        		erm.createMonitorWaitedRequest(),
        		ChainingMonitorWaitedRequest.class);
    }
    
    public ChainingMonitorWaitRequest monitorWaitRequest() {
        return (ChainingMonitorWaitRequest)EventRequestProxy.proxy(
        		erm.createMonitorWaitRequest(),
        		ChainingMonitorWaitRequest.class);
    }
    
    public ChainingStepRequest stepRequest( ThreadReference thread, 
    								        int size, 
    								        int depth ) {
        return (ChainingStepRequest)EventRequestProxy.proxy(
        		erm.createStepRequest(thread, size, depth),
        		ChainingStepRequest.class);
    }
    
    public ChainingThreadDeathRequest threadDeathRequest() {
        return (ChainingThreadDeathRequest)EventRequestProxy.proxy(
        		erm.createThreadDeathRequest(),
        		ChainingThreadDeathRequest.class);
    }
    
    public ChainingThreadStartRequest threadStartRequest() {
        return (ChainingThreadStartRequest)EventRequestProxy.proxy(
        		erm.createThreadStartRequest(),
        		ChainingThreadStartRequest.class);
    }
    
    public ChainingVMDeathRequest vmDeathRequest() {
        return (ChainingVMDeathRequest)EventRequestProxy.proxy(
        		erm.createVMDeathRequest(),
        		ChainingVMDeathRequest.class);
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

    public <T extends EventRequest> List<T> filter(List<T> ers) {
    	List<T> out = new ArrayList<T>();
    	for(T er: ers) {
    		if(er.getProperty(PROP_KEY) == this) {
    			out.add(er);
    		}
    	}
    	return out;
    }
}
