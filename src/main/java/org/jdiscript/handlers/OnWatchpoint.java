package org.jdiscript.handlers;

import com.sun.jdi.event.WatchpointEvent;

public interface OnWatchpoint extends DebugWatchpointHandler
{
    void exec(WatchpointEvent event);
}
