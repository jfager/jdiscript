package org.jdiscript;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import org.jdiscript.handlers.Once;
import org.jdiscript.handlers.Sampled;
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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.LocatableEvent;
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
 * Top-level class encapsulating common operations for working with
 * JDI and jdiscript.
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
 *  <li>
 *    Provides convenience methods for specifying handlers for field
 *    access and modification.
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
            Thread.currentThread().interrupt();
        }

    }

    /**
     * Start the event loop in the background and return immediately.
     * <p>
     * Unlike {@link #run()}, this method does not block.  The returned
     * {@link EventThread} runs as a daemon thread; the JVM can exit normally
     * even if the target VM is still connected.
     * <p>
     * Handlers registered via any of the {@code on*} / {@code *Request} methods
     * — before or after calling {@code start()} — are picked up automatically.
     * This is the foundation for interactive and agentic debugging: start the
     * loop once, then drive it from external input (stdin, HTTP, AI tool calls,
     * a JShell session, etc.).
     * <p>
     * Example:
     * <pre>
     *   JDIScript j = new JDIScript(new VMSocketAttacher(5005).attach());
     *   EventThread et = j.start();
     *   // Register handlers dynamically...
     *   j.onMethodInvocation("com.example.Foo", "bar", e -&gt; { ... });
     *   et.join(); // optional: block until VM exits
     * </pre>
     *
     * @return The running {@link EventThread} (daemon thread).
     */
    public EventThread start() {
        return start(Collections.emptyList());
    }

    /**
     * Like {@link #start()}, with VM-level event handlers (VMDeath, VMDisconnect,
     * etc.) pre-registered.
     *
     * @param handlers VM-level event handlers.
     * @return The running {@link EventThread} (daemon thread).
     */
    public EventThread start(List<DebugEventHandler> handlers) {
        DebugEventDispatcher dispatcher = new DebugEventDispatcher();
        dispatcher.addHandlers(handlers);
        EventThread et = new EventThread(vm, dispatcher);
        et.setDaemon(true);
        et.start();
        return et;
    }

    /**
     * Suspend all threads in the target VM, run {@code work}, then resume.
     * <p>
     * This is the primary mechanism for <em>point-in-time inspection</em> in
     * interactive and agentic workflows: pause the VM, read state (instance
     * counts, field values, etc.), and let it continue — all without needing a
     * breakpoint event.
     * <p>
     * {@link VirtualMachine#suspend()} and {@link VirtualMachine#resume()} use a
     * reference count, so calling this inside an event handler (where the VM is
     * already partly suspended) is safe.
     * <p>
     * <strong>Do not invoke remote methods</strong> inside {@code work} unless
     * you use {@link ObjectReference#INVOKE_SINGLE_THREADED}, since all other
     * threads are paused.
     *
     * @param work Code to execute while the VM is fully suspended.
     */
    public void withSuspend(Runnable work) {
        vm.suspend();
        try {
            work.run();
        } finally {
            vm.resume();
        }
    }

    /**
     * Like {@link #withSuspend(Runnable)} but returns a value.
     *
     * @param <T>  The return type.
     * @param work Code to execute while the VM is fully suspended; its return
     *             value is forwarded to the caller.
     * @return Whatever {@code work} returns.
     */
    public <T> T withSuspend(Supplier<T> work) {
        vm.suspend();
        try {
            return work.get();
        } finally {
            vm.resume();
        }
    }

    // Convenience methods for creating EventRequests, that will automatically
    // set the handler as a property so that the Dispatcher works correctly.

    /**
     * @see EventRequestManager#createAccessWatchpointRequest
     *
     * @param field The Field to watch.
     * @return A chaining wrapper for {@link AccessWatchpointRequest}
     */
    public ChainingAccessWatchpointRequest accessWatchpointRequest(Field field) {
        return accessWatchpointRequest(field, null);
    }

    /**
     * @see EventRequestManager#createAccessWatchpointRequest
     *
     * @param field The Field to watch.
     * @param handler A handler that fires when the field is accessed.
     * @return A chaining wrapper for {@link AccessWatchpointRequest}
     */
    public ChainingAccessWatchpointRequest accessWatchpointRequest(Field field, OnAccessWatchpoint handler) {
        return new ChainingAccessWatchpointRequest(erm.createAccessWatchpointRequest(field)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createBreakpointRequest
     *
     * @param location The location to register a breakpoint
     * @return A chaining wrapper for {@link BreakpointRequest}
     */
    public ChainingBreakpointRequest breakpointRequest(Location location) {
        return breakpointRequest(location, null);
    }

    /**
     * @see EventRequestManager#createBreakpointRequest
     *
     * @param location The location to register a breakpoint
     * @param handler A handler that fires when the breakpoint is hit.
     * @return A chaining wrapper for {@link BreakpointRequest}
     */
    public ChainingBreakpointRequest breakpointRequest(Location location, OnBreakpoint handler) {
        return new ChainingBreakpointRequest(erm.createBreakpointRequest(location)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createClassPrepareRequest
     *
     * @return A chaining wrapper for {@link ClassPrepareRequest}
     */
    public ChainingClassPrepareRequest classPrepareRequest() {
        return classPrepareRequest(null);
    }

    /**
     * @see EventRequestManager#createClassPrepareRequest
     *
     * @param handler A handler that fires when the class is prepared.
     * @return A chaining wrapper for {@link ClassPrepareRequest}
     */
    public ChainingClassPrepareRequest classPrepareRequest(OnClassPrepare handler) {
        return new ChainingClassPrepareRequest(erm.createClassPrepareRequest()).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createClassUnloadRequest
     *
     * @return A chaining wrapper for {@link ClassUnloadRequest}
     */
    public ChainingClassUnloadRequest classUnloadRequest() {
        return classUnloadRequest(null);
    }

    /**
     * @see EventRequestManager#createClassUnloadRequest
     *
     * @param handler A handler that fires when the class is unloaded.
     * @return A chaining wrapper for {@link ClassUnloadRequest}
     */
    public ChainingClassUnloadRequest classUnloadRequest(OnClassUnload handler) {
        return new ChainingClassUnloadRequest(erm.createClassUnloadRequest()).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createExceptionRequest
     *
     * @param refType If non-null, report exceptions which are
     *                instances of refType (and its sub-types).  If null,
     *                all exceptions will be reported.
     * @param notifyCaught true to report caught exceptions
     * @param notifyUncaught true to report uncaught exceptions.
     * @return A chaining wrapper for {@link ExceptionRequest}
     */
    public ChainingExceptionRequest exceptionRequest(ReferenceType refType,
                                             boolean notifyCaught,
                                             boolean notifyUncaught) {
        return exceptionRequest(refType, notifyCaught, notifyUncaught, null);
    }

    /**
     * @see EventRequestManager#createExceptionRequest
     * @param refType If non-null, report exceptions which are
     *                instances of refType (and its sub-types).  If null,
     *                all exceptions will be reported.
     * @param notifyCaught true to report caught exceptions
     * @param notifyUncaught true to report uncaught exceptions.
     * @param handler A handler that fires when a matched exception occurs.
     * @return A chaining wrapper for {@link ExceptionRequest}
     */
    public ChainingExceptionRequest exceptionRequest(ReferenceType refType,
                                             boolean notifyCaught,
                                             boolean notifyUncaught,
                                             OnException handler) {
        return new ChainingExceptionRequest(erm.createExceptionRequest(refType, notifyCaught, notifyUncaught)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMethodEntryRequest
     *
     * @return A chaining wrapper for {@link MethodEntryRequest}
     */
    public ChainingMethodEntryRequest methodEntryRequest() {
        return methodEntryRequest(null);
    }

    /**
     * @see EventRequestManager#createMethodEntryRequest
     *
     * @param handler A handler that fires when a method is entered.
     * @return A chaining wrapper for {@link MethodEntryRequest}
     */
    public ChainingMethodEntryRequest methodEntryRequest(OnMethodEntry handler) {
        return new ChainingMethodEntryRequest(erm.createMethodEntryRequest()).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMethodExitRequest
     *
     * @return A chaining wrapper for {@link MethodExitRequest}
     */
    public ChainingMethodExitRequest methodExitRequest() {
        return methodExitRequest(null);
    }

    /**
     * @see EventRequestManager#createMethodExitRequest
     *
     * @param handler A handler that fires when a method is exited.
     * @return A chaining wrapper for {@link MethodExitRequest}
     */
    public ChainingMethodExitRequest methodExitRequest(OnMethodExit handler) {
        return new ChainingMethodExitRequest(erm.createMethodExitRequest()).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createModificationWatchpointRequest
     *
     * @param field The field to watch for modifications.
     * @return A chaining wrapper for {@link ModificationWatchpointRequest}
     */
    public ChainingModificationWatchpointRequest modificationWatchpointRequest(Field field) {
        return modificationWatchpointRequest(field, null);
    }

    /**
     * @see EventRequestManager#createModificationWatchpointRequest
     *
     * @param field The field to watch for modifications.
     * @param handler A handler that fires when the field is modified.
     * @return A chaining wrapper for {@link ModificationWatchpointRequest}
     */
    public ChainingModificationWatchpointRequest modificationWatchpointRequest(Field field, OnModificationWatchpoint handler) {
        return new ChainingModificationWatchpointRequest(erm.createModificationWatchpointRequest(field)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMonitorContendedEnteredRequest
     *
     * @return A chaining wrapper for {@link MonitorContendedEnteredRequest}
     */
    public ChainingMonitorContendedEnteredRequest monitorContendedEnteredRequest() {
        return monitorContendedEnteredRequest(null);
    }

    /**
     * @see EventRequestManager#createMonitorContendedEnteredRequest
     *
     * @param handler A handler that fires when a contended monitor is entered.
     * @return A chaining wrapper for {@link MonitorContendedEnteredRequest}
     */
    public ChainingMonitorContendedEnteredRequest monitorContendedEnteredRequest(OnMonitorContendedEntered handler) {
        return new ChainingMonitorContendedEnteredRequest(erm.createMonitorContendedEnteredRequest()).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMonitorContendedEnterRequest
     *
     * @return A chaining wrapper for {@link MonitorContendedEnterRequest}
     */
    public ChainingMonitorContendedEnterRequest monitorContendedEnterRequest() {
        return monitorContendedEnterRequest(null);
    }

    /**
     * @see EventRequestManager#createMonitorContendedEnterRequest
     *
     * @param handler A handler that fires when a thread attempts to enter a contended monitor.
     * @return A chaining wrapper for {@link MonitorContendedEnterRequest}
     */
    public ChainingMonitorContendedEnterRequest monitorContendedEnterRequest(OnMonitorContendedEnter handler) {
        return new ChainingMonitorContendedEnterRequest(erm.createMonitorContendedEnterRequest()).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createMonitorWaitedRequest
     *
     * @return A chaining wrapper for {@link MonitorWaitedRequest}
     */
    public ChainingMonitorWaitedRequest monitorWaitedRequest() {
        return monitorWaitedRequest(null);
    }

    /**
     * @see EventRequestManager#createMonitorWaitedRequest
     *
     * @param handler A handler that fires when a thread has finished waiting on a monitor.
     * @return A chaining wrapper for {@link MonitorWaitedRequest}
     */
    public ChainingMonitorWaitedRequest monitorWaitedRequest(OnMonitorWaited handler) {
        return new ChainingMonitorWaitedRequest(erm.createMonitorWaitedRequest()).addHandler(handler);
    }
    
    /**
     * @see EventRequestManager#createMonitorWaitRequest
     *
     * @return A chaining wrapper for {@link MonitorWaitRequest}
     */
    public ChainingMonitorWaitRequest monitorWaitRequest() {
        return monitorWaitRequest(null);
    }

    /**
     * @see EventRequestManager#createMonitorWaitRequest
     *
     * @param handler A handler that fires when a thread starts waiting on a monitor.
     * @return A chaining wrapper for {@link MonitorWaitRequest}
     */
    public ChainingMonitorWaitRequest monitorWaitRequest(OnMonitorWait handler) {
        return new ChainingMonitorWaitRequest(erm.createMonitorWaitRequest()).addHandler(handler);
    }    

    /**
     * @see EventRequestManager#createStepRequest
     *
     * @param thread The thread to step.
     * @param size The step size.
     * @param depth The step depth.
     * @return A chaining wrapper for {@link StepRequest}
     */
    public ChainingStepRequest stepRequest( ThreadReference thread,
                                            int size,
                                            int depth ) {
        return stepRequest(thread, size, depth, null);
    }

    /**
     * @see EventRequestManager#createStepRequest
     *
     * @param thread The thread to step.
     * @param size The step size.
     * @param depth The step depth.
     * @param handler A handler that fires when the step occurs.
     * @return A chaining wrapper for {@link StepRequest}
     */
    public ChainingStepRequest stepRequest( ThreadReference thread,
                                            int size,
                                            int depth,
                                            OnStep handler) {
        return new ChainingStepRequest(erm.createStepRequest(thread, size, depth)).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createThreadDeathRequest
     *
     * @return A chaining wrapper for {@link ThreadDeathRequest}
     */
    public ChainingThreadDeathRequest threadDeathRequest() {
        return threadDeathRequest(null);
    }

    /**
     * @see EventRequestManager#createThreadDeathRequest
     *
     * @param handler A handler that fires when a thread dies.
     * @return A chaining wrapper for {@link ThreadDeathRequest}
     */
    public ChainingThreadDeathRequest threadDeathRequest(OnThreadDeath handler) {
        return new ChainingThreadDeathRequest(erm.createThreadDeathRequest()).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createThreadStartRequest
     *
     * @return A chaining wrapper for {@link ThreadStartRequest}
     */
    public ChainingThreadStartRequest threadStartRequest() {
        return threadStartRequest(null);
    }

    /**
     * @see EventRequestManager#createThreadStartRequest
     *
     * @param handler A handler that fires when a thread starts.
     * @return A chaining wrapper for {@link ThreadStartRequest}
     */
    public ChainingThreadStartRequest threadStartRequest(OnThreadStart handler) {
        return new ChainingThreadStartRequest(erm.createThreadStartRequest()).addHandler(handler);
    }

    /**
     * @see EventRequestManager#createVMDeathRequest
     *
     * @return A chaining wrapper for {@link VMDeathRequest}
     */
    public ChainingVMDeathRequest vmDeathRequest() {
        return vmDeathRequest(null);
    }

    /**
     * @see EventRequestManager#createVMDeathRequest
     *
     * @param handler A handler that fires when the VM dies.
     * @return A chaining wrapper for {@link VMDeathRequest}
     */
    public ChainingVMDeathRequest vmDeathRequest(OnVMDeath handler) {
        return new ChainingVMDeathRequest(erm.createVMDeathRequest()).addHandler(handler);
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
        return ers.stream()
            .filter(er -> DebugEventDispatcher.getHandlers(er).contains(handler))
            .collect(Collectors.toList());
    }

    public void deleteEventRequest(EventRequest eventRequest) {
        erm.deleteEventRequest(eventRequest);
    }

    public void deleteEventRequests(List<? extends EventRequest> eventRequests) {
        erm.deleteEventRequests(eventRequests);
    }

    /**
     * Print a stacktrace of the given event's thread to stdout.
     *
     * @param event   The event to print a trace for.
     */
    public void printTrace(LocatableEvent event) {
        printTrace(event, null, System.out);
    }

    /**
     * Print a stacktrace of the given event's thread to stdout with
     * a message.
     *
     * @param event   The event to print a trace for.
     * @param msg     A message to print with the stacktrace.
     */
    public void printTrace(LocatableEvent event, String msg) {
        printTrace(event, msg, System.out);
    }

    /**
     * Print a stacktrace of the given event's thread to the given stream
     * with a message.
     *
     * @param event   The event to print a trace for.
     * @param msg     A message to print with the stacktrace.
     * @param ps      The stream to print the trace to.
     */
    public void printTrace(LocatableEvent event, String msg, PrintStream ps) {
        long ts = System.currentTimeMillis();
        ThreadReference thread = event.thread();
        ps.printf("%s: Stacktrace for %s(name='%s', id='%s'): %s\n",
                  ts,
                  thread.type().name(),
                  thread.name(),
                  thread.uniqueID(),
                  (msg == null) ? "" : msg);
        try {
            for(StackFrame frame: thread.frames()) {
                ps.println("    " + frame.location());
            }
        }
        catch(IncompatibleThreadStateException e) {
            ps.println("    ** IncompatibleThreadStateException - " +
                       "thread not suspended in target VM, " +
                       "could not retrieve frames **");
        }
    }
    
    /**
     * Shortcut for the common pattern of decorating any class
     * on preparation.
     * <p>
     * Builds a {@link ClassPrepareRequest}, and adds the given
     * {@link OnClassPrepare} handler.
     *
     * @param handler    The callback to execute when the class is prepped.
     * @return The underlying {@link ChainingClassPrepareRequest}, which can
     *         be used to disable or delete the request later.
     */
    public ChainingClassPrepareRequest onClassPrep(final OnClassPrepare handler) {
        ChainingClassPrepareRequest request = classPrepareRequest()
            .addHandler(handler);
        request.enable();
        return request;
    }

    /**
     * Shortcut for the common pattern of decorating a particular class
     * on preparation.
     * <p>
     * Builds a {@link ClassPrepareRequest}, filters it for the given
     * class name, and adds the given {@link OnClassPrepare} handler.
     *
     * @param className  A class name suitable for use by
     *                   {@link ClassPrepareRequest#addClassFilter(String)}
     * @param handler    The callback to execute when the class is prepped.
     * @return The underlying {@link ChainingClassPrepareRequest}, which can
     *         be used to disable or delete the request later.
     */
    public ChainingClassPrepareRequest onClassPrep(final String className,
                            final OnClassPrepare handler) {
        ChainingClassPrepareRequest request = classPrepareRequest()
            .addClassFilter(className)
            .addHandler(handler);
        request.enable();
        return request;
    }

    /**
     * Shortcut for the common pattern of responding to field accesses.
     * <p>
     * Builds a {@link ClassPrepareRequest}, filters it for the given
     * class name, and adds an {@link OnClassPrepare} handler that
     * creates an {@link AccessWatchpointRequest} for the given field name.
     * Any field accesses are in turn handled by the given handler.
     *
     * @param className  A class name suitable for use by
     *                   {@link ClassPrepareRequest#addClassFilter(String)}
     * @param fieldName  A field name suitable for use by
     *                   {@link ReferenceType#fieldByName(String)}.  Must be
     *                   a field belonging to all classes matched by className.
     * @param handler    The callback to execute when the field is accessed.
     * @return The underlying {@link ChainingClassPrepareRequest}, which can
     *         be used to disable or delete the request later.
     */
    public ChainingClassPrepareRequest onFieldAccess(final String className,
                              final String fieldName,
                              final OnAccessWatchpoint handler) {
        return onClassPrep(className, ev -> {
            Field field = ev.referenceType().fieldByName(fieldName);
            accessWatchpointRequest(field, handler).enable();
        });
    }

    /**
     * Shortcut for the common pattern of responding to field modifications.
     * <p>
     * Builds a {@link ClassPrepareRequest}, filters it for the given
     * class name, and adds an {@link OnClassPrepare} handler that
     * creates a {@link ModificationWatchpointRequest} for the given field name.
     * Any field modifications are in turn handled by the given handler.
     *
     * @param className  A class name suitable for use by
     *                   {@link ClassPrepareRequest#addClassFilter(String)}
     * @param fieldName  A field name suitable for use by
     *                   {@link ReferenceType#fieldByName(String)}.  Must be
     *                   a field belonging to all classes matched by className.
     * @param handler    The callback to execute when the field is modified.
     * @return The underlying {@link ChainingClassPrepareRequest}, which can
     *         be used to disable or delete the request later.
     */
    public ChainingClassPrepareRequest onFieldModification(final String className,
                                    final String fieldName,
                                    final OnModificationWatchpoint handler) {
        return onClassPrep(className, ev -> {
            Field field = ev.referenceType().fieldByName(fieldName);
            modificationWatchpointRequest(field, handler).enable();
        });
    }

    /**
     * Shortcut for the common pattern of responding to particular method
     * invocations.
     * <p>
     * You would hope this could be handled with a simple
     * {@link MethodEntryRequest}, but unfortunately, it can't.
     * MethodEntryRequests can be filtered by class, instance, and
     * thread, but not down to individual methods.
     * <p>
     * This builds a {@link ClassPrepareRequest}, filters it for the
     * given class name, and adds an {@link OnClassPrepare} handler that
     * creates a {@link BreakpointRequest} for the given method name.
     * The resulting BreakpointEvent is in turn handled by the
     * given handler.  Note that if you refer to an overloaded method,
     * all matching methods will have a breakpoint set and be handled by
     * the given handler.  For greater control, use
     * {@link #onMethodInvocation(String, String, String, OnBreakpoint)}
     *
     * @param className  A class name suitable for use by
     *                   {@link ClassPrepareRequest#addClassFilter(String)}
     * @param methodName A method name suitable for use by
     *                   {@link ReferenceType#methodsByName(String)}.  Must be
     *                   a method belonging to all classes matched by className.
     * @param handler    The callback to execute when the method is invoked.
     * @return The underlying {@link ChainingClassPrepareRequest}, which can
     *         be used to disable or delete the request later.
     */
    public ChainingClassPrepareRequest onMethodInvocation(final String className,
                                   final String methodName,
                                   final OnBreakpoint handler) {
        return onClassPrep(className, ev -> {
            ev.referenceType().methodsByName(methodName).forEach(m -> {
                // Abstract and native methods have no location
                if (m.location() != null) {
                    breakpointRequest(m.location(), handler).enable();
                }
            });
        });
    }

    /**
     * Identical to {@link #onMethodInvocation(String, String, OnBreakpoint)},
     * extending filtering to include a method signature.
     *
     * @param className  A class name suitable for use by
     *                   {@link ClassPrepareRequest#addClassFilter(String)}
     * @param methodName A method name suitable for use by
     *                   {@link ReferenceType#methodsByName(String)}.  Must be
     *                   a method belonging to all classes matched by className.
     * @param methodSig  A method signature suitable for use by
     *                   {@link ReferenceType#methodsByName(String, String)}.
     *                   Must be a method belonging to all classes matched by
     *                   className.
     * @param handler    The callback to execute when the method is invoked.
     * @return The underlying {@link ChainingClassPrepareRequest}, which can
     *         be used to disable or delete the request later.
     */
    public ChainingClassPrepareRequest onMethodInvocation(final String className,
                                   final String methodName,
                                   final String methodSig,
                                   final OnBreakpoint handler) {
        return onClassPrep(className, ev -> {
            ev.referenceType().methodsByName(methodName, methodSig).forEach(m -> {
                if (m.location() != null) {
                    breakpointRequest(m.location(), handler).enable();
                }
            });
        });
    }

    /**
     * Like {@link #onMethodInvocation(String, String, OnBreakpoint)} but with
     * an explicit suspend policy.
     * <p>
     * The default policy is {@link EventRequest#SUSPEND_ALL}, which stops every
     * thread on each hit — appropriate for cold paths but expensive on hot ones.
     * Pass {@link EventRequest#SUSPEND_NONE} to deliver events asynchronously
     * without pausing the target (ideal for counters and fire-and-forget logging)
     * or {@link EventRequest#SUSPEND_EVENT_THREAD} to pause only the hitting
     * thread (better for heterogeneous multi-threaded servers).
     * <p>
     * Example — count calls without pausing the target:
     * <pre>
     *   LongAdder counter = new LongAdder();
     *   j.onMethodInvocation("com.example.MyClass", "handleRequest",
     *       EventRequest.SUSPEND_NONE, e -&gt; counter.increment());
     * </pre>
     *
     * @param className     A class name suitable for use by
     *                      {@link ClassPrepareRequest#addClassFilter(String)}
     * @param methodName    A method name suitable for use by
     *                      {@link ReferenceType#methodsByName(String)}.
     * @param suspendPolicy One of {@link EventRequest#SUSPEND_ALL},
     *                      {@link EventRequest#SUSPEND_EVENT_THREAD}, or
     *                      {@link EventRequest#SUSPEND_NONE}.
     * @param handler       The callback to execute when the method is invoked.
     * @return The underlying {@link ChainingClassPrepareRequest}, which can
     *         be used to disable or delete the request later.
     */
    public ChainingClassPrepareRequest onMethodInvocation(final String className,
                                   final String methodName,
                                   final int suspendPolicy,
                                   final OnBreakpoint handler) {
        return onClassPrep(className, ev -> {
            ev.referenceType().methodsByName(methodName).forEach(m -> {
                if (m.location() != null) {
                    breakpointRequest(m.location(), handler)
                        .setSuspendPolicy(suspendPolicy)
                        .enable();
                }
            });
        });
    }

    /**
     * Like {@link #onMethodInvocation(String, String, String, OnBreakpoint)} but
     * with an explicit suspend policy.
     *
     * @param className     A class name suitable for use by
     *                      {@link ClassPrepareRequest#addClassFilter(String)}
     * @param methodName    A method name suitable for use by
     *                      {@link ReferenceType#methodsByName(String)}.
     * @param methodSig     A method signature suitable for use by
     *                      {@link ReferenceType#methodsByName(String, String)}.
     * @param suspendPolicy One of {@link EventRequest#SUSPEND_ALL},
     *                      {@link EventRequest#SUSPEND_EVENT_THREAD}, or
     *                      {@link EventRequest#SUSPEND_NONE}.
     * @param handler       The callback to execute when the method is invoked.
     * @return The underlying {@link ChainingClassPrepareRequest}, which can
     *         be used to disable or delete the request later.
     */
    public ChainingClassPrepareRequest onMethodInvocation(final String className,
                                   final String methodName,
                                   final String methodSig,
                                   final int suspendPolicy,
                                   final OnBreakpoint handler) {
        return onClassPrep(className, ev -> {
            ev.referenceType().methodsByName(methodName, methodSig).forEach(m -> {
                if (m.location() != null) {
                    breakpointRequest(m.location(), handler)
                        .setSuspendPolicy(suspendPolicy)
                        .enable();
                }
            });
        });
    }
    
    /**
     * Creates a breakpointRequest for the exit from the currently executing method 
     * on the given thread.
     * 
     * @param thread The thread currently executing the method.
     * @param handler The handler for the method exit breakpoint event.
     * @throws IncompatibleThreadStateException if the thread is not suspended in the target VM
     * @throws AbsentInformationException if there is no line number information for this method.
     */
    public void onCurrentMethodExit(final ThreadReference thread,
                                    final OnBreakpoint handler)
        throws IncompatibleThreadStateException, AbsentInformationException 
    {
        List<Location> locs = thread.frame(0).location().method().allLineLocations();
        Location last = locs.get(locs.size()-1);
        breakpointRequest(last, handler)
            .addInstanceFilter(thread.frame(0).thisObject())
            .enable();
    }

    /**
     * Create a stepRequest and enable it.
     * 
     * @param thread The thread to step.
     * @param size The step size.
     * @param depth The step depth.
     * @param handler The handler for the step event.
     */
    public void onStep(final ThreadReference thread,
    		           final int size,
    		           final int depth,
    		           final OnStep handler) {
    	stepRequest(thread, size, depth, handler).enable();
    }

    /**
     * onStep with size=StepRequest.STEP_MIN, depth=StepRequest.STEP_INTO
     * @param thread The thread to step into.
     * @param handler The handler for the step event.
     */
    public void onStepInto(final ThreadReference thread, 
    		               final OnStep handler) {
    	onStep(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO, handler);
    }
    
    /**
     * onStep with size=StepRequest.STEP_MIN, depth=StepRequest.STEP_OVER
     * @param thread The thread to step over.
     * @param handler The handler for the step event.
     */    
    public void onStepOver(final ThreadReference thread,
    					   final OnStep handler) {
    	onStep(thread, StepRequest.STEP_MIN, StepRequest.STEP_OVER, handler);
    }
    
    /**
     * onStep with size=StepRequest.STEP_MIN, depth=StepRequest.STEP_OUT
     * @param thread The thread to step out.
     * @param handler The handler for the step event.
     */    
    public void onStepOut(final ThreadReference thread,
    					  final OnStep handler) {
        onStep(thread, StepRequest.STEP_MIN, StepRequest.STEP_OUT, handler);
    }

    /**
     * Shortcut for the common pattern of responding to thread start events.
     * <p>
     * Builds a {@link ThreadStartRequest} and adds the given handler.
     *
     * @param handler    The callback to execute when a thread starts.
     */
    public void onThreadStart(final OnThreadStart handler) {
        threadStartRequest(handler).enable();
    }

    /**
     * Shortcut for the common pattern of responding to thread death events.
     * <p>
     * Builds a {@link ThreadDeathRequest} and adds the given handler.
     *
     * @param handler    The callback to execute when a thread dies.
     */
    public void onThreadDeath(final OnThreadDeath handler) {
        threadDeathRequest(handler).enable();
    }

    /**
     * Shortcut for the common pattern of tracking exceptions thrown
     * by application code.
     * <p>
     * Builds an {@link ExceptionRequest} for all exception types with
     * standard class exclusion filters for {@code java.*}, {@code sun.*},
     * and {@code jdk.*} packages, so that only exceptions thrown from
     * application code are reported.
     *
     * @param notifyCaught   true to report caught exceptions
     * @param notifyUncaught true to report uncaught exceptions
     * @param handler        The callback to execute when an exception is thrown.
     */
    public void onException(final boolean notifyCaught,
                            final boolean notifyUncaught,
                            final OnException handler) {
        exceptionRequest(null, notifyCaught, notifyUncaught, handler)
            .addClassExclusionFilter("java.*")
            .addClassExclusionFilter("sun.*")
            .addClassExclusionFilter("jdk.*")
            .enable();
    }

    /**
     * Shortcut for the common pattern of responding to a particular
     * method's exit.
     * <p>
     * Similar to {@link #onMethodInvocation(String, String, OnBreakpoint)},
     * but uses a {@link MethodExitRequest} filtered by class instead of
     * breakpoints.  The handler receives a {@link com.sun.jdi.event.MethodExitEvent}
     * which includes the return value.
     * <p>
     * Note that MethodExitRequests can only be filtered by class, not
     * by individual method.  The handler will be invoked for every method
     * exit in the matched class; filter by method name in your handler
     * if needed.
     *
     * @param className  A class name suitable for use by
     *                   {@link MethodExitRequest#addClassFilter(String)}
     * @param handler    The callback to execute when a method in the class exits.
     */
    public void onMethodExit(final String className,
                             final OnMethodExit handler) {
        methodExitRequest(handler)
            .addClassFilter(className)
            .enable();
    }

    /**
     * Like {@link #onCurrentMethodExit(ThreadReference, OnBreakpoint)} but
     * wraps checked exceptions in a {@link RuntimeException} instead of
     * declaring them.
     * <p>
     * This is the most common usage pattern &mdash; callers almost always
     * wrap {@code onCurrentMethodExit} in
     * {@link org.jdiscript.util.Utils#unchecked(org.jdiscript.util.Utils.Block)}.
     * This method inlines that wrapping for convenience.
     *
     * @param thread  The thread currently executing the method.
     * @param handler The handler for the method exit breakpoint event.
     * @throws RuntimeException wrapping {@link IncompatibleThreadStateException}
     *         or {@link AbsentInformationException}
     */
    public void onCurrentMethodExitUnchecked(final ThreadReference thread,
                                             final OnBreakpoint handler) {
        try {
            onCurrentMethodExit(thread, handler);
        } catch(IncompatibleThreadStateException | AbsentInformationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Walk the call stack of the given thread and return the {@link Location}
     * of the nearest frame whose declaring type starts with the given
     * package prefix, or {@code null} if no such frame is found.
     * <p>
     * This is useful for scoping analysis to application code.  For example,
     * when breaking on {@code java.lang.String.&lt;init&gt;}, you can call
     * {@code nearestCaller("com.myapp", thread)} to find which of your
     * classes triggered the String creation.
     *
     * @param packagePrefix  A package prefix (e.g. {@code "com.myapp"}).
     * @param thread         The suspended thread to inspect.
     * @return The location of the nearest matching frame, or {@code null}.
     * @throws RuntimeException wrapping {@link IncompatibleThreadStateException}
     */
    public Location nearestCaller(final String packagePrefix,
                                  final ThreadReference thread) {
        return nearestCaller(
            loc -> loc.declaringType().name().startsWith(packagePrefix),
            thread);
    }

    /**
     * Walk the call stack of the given thread and return the {@link Location}
     * of the nearest frame (starting from frame 1, skipping the current frame)
     * that matches the given predicate, or {@code null} if none matches.
     * <p>
     * This generalizes {@link #nearestCaller(String, ThreadReference)} to
     * support arbitrary filtering.  Common uses include exclusion-based
     * filtering to skip framework internals:
     * <pre>
     *   nearestCaller(loc -&gt; !loc.declaringType().name().startsWith("java."),
     *                 thread)
     * </pre>
     *
     * @param filter  A predicate that returns {@code true} for matching frames.
     * @param thread  The suspended thread to inspect.
     * @return The location of the nearest matching frame, or {@code null}.
     * @throws RuntimeException wrapping {@link IncompatibleThreadStateException}
     */
    public Location nearestCaller(final Predicate<Location> filter,
                                  final ThreadReference thread) {
        try {
            List<StackFrame> frames = thread.frames();
            for (int i = 1; i < frames.size(); i++) {
                Location loc = frames.get(i).location();
                if (filter.test(loc)) {
                    return loc;
                }
            }
            return null;
        } catch(IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build a string key representing the full call stack of the given
     * thread, suitable for use as a {@link java.util.Map} key in
     * histogram-style profiling.
     * <p>
     * The format is {@code "Type.method(line):Type.method(line):..."} from
     * top of stack to bottom.
     *
     * @param thread  The suspended thread to inspect.
     * @return A colon-separated string of stack frame locations.
     * @throws RuntimeException wrapping {@link IncompatibleThreadStateException}
     */
    public String stacktraceKey(final ThreadReference thread) {
        try {
            StringBuilder sb = new StringBuilder();
            for (StackFrame frame : thread.frames()) {
                Location loc = frame.location();
                if (sb.length() > 0) {
                    sb.append(":");
                }
                sb.append(loc.declaringType().name())
                  .append(".").append(loc.method().name())
                  .append("(").append(loc.lineNumber()).append(")");
            }
            return sb.toString();
        } catch(IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a handler that runs the given handler once and then disables
     * the event request that caused the handler to be invoked.
     *
     * @param <K> The type of the inner handler
     * @param handler  The inner handler
     * @return An instance of {@link Once} cast to the same type as the inner handler.
     */
    @SuppressWarnings("unchecked")
    public <K extends DebugEventHandler> K once(K handler) {
    	return (K)new Once(handler);
    }

    /**
     * Create a handler that fires the inner handler, then disables the
     * triggering event request and re-enables it after {@code intervalMs}
     * milliseconds.
     * <p>
     * This caps the event rate at roughly {@code 1000 / intervalMs} hits per
     * second regardless of how hot the target method is, retaining most of the
     * target's throughput while still delivering periodic samples.  A 100 ms
     * interval gives ~97.5% throughput even for a tight-loop breakpoint.
     * <p>
     * Example:
     * <pre>
     *   j.onMethodInvocation("com.example.MyClass", "hotMethod",
     *       j.sampled(100, e -&gt; System.out.println("sampled: " + e.location())));
     * </pre>
     *
     * @param <K>        The type of the inner handler.
     * @param intervalMs How long to suppress the request after each hit (ms).
     * @param handler    The inner handler to invoke on each sampled hit.
     * @return A {@link Sampled} wrapper cast to the same type as the inner handler.
     */
    @SuppressWarnings("unchecked")
    public <K extends DebugEventHandler> K sampled(long intervalMs, K handler) {
        return (K) new Sampled(handler, intervalMs);
    }
    
    public String fullName(Method method) {
    	final String refType = method.declaringType().name();
    	final String methName = method.name();
    	final String methSig = String.join(", ", method.argumentTypeNames());
    	return refType + "." + methName + "(" + methSig + ")";
    }

    // ---------------------------------------------------------------
    // Heap inspection
    // ---------------------------------------------------------------

    /**
     * Count the number of live instances of a class in the target VM.
     * <p>
     * Uses {@link VirtualMachine#instanceCounts(List)} which requires the VM
     * to have been started with {@code -XX:+EnableDynamicAgentLoading} or with
     * standard JDWP — both of which are always true when you are attached via
     * jdiscript.  Sums counts across all loaded classes matching the name (e.g.
     * if the same class appears in multiple class loaders).
     * <p>
     * Typical use: check whether objects are being retained unexpectedly.
     * <pre>
     *   long count = j.instanceCount("com.example.UserSession");
     *   System.out.println("Live sessions: " + count);
     * </pre>
     *
     * @param className Fully-qualified class name (e.g. {@code "com.example.Foo"}).
     * @return The total number of live instances, or 0 if the class is not loaded.
     */
    public long instanceCount(String className) {
        List<ReferenceType> types = vm.classesByName(className);
        if (types.isEmpty()) return 0;
        long[] counts = vm.instanceCounts(types);
        long total = 0;
        for (long c : counts) total += c;
        return total;
    }

    /**
     * Retrieve up to {@code maxCount} live instances of a class from the
     * target VM.
     * <p>
     * The returned references are valid only while the VM is suspended or
     * while the calling thread holds them.  Iterate promptly; do not store
     * them beyond the enclosing event handler.
     * <p>
     * Typical use: inspect the state of all live objects of a given type.
     * <pre>
     *   j.findInstances("com.example.Connection", 50).forEach(ref -> {
     *       String state = RemoteObject.invokeRemote(ref, "getState",
     *           "()Ljava/lang/String;", thread)
     *           .map(v -> RemoteObject.valueToString(v, thread))
     *           .orElse("?");
     *       System.out.println(ref.uniqueID() + ": " + state);
     *   });
     * </pre>
     *
     * @param className Fully-qualified class name.
     * @param maxCount  Maximum number of instances to return per loaded type.
     * @return A list of object references (may be empty if the class is not loaded).
     */
    public List<ObjectReference> findInstances(String className, long maxCount) {
        List<ReferenceType> types = vm.classesByName(className);
        List<ObjectReference> result = new ArrayList<>();
        for (ReferenceType type : types) {
            result.addAll(type.instances(maxCount));
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Entry/exit pairing
    // ---------------------------------------------------------------

    /**
     * Time a method invocation and deliver the duration to a callback.
     * <p>
     * Combines {@link #onMethodInvocation(String, String, OnBreakpoint)} with
     * {@link #onCurrentMethodExitUnchecked(ThreadReference, OnBreakpoint)} so
     * that the caller receives both the entry event and the wall-clock duration
     * of each call.  This is the recommended pattern for slow-call detection,
     * SLA monitoring, and profiling production code without modifying it.
     * <p>
     * <strong>Limitations:</strong> Uses an instance filter on {@code this}
     * to correlate entry with exit, so it works correctly for instance methods
     * only.  Static methods and constructors will throw at runtime (the exit
     * breakpoint requires a non-null {@code thisObject}).
     * <p>
     * Example — log calls slower than 100 ms:
     * <pre>
     *   j.onMethodTimed("com.example.UserService", "findUser",
     *       (entry, durationMs) -&gt; {
     *           if (durationMs &gt; 100) {
     *               System.out.println("SLOW findUser: " + durationMs + "ms"
     *                   + " caller=" + j.nearestCaller("com.example", entry.thread()));
     *           }
     *       });
     * </pre>
     *
     * @param className  Fully-qualified class name.
     * @param methodName Method name (all overloads are instrumented).
     * @param handler    Receives the entry event and the call duration in milliseconds.
     * @return The underlying {@link ChainingClassPrepareRequest}, which can be
     *         used to disable or delete the request later.
     */
    public ChainingClassPrepareRequest onMethodTimed(String className,
                                                      String methodName,
                                                      BiConsumer<BreakpointEvent, Long> handler) {
        return onMethodInvocation(className, methodName, entryEvent -> {
            long start = System.nanoTime();
            onCurrentMethodExitUnchecked(entryEvent.thread(), exit -> {
                handler.accept(entryEvent, (System.nanoTime() - start) / 1_000_000);
                deleteEventRequest(exit.request());
            });
        });
    }

}
