/*
 * @(#)EventThread.java    1.6 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * Copyright (c) 1997-2001 by Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package org.jdiscript.events;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;

/**
 * This class processes incoming JDI events
 *
 * @version EventThread.java 1.6 05/11/17 04:47:11
 * @author Robert Field
 * @author Jason Fager
 */
public class EventThread extends Thread {

    private final VirtualMachine vm; // Running VM
    private final DebugEventDispatcher dispatcher;

    public EventThread( VirtualMachine vm,
                        DebugEventDispatcher dispatcher )
    {
        super("jdiscript-event-thread");
        this.vm = vm;
        this.dispatcher = dispatcher;
    }

    /**
     * Run the event handling thread. As long as we are connected, get event
     * sets off the queue and dispatch the events within them.
     */
    @Override
    public void run() {
        EventQueue queue = vm.eventQueue();
        while (true) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator it = eventSet.eventIterator();
                while (it.hasNext()) {
                    dispatcher.dispatch(it.nextEvent());
                }
                eventSet.resume();
            } catch (InterruptedException exc) {
                vm.dispose();
                break;
            } catch (VMDisconnectedException discExc) {
                handleDisconnectedException();
                break;
            }
        }
    }


    // A VMDisconnectedException has happened while dealing with another event.
    // We need to flush the event queue, dealing only with exit events (VMDeath,
    // VMDisconnect) so that we terminate correctly.
    private void handleDisconnectedException() {
        EventQueue queue = vm.eventQueue();
        boolean connected = true;
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator iter = eventSet.eventIterator();
                while (iter.hasNext()) {
                    Event event = iter.nextEvent();
                    if (event instanceof VMDeathEvent) {
                        dispatcher.dispatch(event);
                    } else if (event instanceof VMDisconnectEvent) {
                        dispatcher.dispatch(event);
                        connected = false;
                    }
                }
                eventSet.resume(); // Resume the VM
            } catch (InterruptedException e) {
                vm.dispose();
                break;
            } catch (VMDisconnectedException e) {
                break;
            }
        }
    }
}
