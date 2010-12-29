package org.jdiscript.handlers;

import com.sun.jdi.event.BreakpointEvent;

public interface OnBreakpoint extends DebugLocatableHandler
{
    void exec(BreakpointEvent event);
}
