package org.jdiscript.handlers;

import com.sun.jdi.event.MonitorWaitedEvent;

public interface OnMonitorWaited extends DebugLocatableHandler
{
    void monitorWaited(MonitorWaitedEvent event);
}

