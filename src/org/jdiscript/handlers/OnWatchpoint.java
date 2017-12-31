package org.jdiscript.handlers;

import com.sun.jdi.event.WatchpointEvent;

@FunctionalInterface
public interface OnWatchpoint extends DebugWatchpointHandler {

    void watchpoint(WatchpointEvent event);
}
