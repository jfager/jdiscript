package org.jdiscript.requests;

import org.jdiscript.handlers.OnStep;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ChainingStepRequest extends JDIScriptEventRequest {

    //Chaining EventRequest methods
    ChainingStepRequest addCountFilter(int count);

    ChainingStepRequest disable();

    ChainingStepRequest enable();

    ChainingStepRequest putProperty(Object key, Object value);

    ChainingStepRequest setEnabled(boolean val);

    ChainingStepRequest setSuspendPolicy(int policy);

    //Non-chaining StepRequest methods
    int depth();

    int size();

    ThreadReference thread();

    //Chaining StepRequest methods
    ChainingStepRequest addClassExclusionFilter(String classPattern);

    ChainingStepRequest addClassFilter(ReferenceType refType);

    ChainingStepRequest addClassFilter(String classPattern);

    ChainingStepRequest addInstanceFilter(ObjectReference instance);

    ChainingStepRequest addHandler(OnStep handler);
}
