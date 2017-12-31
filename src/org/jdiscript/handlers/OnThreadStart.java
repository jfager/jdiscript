package org.jdiscript.handlers;

import com.sun.jdi.event.ThreadStartEvent;

@FunctionalInterface
public interface OnThreadStart extends DebugEventHandler {

    void threadStart(ThreadStartEvent event);
}
