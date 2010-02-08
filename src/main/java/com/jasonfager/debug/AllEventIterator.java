package com.jasonfager.debug;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;

public class AllEventIterator implements Iterator<Event> {
	private final VirtualMachine vm;
	
	private EventSet currEventSet = null;
	private EventIterator currEventIter = null;
	
	public AllEventIterator(VirtualMachine vm) {
		this.vm = vm;
	}
	
	@Override
	public boolean hasNext() {
		if(currEventIter != null && currEventIter.hasNext()) {
			return true;
		}
		try {
			if(currEventSet != null) {
				currEventSet.resume();
			}
			currEventSet = vm.eventQueue().remove();
			currEventIter = currEventSet.eventIterator();
			return hasNext();
		} catch(VMDisconnectedException vmde) {
			return false;
		} catch(InterruptedException ie) {
			return false;
		}
	}

	@Override
	public Event next() {
		if(!hasNext()) {
			throw new NoSuchElementException();
		}
		return currEventIter.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
