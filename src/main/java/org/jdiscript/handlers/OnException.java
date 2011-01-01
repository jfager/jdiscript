package org.jdiscript.handlers;

import com.sun.jdi.event.ExceptionEvent;

public interface OnException extends DebugLocatableHandler
{
    void exception(ExceptionEvent e);
}
