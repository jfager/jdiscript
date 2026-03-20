package org.jdiscript;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.jdiscript.handlers.*;
import org.jdiscript.requests.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.*;

/**
 * Unit tests for JDIScript using a mocked VirtualMachine + EventRequestManager.
 * These cover all request factory methods, filter/delete methods, convenience
 * handlers, and thread-inspection utilities without needing a live target JVM.
 */
class JDIScriptTest {

    private VirtualMachine vm;
    private EventRequestManager erm;
    private JDIScript j;

    @BeforeEach
    void setUp() {
        vm = mock(VirtualMachine.class);
        erm = mock(EventRequestManager.class);
        when(vm.eventRequestManager()).thenReturn(erm);
        j = new JDIScript(vm);
    }

    // ---------------------------------------------------------------
    // Basic accessors
    // ---------------------------------------------------------------

    @Test
    void vm_returns_the_virtual_machine() {
        assertSame(vm, j.vm());
    }

    // ---------------------------------------------------------------
    // once() and sampled() factory methods
    // ---------------------------------------------------------------

    @Test
    void once_wraps_handler_in_Once() {
        OnBreakpoint inner = e -> {};
        OnBreakpoint wrapped = j.once(inner);
        assertInstanceOf(Once.class, wrapped);
    }

    @Test
    void sampled_wraps_handler_in_Sampled() {
        OnBreakpoint inner = e -> {};
        OnBreakpoint wrapped = j.sampled(100, inner);
        assertInstanceOf(Sampled.class, wrapped);
    }

    // ---------------------------------------------------------------
    // Request factory methods
    // ---------------------------------------------------------------

    @Test
    void classPrepareRequest_no_handler_calls_erm() {
        ClassPrepareRequest req = mock(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        ChainingClassPrepareRequest result = j.classPrepareRequest();

        verify(erm).createClassPrepareRequest();
        assertNotNull(result);
    }

    @Test
    void classPrepareRequest_with_handler_registers_handler() {
        ClassPrepareRequest req = mock(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        OnClassPrepare handler = e -> {};
        j.classPrepareRequest(handler);

        verify(req).putProperty(any(), any());
    }

    @Test
    void classUnloadRequest_no_handler_calls_erm() {
        ClassUnloadRequest req = mock(ClassUnloadRequest.class);
        when(erm.createClassUnloadRequest()).thenReturn(req);

        assertNotNull(j.classUnloadRequest());
        verify(erm).createClassUnloadRequest();
    }

    @Test
    void classUnloadRequest_with_handler_registers_handler() {
        ClassUnloadRequest req = mock(ClassUnloadRequest.class);
        when(erm.createClassUnloadRequest()).thenReturn(req);

        j.classUnloadRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void exceptionRequest_no_handler_calls_erm() {
        ExceptionRequest req = mock(ExceptionRequest.class);
        when(erm.createExceptionRequest(null, true, false)).thenReturn(req);

        assertNotNull(j.exceptionRequest(null, true, false));
        verify(erm).createExceptionRequest(null, true, false);
    }

    @Test
    void exceptionRequest_with_handler_registers_handler() {
        ExceptionRequest req = mock(ExceptionRequest.class);
        when(erm.createExceptionRequest(null, true, false)).thenReturn(req);

        j.exceptionRequest(null, true, false, e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void methodEntryRequest_no_handler_calls_erm() {
        MethodEntryRequest req = mock(MethodEntryRequest.class);
        when(erm.createMethodEntryRequest()).thenReturn(req);

        assertNotNull(j.methodEntryRequest());
        verify(erm).createMethodEntryRequest();
    }

    @Test
    void methodEntryRequest_with_handler_registers_handler() {
        MethodEntryRequest req = mock(MethodEntryRequest.class);
        when(erm.createMethodEntryRequest()).thenReturn(req);

        j.methodEntryRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void methodExitRequest_no_handler_calls_erm() {
        MethodExitRequest req = mock(MethodExitRequest.class);
        when(erm.createMethodExitRequest()).thenReturn(req);

        assertNotNull(j.methodExitRequest());
        verify(erm).createMethodExitRequest();
    }

    @Test
    void methodExitRequest_with_handler_registers_handler() {
        MethodExitRequest req = mock(MethodExitRequest.class);
        when(erm.createMethodExitRequest()).thenReturn(req);

        j.methodExitRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void monitorWaitRequest_no_handler_calls_erm() {
        MonitorWaitRequest req = mock(MonitorWaitRequest.class);
        when(erm.createMonitorWaitRequest()).thenReturn(req);

        assertNotNull(j.monitorWaitRequest());
        verify(erm).createMonitorWaitRequest();
    }

    @Test
    void monitorWaitRequest_with_handler_registers_handler() {
        MonitorWaitRequest req = mock(MonitorWaitRequest.class);
        when(erm.createMonitorWaitRequest()).thenReturn(req);

        j.monitorWaitRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void monitorWaitedRequest_no_handler_calls_erm() {
        MonitorWaitedRequest req = mock(MonitorWaitedRequest.class);
        when(erm.createMonitorWaitedRequest()).thenReturn(req);

        assertNotNull(j.monitorWaitedRequest());
        verify(erm).createMonitorWaitedRequest();
    }

    @Test
    void monitorWaitedRequest_with_handler_registers_handler() {
        MonitorWaitedRequest req = mock(MonitorWaitedRequest.class);
        when(erm.createMonitorWaitedRequest()).thenReturn(req);

        j.monitorWaitedRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void monitorContendedEnterRequest_no_handler_calls_erm() {
        MonitorContendedEnterRequest req = mock(MonitorContendedEnterRequest.class);
        when(erm.createMonitorContendedEnterRequest()).thenReturn(req);

        assertNotNull(j.monitorContendedEnterRequest());
        verify(erm).createMonitorContendedEnterRequest();
    }

    @Test
    void monitorContendedEnterRequest_with_handler_registers_handler() {
        MonitorContendedEnterRequest req = mock(MonitorContendedEnterRequest.class);
        when(erm.createMonitorContendedEnterRequest()).thenReturn(req);

        j.monitorContendedEnterRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void monitorContendedEnteredRequest_no_handler_calls_erm() {
        MonitorContendedEnteredRequest req = mock(MonitorContendedEnteredRequest.class);
        when(erm.createMonitorContendedEnteredRequest()).thenReturn(req);

        assertNotNull(j.monitorContendedEnteredRequest());
        verify(erm).createMonitorContendedEnteredRequest();
    }

    @Test
    void monitorContendedEnteredRequest_with_handler_registers_handler() {
        MonitorContendedEnteredRequest req = mock(MonitorContendedEnteredRequest.class);
        when(erm.createMonitorContendedEnteredRequest()).thenReturn(req);

        j.monitorContendedEnteredRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void threadDeathRequest_no_handler_calls_erm() {
        ThreadDeathRequest req = mock(ThreadDeathRequest.class);
        when(erm.createThreadDeathRequest()).thenReturn(req);

        assertNotNull(j.threadDeathRequest());
        verify(erm).createThreadDeathRequest();
    }

    @Test
    void threadDeathRequest_with_handler_registers_handler() {
        ThreadDeathRequest req = mock(ThreadDeathRequest.class);
        when(erm.createThreadDeathRequest()).thenReturn(req);

        j.threadDeathRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void threadStartRequest_no_handler_calls_erm() {
        ThreadStartRequest req = mock(ThreadStartRequest.class);
        when(erm.createThreadStartRequest()).thenReturn(req);

        assertNotNull(j.threadStartRequest());
        verify(erm).createThreadStartRequest();
    }

    @Test
    void threadStartRequest_with_handler_registers_handler() {
        ThreadStartRequest req = mock(ThreadStartRequest.class);
        when(erm.createThreadStartRequest()).thenReturn(req);

        j.threadStartRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void vmDeathRequest_no_handler_calls_erm() {
        VMDeathRequest req = mock(VMDeathRequest.class);
        when(erm.createVMDeathRequest()).thenReturn(req);

        assertNotNull(j.vmDeathRequest());
        verify(erm).createVMDeathRequest();
    }

    @Test
    void vmDeathRequest_with_handler_registers_handler() {
        VMDeathRequest req = mock(VMDeathRequest.class);
        when(erm.createVMDeathRequest()).thenReturn(req);

        j.vmDeathRequest(e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void stepRequest_no_handler_calls_erm() {
        ThreadReference thread = mock(ThreadReference.class);
        StepRequest req = mock(StepRequest.class);
        when(erm.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO)).thenReturn(req);

        assertNotNull(j.stepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO));
        verify(erm).createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO);
    }

    @Test
    void stepRequest_with_handler_registers_handler() {
        ThreadReference thread = mock(ThreadReference.class);
        StepRequest req = mock(StepRequest.class);
        when(erm.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_OVER)).thenReturn(req);

        j.stepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_OVER, e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void accessWatchpointRequest_no_handler_calls_erm() {
        Field field = mock(Field.class);
        AccessWatchpointRequest req = mock(AccessWatchpointRequest.class);
        when(erm.createAccessWatchpointRequest(field)).thenReturn(req);

        assertNotNull(j.accessWatchpointRequest(field));
        verify(erm).createAccessWatchpointRequest(field);
    }

    @Test
    void accessWatchpointRequest_with_handler_registers_handler() {
        Field field = mock(Field.class);
        AccessWatchpointRequest req = mock(AccessWatchpointRequest.class);
        when(erm.createAccessWatchpointRequest(field)).thenReturn(req);

        j.accessWatchpointRequest(field, e -> {});

        verify(req).putProperty(any(), any());
    }

    @Test
    void modificationWatchpointRequest_no_handler_calls_erm() {
        Field field = mock(Field.class);
        ModificationWatchpointRequest req = mock(ModificationWatchpointRequest.class);
        when(erm.createModificationWatchpointRequest(field)).thenReturn(req);

        assertNotNull(j.modificationWatchpointRequest(field));
        verify(erm).createModificationWatchpointRequest(field);
    }

    @Test
    void modificationWatchpointRequest_with_handler_registers_handler() {
        Field field = mock(Field.class);
        ModificationWatchpointRequest req = mock(ModificationWatchpointRequest.class);
        when(erm.createModificationWatchpointRequest(field)).thenReturn(req);

        j.modificationWatchpointRequest(field, e -> {});

        verify(req).putProperty(any(), any());
    }

    // ---------------------------------------------------------------
    // Filter methods (*requests(handler))
    // ---------------------------------------------------------------

    // A concrete DebugEventHandler for use in filter tests (marker interface, not functional)
    private static final OnBreakpoint DUMMY_HANDLER = e -> {};

    /** Mock request that actually stores/retrieves properties (needed for handler registration). */
    private static <T extends EventRequest> T mockRequestWithProps(Class<T> type) {
        T req = mock(type);
        Map<Object, Object> props = new HashMap<>();
        doAnswer(inv -> { props.put(inv.getArgument(0), inv.getArgument(1)); return null; })
            .when(req).putProperty(any(), any());
        when(req.getProperty(any())).thenAnswer(inv -> props.get(inv.getArgument(0)));
        return req;
    }

    @Test
    void accessWatchpointRequests_delegates_to_erm_and_filters() {
        when(erm.accessWatchpointRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.accessWatchpointRequests(DUMMY_HANDLER));
    }

    @Test
    void breakpointRequests_delegates_to_erm_and_filters() {
        when(erm.breakpointRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.breakpointRequests(DUMMY_HANDLER));
    }

    @Test
    void classPrepareRequests_delegates_to_erm_and_filters() {
        when(erm.classPrepareRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.classPrepareRequests(DUMMY_HANDLER));
    }

    @Test
    void classUnloadRequests_delegates_to_erm_and_filters() {
        when(erm.classUnloadRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.classUnloadRequests(DUMMY_HANDLER));
    }

    @Test
    void exceptionRequests_delegates_to_erm_and_filters() {
        when(erm.exceptionRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.exceptionRequests(DUMMY_HANDLER));
    }

    @Test
    void methodEntryRequests_delegates_to_erm_and_filters() {
        when(erm.methodEntryRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.methodEntryRequests(DUMMY_HANDLER));
    }

    @Test
    void methodExitRequests_delegates_to_erm_and_filters() {
        when(erm.methodExitRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.methodExitRequests(DUMMY_HANDLER));
    }

    @Test
    void modificationWatchpointRequests_delegates_to_erm_and_filters() {
        when(erm.modificationWatchpointRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.modificationWatchpointRequests(DUMMY_HANDLER));
    }

    @Test
    void monitorContendedEnteredRequests_delegates_to_erm_and_filters() {
        when(erm.monitorContendedEnteredRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.monitorContendedEnteredRequests(DUMMY_HANDLER));
    }

    @Test
    void monitorContendedEnterRequests_delegates_to_erm_and_filters() {
        when(erm.monitorContendedEnterRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.monitorContendedEnterRequests(DUMMY_HANDLER));
    }

    @Test
    void monitorWaitedRequests_delegates_to_erm_and_filters() {
        when(erm.monitorWaitedRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.monitorWaitedRequests(DUMMY_HANDLER));
    }

    @Test
    void monitorWaitRequests_delegates_to_erm_and_filters() {
        when(erm.monitorWaitRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.monitorWaitRequests(DUMMY_HANDLER));
    }

    @Test
    void stepRequests_delegates_to_erm_and_filters() {
        when(erm.stepRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.stepRequests(DUMMY_HANDLER));
    }

    @Test
    void threadDeathRequests_delegates_to_erm_and_filters() {
        when(erm.threadDeathRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.threadDeathRequests(DUMMY_HANDLER));
    }

    @Test
    void threadStartRequests_delegates_to_erm_and_filters() {
        when(erm.threadStartRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.threadStartRequests(DUMMY_HANDLER));
    }

    @Test
    void vmDeathRequests_delegates_to_erm_and_filters() {
        when(erm.vmDeathRequests()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), j.vmDeathRequests(DUMMY_HANDLER));
    }

    @Test
    void filter_returns_request_whose_handler_matches() {
        ClassPrepareRequest req = mockRequestWithProps(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        OnClassPrepare handler = e -> {};
        j.classPrepareRequest(handler);

        when(erm.classPrepareRequests()).thenReturn(List.of(req));
        List<ClassPrepareRequest> found = j.classPrepareRequests(handler);
        assertEquals(1, found.size());
        assertSame(req, found.get(0));
    }

    @Test
    void filter_does_not_return_request_for_different_handler() {
        ClassPrepareRequest req = mockRequestWithProps(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        OnClassPrepare handler1 = e -> {};
        OnClassPrepare handler2 = e -> {};
        j.classPrepareRequest(handler1);

        when(erm.classPrepareRequests()).thenReturn(List.of(req));
        List<ClassPrepareRequest> found = j.classPrepareRequests(handler2);
        assertTrue(found.isEmpty());
    }

    // ---------------------------------------------------------------
    // Delete methods
    // ---------------------------------------------------------------

    @Test
    void deleteEventRequest_delegates_to_erm() {
        EventRequest req = mock(EventRequest.class);
        j.deleteEventRequest(req);
        verify(erm).deleteEventRequest(req);
    }

    @Test
    void deleteEventRequests_delegates_to_erm() {
        EventRequest req = mock(EventRequest.class);
        List<EventRequest> reqs = List.of(req);
        j.deleteEventRequests(reqs);
        verify(erm).deleteEventRequests(reqs);
    }

    // ---------------------------------------------------------------
    // Convenience handler methods (on*)
    // ---------------------------------------------------------------

    @Test
    void onClassPrep_no_filter_creates_and_enables_classPrepareRequest() {
        ClassPrepareRequest req = mock(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        j.onClassPrep(e -> {});

        verify(req).enable();
    }

    @Test
    void onThreadStart_creates_and_enables_threadStartRequest() {
        ThreadStartRequest req = mock(ThreadStartRequest.class);
        when(erm.createThreadStartRequest()).thenReturn(req);

        j.onThreadStart(e -> {});

        verify(req).enable();
    }

    @Test
    void onThreadDeath_creates_and_enables_threadDeathRequest() {
        ThreadDeathRequest req = mock(ThreadDeathRequest.class);
        when(erm.createThreadDeathRequest()).thenReturn(req);

        j.onThreadDeath(e -> {});

        verify(req).enable();
    }

    @Test
    void onException_creates_exception_request_and_enables() {
        ExceptionRequest req = mock(ExceptionRequest.class);
        when(erm.createExceptionRequest(null, true, true)).thenReturn(req);

        j.onException(true, true, e -> {});

        verify(req).enable();
    }

    @Test
    void onMethodExit_creates_methodExit_request_and_enables() {
        MethodExitRequest req = mock(MethodExitRequest.class);
        when(erm.createMethodExitRequest()).thenReturn(req);

        j.onMethodExit("com.example.Foo", e -> {});

        verify(req).enable();
    }

    @Test
    void onFieldAccess_creates_classPrepare_request_and_enables() {
        ClassPrepareRequest req = mock(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        j.onFieldAccess("com.example.Foo", "myField", e -> {});

        verify(req).enable();
    }

    @Test
    void onFieldModification_creates_classPrepare_request_and_enables() {
        ClassPrepareRequest req = mock(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        j.onFieldModification("com.example.Foo", "myField", e -> {});

        verify(req).enable();
    }

    @Test
    void onMethodInvocation_by_name_creates_classPrepare_request_and_enables() {
        ClassPrepareRequest req = mock(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        j.onMethodInvocation("com.example.Foo", "doSomething", e -> {});

        verify(req).enable();
    }

    @Test
    void onMethodInvocation_with_signature_creates_classPrepare_request_and_enables() {
        ClassPrepareRequest req = mock(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        j.onMethodInvocation("com.example.Foo", "doSomething", "()V", e -> {});

        verify(req).enable();
    }

    @Test
    void onMethodInvocation_with_suspendPolicy_creates_classPrepare_request_and_enables() {
        ClassPrepareRequest req = mock(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        j.onMethodInvocation("com.example.Foo", "doSomething",
            EventRequest.SUSPEND_NONE, e -> {});

        verify(req).enable();
    }

    @Test
    void onMethodInvocation_with_sig_and_suspendPolicy_creates_classPrepare_request_and_enables() {
        ClassPrepareRequest req = mock(ClassPrepareRequest.class);
        when(erm.createClassPrepareRequest()).thenReturn(req);

        j.onMethodInvocation("com.example.Foo", "doSomething", "()V",
            EventRequest.SUSPEND_NONE, e -> {});

        verify(req).enable();
    }

    // ---------------------------------------------------------------
    // onStep* convenience methods
    // ---------------------------------------------------------------

    @Test
    void onStep_creates_and_enables_stepRequest() {
        ThreadReference thread = mock(ThreadReference.class);
        StepRequest req = mock(StepRequest.class);
        when(erm.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO)).thenReturn(req);

        j.onStep(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO, e -> {});

        verify(req).enable();
    }

    @Test
    void onStepInto_creates_stepRequest_with_STEP_INTO() {
        ThreadReference thread = mock(ThreadReference.class);
        StepRequest req = mock(StepRequest.class);
        when(erm.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO)).thenReturn(req);

        j.onStepInto(thread, e -> {});

        verify(erm).createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO);
        verify(req).enable();
    }

    @Test
    void onStepOver_creates_stepRequest_with_STEP_OVER() {
        ThreadReference thread = mock(ThreadReference.class);
        StepRequest req = mock(StepRequest.class);
        when(erm.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_OVER)).thenReturn(req);

        j.onStepOver(thread, e -> {});

        verify(erm).createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_OVER);
        verify(req).enable();
    }

    @Test
    void onStepOut_creates_stepRequest_with_STEP_OUT() {
        ThreadReference thread = mock(ThreadReference.class);
        StepRequest req = mock(StepRequest.class);
        when(erm.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_OUT)).thenReturn(req);

        j.onStepOut(thread, e -> {});

        verify(erm).createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_OUT);
        verify(req).enable();
    }

    // ---------------------------------------------------------------
    // nearestCaller
    // ---------------------------------------------------------------

    @Test
    void nearestCaller_by_prefix_returns_null_when_no_matching_frame() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        StackFrame frame0 = mock(StackFrame.class);
        Location loc0 = mock(Location.class);
        ReferenceType type0 = mock(ReferenceType.class);
        when(thread.frames()).thenReturn(List.of(frame0));
        when(frame0.location()).thenReturn(loc0);
        when(loc0.declaringType()).thenReturn(type0);
        when(type0.name()).thenReturn("java.lang.String");

        assertNull(j.nearestCaller("com.example", thread));
    }

    @Test
    void nearestCaller_by_prefix_skips_frame0_and_returns_first_match() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        StackFrame frame0 = mock(StackFrame.class);
        StackFrame frame1 = mock(StackFrame.class);
        Location loc0 = mock(Location.class);
        Location loc1 = mock(Location.class);
        ReferenceType type0 = mock(ReferenceType.class);
        ReferenceType type1 = mock(ReferenceType.class);
        when(thread.frames()).thenReturn(List.of(frame0, frame1));
        when(frame0.location()).thenReturn(loc0);
        when(frame1.location()).thenReturn(loc1);
        when(loc0.declaringType()).thenReturn(type0);
        when(loc1.declaringType()).thenReturn(type1);
        when(type0.name()).thenReturn("java.lang.String");
        when(type1.name()).thenReturn("com.example.MyClass");

        assertSame(loc1, j.nearestCaller("com.example", thread));
    }

    @Test
    void nearestCaller_by_predicate_returns_null_when_stack_is_empty() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        when(thread.frames()).thenReturn(Collections.emptyList());

        assertNull(j.nearestCaller(loc -> true, thread));
    }

    @Test
    void nearestCaller_by_predicate_returns_first_frame_matching_predicate() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        StackFrame frame0 = mock(StackFrame.class);
        StackFrame frame1 = mock(StackFrame.class);
        Location loc0 = mock(Location.class);
        Location loc1 = mock(Location.class);
        when(thread.frames()).thenReturn(List.of(frame0, frame1));
        when(frame0.location()).thenReturn(loc0);
        when(frame1.location()).thenReturn(loc1);

        // Predicate matches everything; frame0 is skipped (index 0), so frame1 (index 1) matches
        assertSame(loc1, j.nearestCaller(loc -> true, thread));
    }

    @Test
    void nearestCaller_wraps_IncompatibleThreadStateException_in_RuntimeException() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        when(thread.frames()).thenThrow(new IncompatibleThreadStateException());

        assertThrows(RuntimeException.class, () -> j.nearestCaller("com.example", thread));
    }

    // ---------------------------------------------------------------
    // stacktraceKey
    // ---------------------------------------------------------------

    @Test
    void stacktraceKey_formats_single_frame() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        StackFrame frame = mock(StackFrame.class);
        Location loc = mock(Location.class);
        ReferenceType type = mock(ReferenceType.class);
        Method method = mock(Method.class);
        when(thread.frames()).thenReturn(List.of(frame));
        when(frame.location()).thenReturn(loc);
        when(loc.declaringType()).thenReturn(type);
        when(type.name()).thenReturn("com.example.Foo");
        when(loc.method()).thenReturn(method);
        when(method.name()).thenReturn("bar");
        when(loc.lineNumber()).thenReturn(42);

        assertEquals("com.example.Foo.bar(42)", j.stacktraceKey(thread));
    }

    @Test
    void stacktraceKey_separates_frames_with_colon() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        StackFrame f1 = mock(StackFrame.class);
        StackFrame f2 = mock(StackFrame.class);
        Location loc1 = mock(Location.class);
        Location loc2 = mock(Location.class);
        ReferenceType type1 = mock(ReferenceType.class);
        ReferenceType type2 = mock(ReferenceType.class);
        Method m1 = mock(Method.class);
        Method m2 = mock(Method.class);
        when(thread.frames()).thenReturn(List.of(f1, f2));
        when(f1.location()).thenReturn(loc1);
        when(f2.location()).thenReturn(loc2);
        when(loc1.declaringType()).thenReturn(type1);
        when(loc2.declaringType()).thenReturn(type2);
        when(type1.name()).thenReturn("com.example.A");
        when(type2.name()).thenReturn("com.example.B");
        when(loc1.method()).thenReturn(m1);
        when(loc2.method()).thenReturn(m2);
        when(m1.name()).thenReturn("foo");
        when(m2.name()).thenReturn("bar");
        when(loc1.lineNumber()).thenReturn(10);
        when(loc2.lineNumber()).thenReturn(20);

        assertEquals("com.example.A.foo(10):com.example.B.bar(20)", j.stacktraceKey(thread));
    }

    @Test
    void stacktraceKey_wraps_IncompatibleThreadStateException_in_RuntimeException() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        when(thread.frames()).thenThrow(new IncompatibleThreadStateException());

        assertThrows(RuntimeException.class, () -> j.stacktraceKey(thread));
    }

    // ---------------------------------------------------------------
    // printTrace
    // ---------------------------------------------------------------

    @Test
    void printTrace_with_stream_prints_thread_info_and_frames() throws Exception {
        LocatableEvent event = mock(LocatableEvent.class);
        ThreadReference thread = mock(ThreadReference.class);
        StackFrame frame = mock(StackFrame.class);
        Location loc = mock(Location.class);
        ReferenceType threadType = mock(ReferenceType.class);
        when(event.thread()).thenReturn(thread);
        when(thread.type()).thenReturn(threadType);
        when(threadType.name()).thenReturn("java.lang.Thread");
        when(thread.name()).thenReturn("main");
        when(thread.uniqueID()).thenReturn(1L);
        when(thread.frames()).thenReturn(List.of(frame));
        when(frame.location()).thenReturn(loc);
        when(loc.toString()).thenReturn("com.example.Foo:42");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        j.printTrace(event, "my message", new PrintStream(baos));
        String output = baos.toString();

        assertTrue(output.contains("main"), "should contain thread name");
        assertTrue(output.contains("my message"), "should contain message");
        assertTrue(output.contains("com.example.Foo:42"), "should contain frame location");
    }

    @Test
    void printTrace_with_null_msg_does_not_throw() throws Exception {
        LocatableEvent event = mock(LocatableEvent.class);
        ThreadReference thread = mock(ThreadReference.class);
        ReferenceType threadType = mock(ReferenceType.class);
        when(event.thread()).thenReturn(thread);
        when(thread.type()).thenReturn(threadType);
        when(threadType.name()).thenReturn("java.lang.Thread");
        when(thread.name()).thenReturn("main");
        when(thread.uniqueID()).thenReturn(1L);
        when(thread.frames()).thenReturn(Collections.emptyList());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> j.printTrace(event, null, new PrintStream(baos)));
    }

    @Test
    void printTrace_handles_IncompatibleThreadStateException() throws Exception {
        LocatableEvent event = mock(LocatableEvent.class);
        ThreadReference thread = mock(ThreadReference.class);
        ReferenceType threadType = mock(ReferenceType.class);
        when(event.thread()).thenReturn(thread);
        when(thread.type()).thenReturn(threadType);
        when(threadType.name()).thenReturn("java.lang.Thread");
        when(thread.name()).thenReturn("main");
        when(thread.uniqueID()).thenReturn(1L);
        when(thread.frames()).thenThrow(new IncompatibleThreadStateException());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> j.printTrace(event, "msg", new PrintStream(baos)));
        assertTrue(baos.toString().contains("IncompatibleThreadStateException"));
    }
}
