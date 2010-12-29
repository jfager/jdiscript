package org.jdiscript.requests;

import org.jdiscript.handlers.OnClassUnload;

public interface ChainingClassUnloadRequest extends JDIScriptEventRequest {

    //Chaining EventRequest methods
    ChainingClassUnloadRequest addCountFilter(int count);
    ChainingClassUnloadRequest disable();
    ChainingClassUnloadRequest enable();
    ChainingClassUnloadRequest putProperty(Object key, Object value);
    ChainingClassUnloadRequest setEnabled(boolean val);
    ChainingClassUnloadRequest setSuspendPolicy(int policy);

    //Chaining ClassUnloadRequest methods
    ChainingClassUnloadRequest addClassExclusionFilter(String classPattern);
    ChainingClassUnloadRequest addClassFilter(String classPattern);

    ChainingClassUnloadRequest addHandler(OnClassUnload handler);
}
