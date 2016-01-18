package org.jdiscript.handlers;

import com.sun.jdi.event.VMDeathEvent;

@FunctionalInterface
public interface OnVMDeath extends DebugEventHandler
{
    void vmDeath(VMDeathEvent event);
}
