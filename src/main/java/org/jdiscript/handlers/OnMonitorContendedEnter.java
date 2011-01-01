package org.jdiscript.handlers;

import com.sun.jdi.event.MonitorContendedEnterEvent;

public interface OnMonitorContendedEnter extends DebugLocatableHandler
{
    void monitorContendedEnter(MonitorContendedEnterEvent event);
}
