package org.jdiscript.handlers;

import com.sun.jdi.event.MonitorWaitEvent;

public interface OnMonitorWait extends DebugLocatableHandler 
{
	void exec(MonitorWaitEvent event); 
}
