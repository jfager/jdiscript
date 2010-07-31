package org.jdiscript.requests;

import org.jdiscript.handlers.OnMethodEntry;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ChainingMethodEntryRequest extends JDIScriptEventRequest {

	//Chaining EventRequest methods
	ChainingMethodEntryRequest addCountFilter(int count);
	ChainingMethodEntryRequest disable();
	ChainingMethodEntryRequest enable();
	ChainingMethodEntryRequest putProperty(Object key, Object value);
	ChainingMethodEntryRequest setEnabled(boolean val);
	ChainingMethodEntryRequest setSuspendPolicy(int policy);
	
	//Chaining MethodEntryRequest methods
	ChainingMethodEntryRequest addClassExclusionFilter(String classPattern);
	ChainingMethodEntryRequest addClassFilter(ReferenceType refType);
	ChainingMethodEntryRequest addClassFilter(String classPattern);
	ChainingMethodEntryRequest addInstanceFilter(ObjectReference instance);
	ChainingMethodEntryRequest addThreadFilter(ThreadReference thread);

	ChainingMethodEntryRequest addHandler(OnMethodEntry handler);
}
