package org.jdiscript.handlers;

import com.sun.jdi.event.ClassPrepareEvent;

public interface OnClassPrepare extends DebugEventHandler
{
    void exec(ClassPrepareEvent event);
}
