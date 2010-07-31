package org.jdiscript.handlers;

import com.sun.jdi.event.ClassUnloadEvent;

public interface OnClassUnload extends DebugEventHandler
{
	void exec(ClassUnloadEvent event);
}
