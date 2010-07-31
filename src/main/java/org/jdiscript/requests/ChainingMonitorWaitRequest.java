package org.jdiscript.requests;

import org.jdiscript.handlers.OnMonitorWait;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ChainingMonitorWaitRequest extends JDIScriptEventRequest {

	//Chaining EventRequest methods
	ChainingMonitorWaitRequest addCountFilter(int count);
	ChainingMonitorWaitRequest disable();
	ChainingMonitorWaitRequest enable();
	ChainingMonitorWaitRequest putProperty(Object key, Object value);
	ChainingMonitorWaitRequest setEnabled(boolean val);
	ChainingMonitorWaitRequest setSuspendPolicy(int policy);
	
	//Chaining MonitorWaitRequest methods
	ChainingMonitorWaitRequest addClassExclusionFilter(String classPattern);
	ChainingMonitorWaitRequest addClassFilter(ReferenceType refType);
	ChainingMonitorWaitRequest addClassFilter(String classPattern);
	ChainingMonitorWaitRequest addInstanceFilter(ObjectReference instance);
	ChainingMonitorWaitRequest addThreadFilter(ThreadReference thread);

	ChainingMonitorWaitRequest addHandler(OnMonitorWait handler);
}
