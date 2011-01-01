package org.jdiscript.handlers;

import com.sun.jdi.event.VMStartEvent;

public interface OnVMStart extends DebugEventHandler
{
    void vmStart(VMStartEvent event);
}
