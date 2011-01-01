package org.jdiscript.handlers;

import com.sun.jdi.event.StepEvent;

public interface OnStep extends DebugLocatableHandler
{
    void step(StepEvent event);
}
