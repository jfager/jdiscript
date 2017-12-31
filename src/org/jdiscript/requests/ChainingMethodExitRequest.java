package org.jdiscript.requests;

import org.jdiscript.handlers.OnMethodExit;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ChainingMethodExitRequest extends JDIScriptEventRequest {

    //Chaining EventRequest methods
    ChainingMethodExitRequest addCountFilter(int count);

    ChainingMethodExitRequest disable();

    ChainingMethodExitRequest enable();

    ChainingMethodExitRequest putProperty(Object key, Object value);

    ChainingMethodExitRequest setEnabled(boolean val);

    ChainingMethodExitRequest setSuspendPolicy(int policy);

    //Chaining MethodExitRequest methods
    ChainingMethodExitRequest addClassExclusionFilter(String classPattern);

    ChainingMethodExitRequest addClassFilter(ReferenceType refType);

    ChainingMethodExitRequest addClassFilter(String classPattern);

    ChainingMethodExitRequest addInstanceFilter(ObjectReference instance);

    ChainingMethodExitRequest addThreadFilter(ThreadReference thread);

    ChainingMethodExitRequest addHandler(OnMethodExit handler);
}
