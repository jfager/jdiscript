package org.jdiscript.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.jdiscript.events.UnhandledEventException;
import org.junit.jupiter.api.Test;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;

class BaseEventHandlerTest {

    // Minimal concrete subclass — just overrides nothing
    static final BaseEventHandler HANDLER = new BaseEventHandler() {};

    // --- Requested events delegate to unhandledEvent and throw ---

    @Test
    void breakpoint_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.breakpoint(mock(BreakpointEvent.class)));
    }

    @Test
    void step_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.step(mock(StepEvent.class)));
    }

    @Test
    void exception_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.exception(mock(ExceptionEvent.class)));
    }

    @Test
    void methodEntry_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.methodEntry(mock(MethodEntryEvent.class)));
    }

    @Test
    void methodExit_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.methodExit(mock(MethodExitEvent.class)));
    }

    @Test
    void classPrepare_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.classPrepare(mock(ClassPrepareEvent.class)));
    }

    @Test
    void threadStart_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.threadStart(mock(ThreadStartEvent.class)));
    }

    @Test
    void threadDeath_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.threadDeath(mock(ThreadDeathEvent.class)));
    }

    @Test
    void accessWatchpoint_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.accessWatchpoint(mock(AccessWatchpointEvent.class)));
    }

    @Test
    void modificationWatchpoint_throws_UnhandledEventException() {
        assertThrows(UnhandledEventException.class,
            () -> HANDLER.modificationWatchpoint(mock(ModificationWatchpointEvent.class)));
    }

    // --- VM events are deliberate no-ops ---

    @Test
    void vmStart_is_noop() {
        assertDoesNotThrow(() -> HANDLER.vmStart(mock(VMStartEvent.class)));
    }

    @Test
    void vmDeath_is_noop() {
        assertDoesNotThrow(() -> HANDLER.vmDeath(mock(VMDeathEvent.class)));
    }

    @Test
    void vmDisconnect_is_noop() {
        assertDoesNotThrow(() -> HANDLER.vmDisconnect(mock(VMDisconnectEvent.class)));
    }

    // --- UnhandledEventException carries the event ---

    @Test
    void unhandledEvent_exception_contains_the_event() {
        BreakpointEvent event = mock(BreakpointEvent.class);
        UnhandledEventException ex = assertThrows(UnhandledEventException.class,
            () -> HANDLER.breakpoint(event));
        assertSame(event, ex.getEvent());
    }

    // --- VM reference accessors ---

    @Test
    void setVM_and_vm_roundtrip() {
        BaseEventHandler h = new BaseEventHandler() {};
        VirtualMachine vm = mock(VirtualMachine.class);
        h.setVM(vm);
        assertSame(vm, h.vm());
    }

    @Test
    void vm_is_null_before_setVM() {
        BaseEventHandler h = new BaseEventHandler() {};
        assertNull(h.vm());
    }
}
