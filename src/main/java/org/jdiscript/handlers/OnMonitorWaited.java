package org.jdiscript.handlers;

import com.sun.jdi.event.MonitorWaitedEvent;

@FunctionalInterface
public interface OnMonitorWaited extends DebugLocatableHandler
{
    void monitorWaited(MonitorWaitedEvent event);
}

