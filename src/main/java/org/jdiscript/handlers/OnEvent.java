package org.jdiscript.handlers;

import com.sun.jdi.event.Event;

public interface OnEvent extends DebugEventHandler
{
    void exec(Event event);
}
