package org.jdiscript.requests;

import org.jdiscript.handlers.OnThreadStart;

import com.sun.jdi.ThreadReference;

public interface ChainingThreadStartRequest extends JDIScriptEventRequest {

    //Chaining EventRequest methods
    ChainingThreadStartRequest addCountFilter(int count);
    ChainingThreadStartRequest disable();
    ChainingThreadStartRequest enable();
    ChainingThreadStartRequest putProperty(Object key, Object value);
    ChainingThreadStartRequest setEnabled(boolean val);
    ChainingThreadStartRequest setSuspendPolicy(int policy);

    //Chaining ThreadStartRequest methods
    ChainingThreadStartRequest addThreadFilter(ThreadReference instance);

    ChainingThreadStartRequest addHandler(OnThreadStart handler);
}
