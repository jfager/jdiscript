package org.jdiscript.handlers;

import com.sun.jdi.event.MethodExitEvent;

public interface OnMethodExit extends DebugLocatableHandler
{
	void exec(MethodExitEvent event);
}
