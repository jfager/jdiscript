package org.jdiscript.handlers;

import org.jdiscript.events.DebugEventDispatcher;

import com.sun.jdi.event.Event;

public class Once extends BaseEventHandler {

    private final DebugEventHandler handler;

    public Once(DebugEventHandler handler) {
        this.handler = handler;
    }

    @Override
    public void unhandledEvent(Event e) {
        DebugEventDispatcher.doFullDispatch(e, handler);
        e.request().disable();
    }
}
