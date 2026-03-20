package org.jdiscript.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdiscript.events.DebugEventDispatcher;
import org.junit.jupiter.api.Test;

import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.request.EventRequest;

class OnceTest {

    @Test
    void fires_inner_handler_on_event() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mock(EventRequest.class);
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        Once once = new Once((OnBreakpoint) e -> called.set(true));
        DebugEventDispatcher.doFullDispatch(event, once);

        assertTrue(called.get(), "inner handler should be called");
    }

    @Test
    void disables_request_after_firing() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mock(EventRequest.class);
        when(event.request()).thenReturn(request);

        Once once = new Once((OnBreakpoint) e -> {});
        DebugEventDispatcher.doFullDispatch(event, once);

        verify(request).disable();
    }

    @Test
    void inner_handler_receives_the_event() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mock(EventRequest.class);
        when(event.request()).thenReturn(request);

        AtomicBoolean receivedCorrectEvent = new AtomicBoolean();
        Once once = new Once((OnBreakpoint) e -> receivedCorrectEvent.set(e == event));
        DebugEventDispatcher.doFullDispatch(event, once);

        assertTrue(receivedCorrectEvent.get());
    }

    @Test
    void fires_each_time_it_is_dispatched_to() {
        // Once disables the request but doesn't unregister itself, so if the
        // request were somehow re-enabled and fired again, the handler would
        // fire again. We verify it calls the inner handler each dispatch.
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mock(EventRequest.class);
        when(event.request()).thenReturn(request);

        AtomicInteger count = new AtomicInteger();
        Once once = new Once((OnBreakpoint) e -> count.incrementAndGet());
        DebugEventDispatcher.doFullDispatch(event, once);
        DebugEventDispatcher.doFullDispatch(event, once);

        assertEquals(2, count.get());
    }
}
