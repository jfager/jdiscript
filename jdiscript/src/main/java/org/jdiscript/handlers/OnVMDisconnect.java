package org.jdiscript.handlers;

import com.sun.jdi.event.VMDisconnectEvent;

@FunctionalInterface
public interface OnVMDisconnect extends DebugEventHandler
{
    void vmDisconnect(VMDisconnectEvent event);
}
