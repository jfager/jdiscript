package org.jdiscript.requests;

import org.jdiscript.handlers.OnBreakpoint;

import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

public interface ChainingBreakpointRequest extends JDIScriptEventRequest {

    //Chaining EventRequest methods
    ChainingBreakpointRequest addCountFilter(int count);

    ChainingBreakpointRequest disable();

    ChainingBreakpointRequest enable();

    ChainingBreakpointRequest putProperty(Object key, Object value);

    ChainingBreakpointRequest setEnabled(boolean val);

    ChainingBreakpointRequest setSuspendPolicy(int policy);

    //Non-chaining BreakpointRequest methods
    Location location();

    //Chaining BreakpointRequest methods
    ChainingBreakpointRequest addInstanceFilter(ObjectReference instance);

    ChainingBreakpointRequest addThreadFilter(ThreadReference thread);

    ChainingBreakpointRequest addHandler(OnBreakpoint handler);
}
