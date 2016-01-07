package org.jdiscript.handlers;

import com.sun.jdi.VirtualMachine;
import org.jdiscript.events.DebugEventDispatcher;

import com.sun.jdi.event.Event;

public class Once extends BaseEventHandler {
	private DebugEventHandler handler;
	public Once(VirtualMachine vm, DebugEventHandler handler) {
		super(vm);
		this.handler = handler;
	}
	@Override
    public void unhandledEvent( Event e ) {
		DebugEventDispatcher.doFullDispatch(e, handler);
		e.request().disable();
    }
}
