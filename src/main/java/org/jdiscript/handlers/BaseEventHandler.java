package org.jdiscript.handlers;

import org.jdiscript.events.UnhandledEventException;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;

/**
 * Each handler method for requested events delegates to unhandledEvent(),
 * which throws an UnhandledEventException.  Handler methods for unrequested
 * events do nothing.
 *
 * This class is intended to be subclassed.
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
               OnWatchpoint
{

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

    public void exec(AccessWatchpointEvent e) {
        unhandledEvent(e);
    }

    public void exec(BreakpointEvent e) {
        unhandledEvent(e);
    }

    public void exec(ClassPrepareEvent e) {
        unhandledEvent(e);
    }

    public void exec(ClassUnloadEvent e) {
        unhandledEvent(e);
    }

    public void exec(Event e) {
        unhandledEvent(e);
    }

    public void exec(ExceptionEvent e) {
        unhandledEvent(e);
    }

    public void exec(LocatableEvent e) {
        unhandledEvent(e);
    }

    public void exec(MethodEntryEvent e) {
        unhandledEvent(e);
    }

    public void exec(MethodExitEvent e) {
        unhandledEvent(e);
    }

    public void exec(ModificationWatchpointEvent e) {
        unhandledEvent(e);
    }

    public void exec(MonitorContendedEnterEvent e) {
        unhandledEvent(e);
    }

    public void exec(MonitorContendedEnteredEvent e){
        unhandledEvent(e);
    }

    public void exec(MonitorWaitEvent e) {
        unhandledEvent(e);
    }

    public void exec(MonitorWaitedEvent e) {
        unhandledEvent(e);
    }

    public void exec(StepEvent e) {
        unhandledEvent(e);
    }

    public void exec(ThreadDeathEvent e) {
        unhandledEvent(e);
    }

    public void exec(ThreadStartEvent e) {
        unhandledEvent(e);
    }

    public void exec(VMDeathEvent e) {
        //do nothing
    }

    public void exec(VMDisconnectEvent e) {
        //do nothing
    }

    public void exec(VMStartEvent e) {
        //do nothing
    }

    public void exec(WatchpointEvent e) {
        unhandledEvent(e);
    }

    public void unhandledEvent( Event e ) {
        throw new UnhandledEventException(e);
    }
}
