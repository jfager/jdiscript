package org.jdiscript.handlers;

import com.sun.jdi.event.VMDeathEvent;

public interface OnVMDeath extends DebugEventHandler
{
    void exec(VMDeathEvent event);
}
