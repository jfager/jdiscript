package org.jdiscript.requests;

import org.jdiscript.handlers.OnException;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ChainingExceptionRequest extends JDIScriptEventRequest {

    //Chaining EventRequest methods
    ChainingExceptionRequest addCountFilter(int count);

    ChainingExceptionRequest disable();

    ChainingExceptionRequest enable();

    ChainingExceptionRequest putProperty(Object key, Object value);

    ChainingExceptionRequest setEnabled(boolean val);

    ChainingExceptionRequest setSuspendPolicy(int policy);

    //Non-chaining ExceptionRequest methods
    ReferenceType exception();

    boolean notifyCaught();

    boolean notifyUncaught();

    //Chaining ExceptionRequest methods
    ChainingExceptionRequest addClassExclusionFilter(String classPattern);

    ChainingExceptionRequest addClassFilter(ReferenceType refType);

    ChainingExceptionRequest addClassFilter(String classPattern);

    ChainingExceptionRequest addInstanceFilter(ObjectReference instance);

    ChainingExceptionRequest addThreadFilter(ThreadReference thread);

    ChainingExceptionRequest addHandler(OnException handler);

}
