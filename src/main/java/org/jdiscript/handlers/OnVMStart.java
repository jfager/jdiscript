package org.jdiscript.handlers;

import com.sun.jdi.event.VMStartEvent;

@FunctionalInterface
public interface OnVMStart extends DebugEventHandler
{
    void vmStart(VMStartEvent event);
}
