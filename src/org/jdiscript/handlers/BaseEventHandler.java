package org.jdiscript.handlers;

import org.jdiscript.events.UnhandledEventException;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;

/**
 * Each handler method for requested events delegates to unhandledEvent(), which
 * throws an UnhandledEventException. Handler methods for unrequested events do
 * nothing.
 *
 * This class is intended to be a subclass.
 *
 * @author jfager
 */
public class BaseEventHandler
        implements OnAccessWatchpoint, OnBreakpoint, OnClassPrepare,
        OnClassUnload, OnEvent, OnException, OnLocatable,
        OnMethodEntry, OnMethodExit, OnModificationWatchpoint,
        OnMonitorContendedEnter, OnMonitorContendedEntered,
        OnMonitorWait, OnMonitorWaited, OnStep, OnThreadDeath,
        OnThreadStart, OnVMDeath, OnVMDisconnect, OnVMStart,
        OnWatchpoint {

    private VirtualMachine vm;

    public void setVM(VirtualMachine vm) {
        this.vm = vm;
    }

    public VirtualMachine vm() {
        return vm;
    }

    public void notifySuspendPolicy(int suspendPolicy) {
        // do nothing
    }

    /**
     *
     * @param e
     */
    @Override
    public void accessWatchpoint(AccessWatchpointEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void breakpoint(BreakpointEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void classPrepare(ClassPrepareEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void classUnload(ClassUnloadEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void event(Event e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void exception(ExceptionEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void locatable(LocatableEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void methodEntry(MethodEntryEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void methodExit(MethodExitEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void modificationWatchpoint(ModificationWatchpointEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void monitorContendedEnter(MonitorContendedEnterEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void monitorContendedEntered(MonitorContendedEnteredEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void monitorWait(MonitorWaitEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void monitorWaited(MonitorWaitedEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void step(StepEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void threadDeath(ThreadDeathEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void threadStart(ThreadStartEvent e) {
        unhandledEvent(e);
    }

    /**
     *
     * @param e
     */
    @Override
    public void vmDeath(VMDeathEvent e) {
        //do nothing
    }

    /**
     *
     * @param e
     */
    @Override
    public void vmDisconnect(VMDisconnectEvent e) {
        //do nothing
    }

    /**
     *
     * @param e
     */
    @Override
    public void vmStart(VMStartEvent e) {
        //do nothing
    }

    /**
     *
     * @param e
     */
    @Override
    public void watchpoint(WatchpointEvent e) {
        unhandledEvent(e);
    }

    public void unhandledEvent(Event e) {
        throw new UnhandledEventException(e);
    }
}
