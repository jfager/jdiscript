package org.jdiscript.requests;

import com.sun.jdi.Mirror;

public interface JDIScriptEventRequest extends Mirror {

    //Non-chaining EventRequest methods
    Object getProperty(Object key);
    boolean isEnabled();
    int suspendPolicy();

    //ChainableSubInterfaces should define a method with signature
    //'ChainableSubInterface addHandler(OnSomeEvent e);'

}
