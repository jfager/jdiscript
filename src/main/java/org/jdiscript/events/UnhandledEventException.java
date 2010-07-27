package org.jdiscript.events;

import com.sun.jdi.event.Event;

public class UnhandledEventException extends RuntimeException {
	private static final long serialVersionUID = 20091001L;

	private final Event e;
	
	public UnhandledEventException(Event e) {
		super();
		this.e = e;
	}
	
	public Event getEvent() {
		return this.e;
	}
}
