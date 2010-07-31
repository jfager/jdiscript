package org.jdiscript.handlers;

import com.sun.jdi.event.LocatableEvent;

public interface OnLocatable extends DebugLocatableHandler 
{
	void exec(LocatableEvent event);
}
