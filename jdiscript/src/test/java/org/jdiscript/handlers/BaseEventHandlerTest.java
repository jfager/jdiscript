package org.jdiscript.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.events.UnhandledEventException;
import org.junit.jupiter.api.Test;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;

/**
 * JaCoCo places probes *after* the invokevirtual instruction. Because the
 * default unhandledEvent always throws, those probes never fire when the
 * default handler is used. We work around this by providing a no-throw
 * override for the "event delegates to unhandledEvent" tests, which lets the
 * probe fire after the call returns normally.
 */
class BaseEventHandlerTest {

    /** Non-throwing handler that records whether unhandledEvent was called. */
    private static BaseEventHandler trackingHandler(AtomicBoolean called) {
        return new BaseEventHandler() {
            @Override
            public void unhandledEvent(Event e) {
                called.set(true);
            }
        };
    }

    private static void assertDelegates(Event event) {
        AtomicBoolean called = new AtomicBoolean();
        DebugEventDispatcher.doFullDispatch(event, trackingHandler(called));
        assertTrue(called.get());
    }

    // --- Each event method must delegate to unhandledEvent ---

    @Test void breakpoint_delegates() { assertDelegates(mock(BreakpointEvent.class)); }
    @Test void step_delegates() { assertDelegates(mock(StepEvent.class)); }
    @Test void exception_delegates() { assertDelegates(mock(ExceptionEvent.class)); }
    @Test void methodEntry_delegates() { assertDelegates(mock(MethodEntryEvent.class)); }
    @Test void methodExit_delegates() { assertDelegates(mock(MethodExitEvent.class)); }
    @Test void classPrepare_delegates() { assertDelegates(mock(ClassPrepareEvent.class)); }
    @Test void threadStart_delegates() { assertDelegates(mock(ThreadStartEvent.class)); }
    @Test void threadDeath_delegates() { assertDelegates(mock(ThreadDeathEvent.class)); }
    @Test void accessWatchpoint_delegates() { assertDelegates(mock(AccessWatchpointEvent.class)); }
    @Test void modificationWatchpoint_delegates() { assertDelegates(mock(ModificationWatchpointEvent.class)); }
    @Test void classUnload_delegates() { assertDelegates(mock(ClassUnloadEvent.class)); }
    @Test void monitorContendedEnter_delegates() { assertDelegates(mock(MonitorContendedEnterEvent.class)); }
    @Test void monitorContendedEntered_delegates() { assertDelegates(mock(MonitorContendedEnteredEvent.class)); }
    @Test void monitorWait_delegates() { assertDelegates(mock(MonitorWaitEvent.class)); }
    @Test void monitorWaited_delegates() { assertDelegates(mock(MonitorWaitedEvent.class)); }

    @Test
    void watchpoint_catchall_delegates() {
        // WatchpointEvent mock (not a more specific subtype) hits the catchall
        assertDelegates(mock(WatchpointEvent.class));
    }

    @Test
    void locatable_catchall_delegates() {
        // LocatableEvent mock (not a more specific subtype) hits the catchall
        assertDelegates(mock(LocatableEvent.class));
    }

    @Test
    void event_catchall_delegates() {
        // Plain Event mock (no specific subtype) hits the final else branch
        assertDelegates(mock(Event.class));
    }

    // --- VM events are deliberate no-ops ---

    @Test
    void vmStart_is_noop() {
        BaseEventHandler handler = new BaseEventHandler() {};
        assertDoesNotThrow(() -> DebugEventDispatcher.doFullDispatch(mock(VMStartEvent.class), handler));
    }

    @Test
    void vmDeath_is_noop() {
        BaseEventHandler handler = new BaseEventHandler() {};
        assertDoesNotThrow(() -> DebugEventDispatcher.doFullDispatch(mock(VMDeathEvent.class), handler));
    }

    @Test
    void vmDisconnect_is_noop() {
        BaseEventHandler handler = new BaseEventHandler() {};
        assertDoesNotThrow(() -> DebugEventDispatcher.doFullDispatch(mock(VMDisconnectEvent.class), handler));
    }

    // --- notifySuspendPolicy is a no-op ---

    @Test
    void notifySuspendPolicy_is_noop() {
        assertDoesNotThrow(() -> new BaseEventHandler() {}.notifySuspendPolicy(0));
    }

    // --- Default unhandledEvent throws and carries the event ---

    @Test
    void unhandledEvent_throws_UnhandledEventException() {
        BaseEventHandler handler = new BaseEventHandler() {};
        assertThrows(UnhandledEventException.class,
            () -> handler.unhandledEvent(mock(Event.class)));
    }

    @Test
    void unhandledEvent_exception_contains_the_event() {
        BaseEventHandler handler = new BaseEventHandler() {};
        Event event = mock(Event.class);
        UnhandledEventException ex = assertThrows(UnhandledEventException.class,
            () -> handler.unhandledEvent(event));
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
        assertNull(new BaseEventHandler() {}.vm());
    }
}
