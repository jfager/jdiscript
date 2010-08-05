package org.jdiscript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.events.EventThread;
import org.jdiscript.handlers.DebugEventHandler;
import org.jdiscript.handlers.OnAccessWatchpoint;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnClassPrepare;
import org.jdiscript.handlers.OnClassUnload;
import org.jdiscript.handlers.OnException;
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
        return accessWatchpointRequest(field, null);
    }

    public ChainingAccessWatchpointRequest accessWatchpointRequest(Field field, OnAccessWatchpoint handler) {
        return ((ChainingAccessWatchpointRequest)EventRequestProxy.proxy(
        		erm.createAccessWatchpointRequest(field),
        		ChainingAccessWatchpointRequest.class)).addHandler(handler);
    }

    public ChainingBreakpointRequest breakpointRequest(Location location) {
        return breakpointRequest(location, null);
    }

    public ChainingBreakpointRequest breakpointRequest(Location location, OnBreakpoint handler) {
        return ((ChainingBreakpointRequest)EventRequestProxy.proxy(
        		erm.createBreakpointRequest(location),
        		ChainingBreakpointRequest.class)).addHandler(handler);
    }

    public ChainingClassPrepareRequest classPrepareRequest() {
        return classPrepareRequest(null);
    }

    public ChainingClassPrepareRequest classPrepareRequest(OnClassPrepare handler) {
        return ((ChainingClassPrepareRequest)EventRequestProxy.proxy(
        		erm.createClassPrepareRequest(),
        		ChainingClassPrepareRequest.class)).addHandler(handler);
    }

    public ChainingClassUnloadRequest classUnloadRequest() {
        return classUnloadRequest(null);
    }

    public ChainingClassUnloadRequest classUnloadRequest(OnClassUnload handler) {
        return ((ChainingClassUnloadRequest)EventRequestProxy.proxy(
        		erm.createClassUnloadRequest(),
        		ChainingClassUnloadRequest.class)).addHandler(handler);
    }
    
    public ChainingExceptionRequest exceptionRequest(ReferenceType refType, 
    										 boolean notifyCaught, 
    										 boolean notifyUncaught) {
    	return exceptionRequest(refType, notifyCaught, notifyUncaught, null);
    }

    public ChainingExceptionRequest exceptionRequest(ReferenceType refType, 
    										 boolean notifyCaught, 
    										 boolean notifyUncaught, 
    										 OnException handler) {
        return ((ChainingExceptionRequest)EventRequestProxy.proxy(
        		erm.createExceptionRequest(refType, 
            							   notifyCaught, 
            							   notifyUncaught),
            	ChainingExceptionRequest.class)).addHandler(handler);
    }

    public ChainingMethodEntryRequest methodEntryRequest() {
        return methodEntryRequest(null);
    }

    public ChainingMethodEntryRequest methodEntryRequest(OnMethodEntry handler) {
        return ((ChainingMethodEntryRequest)EventRequestProxy.proxy(
        		erm.createMethodEntryRequest(),
        		ChainingMethodEntryRequest.class)).addHandler(handler);
    }
    
    public ChainingMethodExitRequest methodExitRequest() {
        return methodExitRequest(null);
    }

    public ChainingMethodExitRequest methodExitRequest(OnMethodExit handler) {
        return ((ChainingMethodExitRequest)EventRequestProxy.proxy(
        		erm.createMethodExitRequest(),
        		ChainingMethodExitRequest.class)).addHandler(handler);
    }
    
    public ChainingModificationWatchpointRequest modificationWatchpointRequest(Field field) {
        return modificationWatchpointRequest(field, null);
    }

    public ChainingModificationWatchpointRequest modificationWatchpointRequest(Field field, OnModificationWatchpoint handler) {
        return ((ChainingModificationWatchpointRequest)EventRequestProxy.proxy(
        		erm.createModificationWatchpointRequest(field),
        		ChainingModificationWatchpointRequest.class)).addHandler(handler);
    }
    
    public ChainingMonitorContendedEnteredRequest monitorContendedEnteredRequest() {
        return monitorContendedEnteredRequest(null);
    }

    public ChainingMonitorContendedEnteredRequest monitorContendedEnteredRequest(OnMonitorContendedEntered handler) {
        return ((ChainingMonitorContendedEnteredRequest)EventRequestProxy.proxy(
        		erm.createMonitorContendedEnteredRequest(),
        		ChainingMonitorContendedEnteredRequest.class)).addHandler(handler);
    }

    public ChainingMonitorContendedEnterRequest monitorContendedEnterRequest() {
        return monitorContendedEnterRequest(null);
    }

    public ChainingMonitorContendedEnterRequest monitorContendedEnterRequest(OnMonitorContendedEnter handler) {
        return ((ChainingMonitorContendedEnterRequest)EventRequestProxy.proxy(
        		erm.createMonitorContendedEnterRequest(),
        		ChainingMonitorContendedEnterRequest.class)).addHandler(handler);
    }
    
    public ChainingMonitorWaitedRequest monitorWaitedRequest() {
        return monitorWaitedRequest(null);
    }

    public ChainingMonitorWaitedRequest monitorWaitedRequest(OnMonitorWaited handler) {
        return ((ChainingMonitorWaitedRequest)EventRequestProxy.proxy(
        		erm.createMonitorWaitedRequest(),
        		ChainingMonitorWaitedRequest.class)).addHandler(handler);
    }
    
    public ChainingMonitorWaitRequest monitorWaitRequest() {
        return monitorWaitRequest(null);
    }

    public ChainingMonitorWaitRequest monitorWaitRequest(OnMonitorWait handler) {
        return ((ChainingMonitorWaitRequest)EventRequestProxy.proxy(
        		erm.createMonitorWaitRequest(),
        		ChainingMonitorWaitRequest.class)).addHandler(handler);
    }
    
    public ChainingStepRequest stepRequest( ThreadReference thread, 
    								        int size, 
    								        int depth ) {
    	return stepRequest( thread, size, depth , null);
    }

    public ChainingStepRequest stepRequest( ThreadReference thread, 
    								    	int size, 
    								    	int depth,
    								    	OnStep handler) {
    	return ((ChainingStepRequest)EventRequestProxy.proxy(
        		erm.createStepRequest(thread, size, depth),
        		ChainingStepRequest.class)).addHandler(handler);
    }
    
    public ChainingThreadDeathRequest threadDeathRequest() {
        return threadDeathRequest(null);
    }

    public ChainingThreadDeathRequest threadDeathRequest(OnThreadDeath handler) {
        return ((ChainingThreadDeathRequest)EventRequestProxy.proxy(
        		erm.createThreadDeathRequest(),
        		ChainingThreadDeathRequest.class)).addHandler(handler);
    }
    
    public ChainingThreadStartRequest threadStartRequest() {
        return threadStartRequest(null);
    }

    public ChainingThreadStartRequest threadStartRequest(OnThreadStart handler) {
        return ((ChainingThreadStartRequest)EventRequestProxy.proxy(
        		erm.createThreadStartRequest(),
        		ChainingThreadStartRequest.class)).addHandler(handler);
    }
    
    public ChainingVMDeathRequest vmDeathRequest() {
        return vmDeathRequest(null);
    }

    public ChainingVMDeathRequest vmDeathRequest(OnVMDeath handler) {
        return ((ChainingVMDeathRequest)EventRequestProxy.proxy(
        		erm.createVMDeathRequest(),
        		ChainingVMDeathRequest.class)).addHandler(handler);
    }
    
    // Convenience method for accessing only those EventRequests that are 
    // associated with a given handler

    public List<AccessWatchpointRequest> accessWatchpointRequests(DebugEventHandler handler) {
        return filter(erm.accessWatchpointRequests(), handler);
    }
    
    public List<BreakpointRequest> breakpointRequests(DebugEventHandler handler) {
        return filter(erm.breakpointRequests(), handler);
    }
    
    public List<ClassPrepareRequest> classPrepareRequests(DebugEventHandler handler) {
        return filter(erm.classPrepareRequests(), handler);
    }
    
    public List<ClassUnloadRequest> classUnloadRequests(DebugEventHandler handler) {
        return filter(erm.classUnloadRequests(), handler);
    }
    
    public List<ExceptionRequest> exceptionRequests(DebugEventHandler handler) {
        return filter(erm.exceptionRequests(), handler);
    }
    
    public List<MethodEntryRequest> methodEntryRequests(DebugEventHandler handler) {
        return filter(erm.methodEntryRequests(), handler);
    }
    
    public List<MethodExitRequest> methodExitRequests(DebugEventHandler handler) {
        return filter(erm.methodExitRequests(), handler);
    }
    
    public List<ModificationWatchpointRequest> modificationWatchpointRequests(DebugEventHandler handler) {
        return filter(erm.modificationWatchpointRequests(), handler);
    }
    
    public List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests(DebugEventHandler handler) {
        return filter(erm.monitorContendedEnteredRequests(), handler);
    }
    
    public List<MonitorContendedEnterRequest> monitorContendedEnterRequests(DebugEventHandler handler) {
        return filter(erm.monitorContendedEnterRequests(), handler);
    }
    
    public List<MonitorWaitedRequest> monitorWaitedRequests(DebugEventHandler handler) {
        return filter(erm.monitorWaitedRequests(), handler);
    }
    
    public List<MonitorWaitRequest> monitorWaitRequests(DebugEventHandler handler) {
        return filter(erm.monitorWaitRequests(), handler);
    }
    
    public List<StepRequest> stepRequests(DebugEventHandler handler) {
        return filter(erm.stepRequests(), handler);
    }
    
    public List<ThreadDeathRequest> threadDeathRequests(DebugEventHandler handler) {
        return filter(erm.threadDeathRequests(), handler);
    }
    
    public List<ThreadStartRequest> threadStartRequests(DebugEventHandler handler) {
        return filter(erm.threadStartRequests(), handler);
    }
    
    public List<VMDeathRequest> vmDeathRequests(DebugEventHandler handler) {
        return filter(erm.vmDeathRequests(), handler);
    }

    public <T extends EventRequest> List<T> filter(List<T> ers, DebugEventHandler handler) {
    	List<T> out = new ArrayList<T>();
    	for(T er: ers) {
    		Set<DebugEventHandler> handlers = getHandlers(er);
    		if(handlers.contains(handler)) {
    			out.add(er);
    		}
    	}
    	return out;
    }
    
    public void deleteEventRequest(EventRequest eventRequest) {
    	erm.deleteEventRequest(eventRequest);
    }
    
    public void deleteEventRequests(List<? extends EventRequest> eventRequests) {
    	erm.deleteEventRequests(eventRequests);
    }
}
