package org.jdiscript.handlers;

import com.sun.jdi.event.StepEvent;

@FunctionalInterface
public interface OnStep extends DebugLocatableHandler
{
    void step(StepEvent event);
}
