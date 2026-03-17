package org.jdiscript.events;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jdiscript.handlers.*;
import org.junit.jupiter.api.Test;

import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;

class DebugEventDispatcherTest {

    /**
     * Create a mock EventRequest that actually stores/retrieves properties,
     * since DebugEventDispatcher uses putProperty/getProperty to attach handlers.
     */
    private static EventRequest mockRequest() {
        EventRequest request = mock(EventRequest.class);
        Map<Object, Object> props = new HashMap<>();
        doAnswer(inv -> {
            props.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(request).putProperty(any(), any());
        when(request.getProperty(any())).thenAnswer(inv -> props.get(inv.getArgument(0)));
        return request;
    }

    // --- Request-based dispatch (doFullDispatch) ---

    @Test
    void dispatches_BreakpointEvent_to_OnBreakpoint_handler() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        OnBreakpoint handler = e -> called.set(true);
        DebugEventDispatcher.addHandler(request, handler);

        new DebugEventDispatcher().dispatch(event);
        assertTrue(called.get());
    }

    @Test
    void dispatches_StepEvent_to_OnStep_handler() {
        StepEvent event = mock(StepEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        OnStep handler = e -> called.set(true);
        DebugEventDispatcher.addHandler(request, handler);

        new DebugEventDispatcher().dispatch(event);
        assertTrue(called.get());
    }

    @Test
    void dispatches_ExceptionEvent_to_OnException_handler() {
        ExceptionEvent event = mock(ExceptionEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        OnException handler = e -> called.set(true);
        DebugEventDispatcher.addHandler(request, handler);

        new DebugEventDispatcher().dispatch(event);
        assertTrue(called.get());
    }

    @Test
    void dispatches_MethodEntryEvent_to_OnMethodEntry_handler() {
        MethodEntryEvent event = mock(MethodEntryEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        OnMethodEntry handler = e -> called.set(true);
        DebugEventDispatcher.addHandler(request, handler);

        new DebugEventDispatcher().dispatch(event);
        assertTrue(called.get());
    }

    @Test
    void dispatches_MethodExitEvent_to_OnMethodExit_handler() {
        MethodExitEvent event = mock(MethodExitEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        OnMethodExit handler = e -> called.set(true);
        DebugEventDispatcher.addHandler(request, handler);

        new DebugEventDispatcher().dispatch(event);
        assertTrue(called.get());
    }

    @Test
    void dispatches_ClassPrepareEvent_to_OnClassPrepare_handler() {
        ClassPrepareEvent event = mock(ClassPrepareEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        OnClassPrepare handler = e -> called.set(true);
        DebugEventDispatcher.addHandler(request, handler);

        new DebugEventDispatcher().dispatch(event);
        assertTrue(called.get());
    }

    @Test
    void dispatches_ThreadStartEvent_to_OnThreadStart_handler() {
        ThreadStartEvent event = mock(ThreadStartEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        OnThreadStart handler = e -> called.set(true);
        DebugEventDispatcher.addHandler(request, handler);

        new DebugEventDispatcher().dispatch(event);
        assertTrue(called.get());
    }

    @Test
    void dispatches_ThreadDeathEvent_to_OnThreadDeath_handler() {
        ThreadDeathEvent event = mock(ThreadDeathEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        OnThreadDeath handler = e -> called.set(true);
        DebugEventDispatcher.addHandler(request, handler);

        new DebugEventDispatcher().dispatch(event);
        assertTrue(called.get());
    }

    // --- VM events (no request, dispatched to global handlers) ---

    @Test
    void dispatches_VMStartEvent_to_global_OnVMStart_handler() {
        VMStartEvent event = mock(VMStartEvent.class);
        when(event.request()).thenReturn(null);

        AtomicBoolean called = new AtomicBoolean();
        OnVMStart handler = e -> called.set(true);

        DebugEventDispatcher dispatcher = new DebugEventDispatcher();
        dispatcher.addHandler(handler);
        dispatcher.dispatch(event);
        assertTrue(called.get());
    }

    @Test
    void dispatches_VMDeathEvent_to_global_OnVMDeath_handler() {
        VMDeathEvent event = mock(VMDeathEvent.class);
        when(event.request()).thenReturn(null);

        AtomicBoolean called = new AtomicBoolean();
        OnVMDeath handler = e -> called.set(true);

        DebugEventDispatcher dispatcher = new DebugEventDispatcher();
        dispatcher.addHandler(handler);
        dispatcher.dispatch(event);
        assertTrue(called.get());
    }

    @Test
    void dispatches_VMDisconnectEvent_to_global_OnVMDisconnect_handler() {
        VMDisconnectEvent event = mock(VMDisconnectEvent.class);
        when(event.request()).thenReturn(null);

        AtomicBoolean called = new AtomicBoolean();
        OnVMDisconnect handler = e -> called.set(true);

        DebugEventDispatcher dispatcher = new DebugEventDispatcher();
        dispatcher.addHandler(handler);
        dispatcher.dispatch(event);
        assertTrue(called.get());
    }

    // --- Handler management ---

    @Test
    void addHandler_returns_true_for_new_handler() {
        EventRequest request = mockRequest();
        OnBreakpoint handler = e -> {};
        assertTrue(DebugEventDispatcher.addHandler(request, handler));
    }

    @Test
    void addHandler_returns_false_for_duplicate_handler() {
        EventRequest request = mockRequest();
        OnBreakpoint handler = e -> {};
        DebugEventDispatcher.addHandler(request, handler);
        assertFalse(DebugEventDispatcher.addHandler(request, handler));
    }

    @Test
    void getHandlers_returns_empty_set_for_no_handlers() {
        EventRequest request = mockRequest();
        assertTrue(DebugEventDispatcher.getHandlers(request).isEmpty());
    }

    @Test
    void multiple_handlers_on_same_request_all_get_called() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        AtomicBoolean called1 = new AtomicBoolean();
        AtomicBoolean called2 = new AtomicBoolean();
        DebugEventDispatcher.addHandler(request, (OnBreakpoint) e -> called1.set(true));
        DebugEventDispatcher.addHandler(request, (OnBreakpoint) e -> called2.set(true));

        new DebugEventDispatcher().dispatch(event);
        assertTrue(called1.get());
        assertTrue(called2.get());
    }

    // --- Mismatched handler type is not called ---

    @Test
    void wrong_handler_type_falls_through_to_catchall() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mockRequest();
        when(event.request()).thenReturn(request);

        // Register a StepEvent handler for a BreakpointEvent — the dispatcher's
        // if-else chain won't match OnStep for a BreakpointEvent, so it falls
        // through to the OnEvent catchall which tries to cast and throws.
        OnStep handler = e -> {};
        DebugEventDispatcher.addHandler(request, handler);

        assertThrows(ClassCastException.class, () ->
            new DebugEventDispatcher().dispatch(event)
        );
    }
}
