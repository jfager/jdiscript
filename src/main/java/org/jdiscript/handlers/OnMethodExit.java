package org.jdiscript.handlers;

import com.sun.jdi.event.MethodExitEvent;

public interface OnMethodExit extends DebugLocatableHandler
{
    void methodExit(MethodExitEvent event);
}
