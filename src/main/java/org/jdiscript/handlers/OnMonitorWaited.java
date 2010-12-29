package org.jdiscript.handlers;

import com.sun.jdi.event.MonitorWaitedEvent;

public interface OnMonitorWaited extends DebugLocatableHandler
{
    void exec(MonitorWaitedEvent event);
}

