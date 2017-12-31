package org.jdiscript.handlers;

import com.sun.jdi.event.MethodEntryEvent;

@FunctionalInterface
public interface OnMethodEntry extends DebugLocatableHandler {

    void methodEntry(MethodEntryEvent event);
}
