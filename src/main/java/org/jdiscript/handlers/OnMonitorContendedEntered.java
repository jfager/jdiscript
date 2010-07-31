package org.jdiscript.handlers;

import com.sun.jdi.event.MonitorContendedEnteredEvent;

public interface OnMonitorContendedEntered extends DebugLocatableHandler
{
	void exec(MonitorContendedEnteredEvent event); 
}
