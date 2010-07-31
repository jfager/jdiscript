package org.jdiscript.requests;

import org.jdiscript.handlers.OnAccessWatchpoint;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ChainingAccessWatchpointRequest extends JDIScriptEventRequest {

	//Chaining EventRequest methods
	ChainingAccessWatchpointRequest addCountFilter(int count);
	ChainingAccessWatchpointRequest disable();
	ChainingAccessWatchpointRequest enable();
	ChainingAccessWatchpointRequest putProperty(Object key, Object value);
	ChainingAccessWatchpointRequest setEnabled(boolean val);
	ChainingAccessWatchpointRequest setSuspendPolicy(int policy);
	
	//Non-chaining WatchpointRequest methods
	Field field();
	
	//Chaining WatchpointRequest methods
	ChainingAccessWatchpointRequest addClassExclusionFilter(String classPattern);
	ChainingAccessWatchpointRequest addClassFilter(ReferenceType refType);
	ChainingAccessWatchpointRequest addClassFilter(String classPattern);
	ChainingAccessWatchpointRequest addInstanceFilter(ObjectReference instance);
	ChainingAccessWatchpointRequest addThreadFilter(ThreadReference thread);
	
	ChainingAccessWatchpointRequest addHandler(OnAccessWatchpoint handler);
}
