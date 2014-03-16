package org.jdiscript.handlers;

import com.sun.jdi.event.LocatableEvent;

@FunctionalInterface
public interface OnLocatable extends DebugLocatableHandler
{
    void locatable(LocatableEvent event);
}
