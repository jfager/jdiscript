package org.jdiscript

import handlers._

import com.sun.jdi.event._

/**
 * Scala implicit conversions from functions to jdiscript event handlers.
 */
object HandlerConversions {
  implicit def accessWatchpoint(f: AccessWatchpointEvent => Unit) =
    new OnAccessWatchpoint() {
      def accessWatchpoint(ev: AccessWatchpointEvent) = f(ev)
    }

  implicit def breakpoint(f: BreakpointEvent => Unit) =
    new OnBreakpoint() {
      def breakpoint(ev: BreakpointEvent) = f(ev)
    }

  implicit def classPrepare(f: ClassPrepareEvent => Unit) =
    new OnClassPrepare() {
      def classPrepare(ev: ClassPrepareEvent) = f(ev)
    }

  implicit def classUnload(f: ClassUnloadEvent => Unit) =
    new OnClassUnload() {
      def classUnload(ev: ClassUnloadEvent) = f(ev)
    }

  implicit def event(f: Event => Unit) =
    new OnEvent() {
      def event(ev: Event) = f(ev)
    }

  implicit def exception(f: ExceptionEvent => Unit) =
    new OnException() {
      def exception(ev: ExceptionEvent) = f(ev)
    }

  implicit def locatable(f: LocatableEvent => Unit) =
    new OnLocatable() {
      def locatable(ev: LocatableEvent) = f(ev)
    }

  implicit def methodEntry(f: MethodEntryEvent => Unit) =
    new OnMethodEntry() {
      def methodEntry(ev: MethodEntryEvent) = f(ev)
    }

  implicit def methodExit(f: MethodExitEvent => Unit) =
    new OnMethodExit() {
      def methodExit(ev: MethodExitEvent) = f(ev)
    }

  implicit def modificationWatchpoint(f: ModificationWatchpointEvent => Unit) =
    new OnModificationWatchpoint() {
      def modificationWatchpoint(ev: ModificationWatchpointEvent) = f(ev)
    }

  implicit def monitorContendedEnter(f: MonitorContendedEnterEvent => Unit) =
    new OnMonitorContendedEnter() {
      def monitorContendedEnter(ev: MonitorContendedEnterEvent) = f(ev)
    }

  implicit def monitorContendedEntered(f: MonitorContendedEnteredEvent => Unit) =
    new OnMonitorContendedEntered() {
      def monitorContendedEntered(ev: MonitorContendedEnteredEvent) = f(ev)
    }

  implicit def monitorWait(f: MonitorWaitEvent => Unit) =
    new OnMonitorWait() {
      def monitorWait(ev: MonitorWaitEvent) = f(ev)
    }

  implicit def monitorWaited(f: MonitorWaitedEvent => Unit) =
    new OnMonitorWaited() {
      def monitorWaited(ev: MonitorWaitedEvent) = f(ev)
    }

  implicit def step(f: StepEvent => Unit) =
    new OnStep() {
      def step(ev: StepEvent) = f(ev)
    }

  implicit def threadDeath(f: ThreadDeathEvent => Unit) =
    new OnThreadDeath() {
      def threadDeath(ev: ThreadDeathEvent) = f(ev)
    }

  implicit def threadStart(f: ThreadStartEvent => Unit) =
    new OnThreadStart() {
      def threadStart(ev: ThreadStartEvent) = f(ev)
    }

  implicit def vmDeath(f: VMDeathEvent => Unit) =
    new OnVMDeath() {
      def vmDeath(ev: VMDeathEvent) = f(ev)
    }

  implicit def vmDisconnect(f: VMDisconnectEvent => Unit) =
    new OnVMDisconnect() {
      def vmDisconnect(ev: VMDisconnectEvent) = f(ev)
    }

  implicit def vmStart(f: VMStartEvent => Unit) =
    new OnVMStart() {
      def vmStart(ev: VMStartEvent) = f(ev)
    }

  implicit def watchpoint(f: WatchpointEvent => Unit) =
    new OnWatchpoint() {
      def watchpoint(ev: WatchpointEvent) = f(ev)
    }
}
