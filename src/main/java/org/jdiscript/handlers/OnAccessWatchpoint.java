package org.jdiscript.handlers;

import com.sun.jdi.event.AccessWatchpointEvent;

public interface OnAccessWatchpoint extends DebugWatchpointHandler
{
    void exec(AccessWatchpointEvent event);
}
