package org.jdiscript.handlers;

import com.sun.jdi.event.ClassUnloadEvent;

@FunctionalInterface
public interface OnClassUnload extends DebugEventHandler {

    void classUnload(ClassUnloadEvent event);
}
