package org.jdiscript.handlers;

import com.sun.jdi.event.MonitorWaitEvent;

@FunctionalInterface
public interface OnMonitorWait extends DebugLocatableHandler
{
    void monitorWait(MonitorWaitEvent event);
}
