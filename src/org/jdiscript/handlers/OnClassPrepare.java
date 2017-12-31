package org.jdiscript.handlers;

import com.sun.jdi.event.ClassPrepareEvent;

@FunctionalInterface
public interface OnClassPrepare extends DebugEventHandler {

    void classPrepare(ClassPrepareEvent event);
}
