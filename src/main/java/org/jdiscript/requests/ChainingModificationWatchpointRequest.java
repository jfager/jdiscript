package org.jdiscript.requests;

import org.jdiscript.handlers.OnModificationWatchpoint;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ChainingModificationWatchpointRequest {

	//Chaining EventRequest methods
	ChainingModificationWatchpointRequest addCountFilter(int count);
	ChainingModificationWatchpointRequest disable();
	ChainingModificationWatchpointRequest enable();
	ChainingModificationWatchpointRequest putProperty(Object key, Object value);
	ChainingModificationWatchpointRequest setEnabled(boolean val);
	ChainingModificationWatchpointRequest setSuspendPolicy(int policy);
	
	//Non-chaining WatchpointRequest methods
	Field field();
	
	//Chaining WatchpointRequest methods
	ChainingModificationWatchpointRequest addClassExclusionFilter(String classPattern);
	ChainingModificationWatchpointRequest addClassFilter(ReferenceType refType);
	ChainingModificationWatchpointRequest addClassFilter(String classPattern);
	ChainingModificationWatchpointRequest addInstanceFilter(ObjectReference instance);
	ChainingModificationWatchpointRequest addThreadFilter(ThreadReference thread);
	
	ChainingModificationWatchpointRequest addHandler(OnModificationWatchpoint handler);
}
