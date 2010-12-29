package org.jdiscript.handlers;

import com.sun.jdi.event.MethodEntryEvent;

public interface OnMethodEntry extends DebugLocatableHandler
{
    void exec(MethodEntryEvent event);
}
