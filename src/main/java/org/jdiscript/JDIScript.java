package org.jdiscript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jdiscript.events.*;
import org.jdiscript.handlers.*;
import org.jdiscript.requests.*;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.*;

/**
 * Top-level class encapsulating common operations for working with
 * JDI and jdiscript.
 * <p>
 * <ul>
 *  <li>
 *    Pulls the {@link EventRequestManager} create*Request
 *    methods up to the top level, shortens their names, returns
 *    chainable requests, and allows handlers to be specified when
 *    requests are created.
 *  </li>
 *  <li>
 *    Pulls the {@link EventRequestManager} *Requests
 *    methods up to the top level and adds filtering by handler.
 *  </li>
 *  <li>
 *    Runs the associated VM by setting up and starting the jdiscript
 *    {@link DebugEventDispatcher} and {@link EventThread}.
 *  </li>
 * </ul>
 */
public class JDIScript {

    private final VirtualMachine vm;
    private final EventRequestManager erm;

    public JDIScript(VirtualMachine vm) {
        this.vm = vm;
        this.erm = vm.eventRequestManager();
    }

    /**
     * @return The underlying {@link VirtualMachine}.
     */
    public VirtualMachine vm() {
        return vm;
    }

    /**
     * Run the underlying {@link VirtualMachine} with no VM
     * event handlers and no timeout.  Note that you may still add
     * handlers for other events created with this class's *Request
     * methods.
     */
    public void run() {
        run(0);
    }

    /**
     * Run the underlying {@link VirtualMachine} with no VM
     * event handlers and a given timeout, in millis.  Note that
     * you may still add handlers for other events created with
     * this class's *Request methods.
     *
     * @param millis    Timeout in millis.
     */
    public void run(long millis) {
        List<DebugEventHandler> empty = Collections.emptyList();
        run(empty, millis);
    }

    /**
     * Run the underlying {@link VirtualMachine} with a single VM
     * event handler and no timeout.  Note that you may still add
     * handlers for other events created with this class's *Request
     * methods.
     *
     * @param handler  A DebugEventHandler for VM events.
     */
    public void run(DebugEventHandler handler) {
        run(handler, 0);
    }

    /**
     * Run the underlying {@link VirtualMachine} with multiple VM
     * event handlers and no timeout.  Note that you may still add
     * handlers for other events created with this class's *Request
     * methods.
     *
     * @param handlers  A list of DebugEventHandlers for VM events.
     */
    public void run(List<DebugEventHandler> handlers) {
        run(handlers, 0);
    }

    /**
     * Run the underlying {@link VirtualMachine} with a single VM
     * event handler and a given timeout, in millis.  Note that you
     * may still add handlers for other events created with this
     * class's *Request methods.
     *
     * @param handler  A DebugEventHandler for VM events.
     * @param millis    Timeout in millis.
     */
    public void run(DebugEventHandler handler, long millis) {
        run(Collections.singletonList(handler), millis);
    }

    /**
     * Run the underlying {@link VirtualMachine} with a multiple VM
     * event handlers and a given timeout, in millis.  Note that you
     * may still add handlers for other events created with this
     * class's *Request methods.
     *
     * @param handlers  A list of DebugEventHandlers for VM events.
     * @param millis    Timeout in millis.
     */
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

    /**
     * @see EventRequestManager#createAccessWatchpointRequest
     */
    public ChainingAccessWatchpointRequest accessWatchpointRequest(Field field) {
        return accessWatchpointRequest(field, null);
    }

    /**
     * @see EventRequestManager#createAccessWatchpointRequest
     */
    public ChainingAccessWatchpointRequest accessWatchpointRequest(Field field, OnAccessWatchpoint handler) {
        return ((ChainingAccessWatchpointRequest)EventRequestProxy.proxy(
                erm.createAccessWatchpointRequest(field),
                ChainingAccessWatchpointRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createBreakpointRequest
     */
    public ChainingBreakpointRequest breakpointRequest(Location location) {
        return breakpointRequest(location, null);
    }

    /**
     * @see EventRequestManager#createBreakpointRequest
     */
    public ChainingBreakpointRequest breakpointRequest(Location location, OnBreakpoint handler) {
        return ((ChainingBreakpointRequest)EventRequestProxy.proxy(
                erm.createBreakpointRequest(location),
                ChainingBreakpointRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createClassPrepareRequest
     */
    public ChainingClassPrepareRequest classPrepareRequest() {
        return classPrepareRequest(null);
    }

    /**
     * @see EventRequestManager#createClassPrepareRequest
     */
    public ChainingClassPrepareRequest classPrepareRequest(OnClassPrepare handler) {
        return ((ChainingClassPrepareRequest)EventRequestProxy.proxy(
                erm.createClassPrepareRequest(),
                ChainingClassPrepareRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createClassUnloadRequest
     */
    public ChainingClassUnloadRequest classUnloadRequest() {
        return classUnloadRequest(null);
    }

    /**
     * @see EventRequestManager#createClassUnloadRequest
     */
    public ChainingClassUnloadRequest classUnloadRequest(OnClassUnload handler) {
        return ((ChainingClassUnloadRequest)EventRequestProxy.proxy(
                erm.createClassUnloadRequest(),
                ChainingClassUnloadRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createExceptionRequest
     */
    public ChainingExceptionRequest exceptionRequest(ReferenceType refType,
                                             boolean notifyCaught,
                                             boolean notifyUncaught) {
        return exceptionRequest(refType, notifyCaught, notifyUncaught, null);
    }

    /**
     * @see EventRequestManager#createExceptionRequest
     */
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

    /**
     * @see EventRequestManager#createMethodEntryRequest
     */
    public ChainingMethodEntryRequest methodEntryRequest() {
        return methodEntryRequest(null);
    }

    /**
     * @see EventRequestManager#createMethodEntryRequest
     */
    public ChainingMethodEntryRequest methodEntryRequest(OnMethodEntry handler) {
        return ((ChainingMethodEntryRequest)EventRequestProxy.proxy(
                erm.createMethodEntryRequest(),
                ChainingMethodEntryRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMethodExitRequest
     */
    public ChainingMethodExitRequest methodExitRequest() {
        return methodExitRequest(null);
    }

    /**
     * @see EventRequestManager#createMethodExitRequest
     */
    public ChainingMethodExitRequest methodExitRequest(OnMethodExit handler) {
        return ((ChainingMethodExitRequest)EventRequestProxy.proxy(
                erm.createMethodExitRequest(),
                ChainingMethodExitRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createModificationWatchpointRequest
     */
    public ChainingModificationWatchpointRequest modificationWatchpointRequest(Field field) {
        return modificationWatchpointRequest(field, null);
    }

    /**
     * @see EventRequestManager#createModificationWatchpointRequest
     */
    public ChainingModificationWatchpointRequest modificationWatchpointRequest(Field field, OnModificationWatchpoint handler) {
        return ((ChainingModificationWatchpointRequest)EventRequestProxy.proxy(
                erm.createModificationWatchpointRequest(field),
                ChainingModificationWatchpointRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMonitorContendedEnteredRequest
     */
    public ChainingMonitorContendedEnteredRequest monitorContendedEnteredRequest() {
        return monitorContendedEnteredRequest(null);
    }

    /**
     * @see EventRequestManager#createMonitorContendedEnteredRequest
     */
    public ChainingMonitorContendedEnteredRequest monitorContendedEnteredRequest(OnMonitorContendedEntered handler) {
        return ((ChainingMonitorContendedEnteredRequest)EventRequestProxy.proxy(
                erm.createMonitorContendedEnteredRequest(),
                ChainingMonitorContendedEnteredRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMonitorContendedEnterRequest
     */
    public ChainingMonitorContendedEnterRequest monitorContendedEnterRequest() {
        return monitorContendedEnterRequest(null);
    }

    /**
     * @see EventRequestManager#createMonitorContendedEnterRequest
     */
    public ChainingMonitorContendedEnterRequest monitorContendedEnterRequest(OnMonitorContendedEnter handler) {
        return ((ChainingMonitorContendedEnterRequest)EventRequestProxy.proxy(
                erm.createMonitorContendedEnterRequest(),
                ChainingMonitorContendedEnterRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMonitorWaitedRequest
     */
    public ChainingMonitorWaitedRequest monitorWaitedRequest() {
        return monitorWaitedRequest(null);
    }

    /**
     * @see EventRequestManager#createMonitorWaitedRequest
     */
    public ChainingMonitorWaitedRequest monitorWaitedRequest(OnMonitorWaited handler) {
        return ((ChainingMonitorWaitedRequest)EventRequestProxy.proxy(
                erm.createMonitorWaitedRequest(),
                ChainingMonitorWaitedRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMonitorWaitRequest
     */
    public ChainingMonitorWaitRequest monitorWaitRequest() {
        return monitorWaitRequest(null);
    }

    /**
     * @see EventRequestManager#createMonitorWaitRequest
     */
    public ChainingMonitorWaitRequest monitorWaitRequest(OnMonitorWait handler) {
        return ((ChainingMonitorWaitRequest)EventRequestProxy.proxy(
                erm.createMonitorWaitRequest(),
                ChainingMonitorWaitRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createStepRequest
     */
    public ChainingStepRequest stepRequest( ThreadReference thread,
                                            int size,
                                            int depth ) {
        return stepRequest( thread, size, depth , null);
    }

    /**
     * @see EventRequestManager#createStepRequest
     */
    public ChainingStepRequest stepRequest( ThreadReference thread,
                                            int size,
                                            int depth,
                                            OnStep handler) {
        return ((ChainingStepRequest)EventRequestProxy.proxy(
                erm.createStepRequest(thread, size, depth),
                ChainingStepRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createThreadDeathRequest
     */
    public ChainingThreadDeathRequest threadDeathRequest() {
        return threadDeathRequest(null);
    }

    /**
     * @see EventRequestManager#createThreadDeathRequest
     */
    public ChainingThreadDeathRequest threadDeathRequest(OnThreadDeath handler) {
        return ((ChainingThreadDeathRequest)EventRequestProxy.proxy(
                erm.createThreadDeathRequest(),
                ChainingThreadDeathRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createThreadStartRequest
     */
    public ChainingThreadStartRequest threadStartRequest() {
        return threadStartRequest(null);
    }

    /**
     * @see EventRequestManager#createThreadStartRequest
     */
    public ChainingThreadStartRequest threadStartRequest(OnThreadStart handler) {
        return ((ChainingThreadStartRequest)EventRequestProxy.proxy(
                erm.createThreadStartRequest(),
                ChainingThreadStartRequest.class)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createVMDeathRequest
     */
    public ChainingVMDeathRequest vmDeathRequest() {
        return vmDeathRequest(null);
    }

    /**
     * @see EventRequestManager#createVMDeathRequest
     */
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
            Set<DebugEventHandler> handlers = DebugEventDispatcher.getHandlers(er);
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
