package org.jdiscript.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jdiscript.events.DebugEventDispatcher;
import org.junit.jupiter.api.Test;

import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.request.EventRequest;

class SampledTest {

    @Test
    void fires_inner_handler() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mock(EventRequest.class);
        when(event.request()).thenReturn(request);

        AtomicBoolean called = new AtomicBoolean();
        Sampled sampled = new Sampled((OnBreakpoint) e -> called.set(true), 50);
        DebugEventDispatcher.doFullDispatch(event, sampled);

        assertTrue(called.get());
    }

    @Test
    void disables_request_immediately() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mock(EventRequest.class);
        when(event.request()).thenReturn(request);

        Sampled sampled = new Sampled((OnBreakpoint) e -> {}, 50);
        DebugEventDispatcher.doFullDispatch(event, sampled);

        verify(request).disable();
    }

    @Test
    void re_enables_request_after_interval() throws InterruptedException {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mock(EventRequest.class);
        when(event.request()).thenReturn(request);

        Sampled sampled = new Sampled((OnBreakpoint) e -> {}, 50);
        DebugEventDispatcher.doFullDispatch(event, sampled);

        // Not yet re-enabled immediately after dispatch
        verify(request, never()).enable();

        // Wait long enough for the virtual thread to re-enable it
        Thread.sleep(300);
        verify(request).enable();
    }

    @Test
    void inner_handler_receives_the_event() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        EventRequest request = mock(EventRequest.class);
        when(event.request()).thenReturn(request);

        AtomicBoolean receivedCorrectEvent = new AtomicBoolean();
        Sampled sampled = new Sampled((OnBreakpoint) e -> receivedCorrectEvent.set(e == event), 50);
        DebugEventDispatcher.doFullDispatch(event, sampled);

        assertTrue(receivedCorrectEvent.get());
    }
}
