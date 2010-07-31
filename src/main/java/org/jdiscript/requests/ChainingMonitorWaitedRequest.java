package org.jdiscript.requests;

import org.jdiscript.handlers.OnMonitorWaited;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ChainingMonitorWaitedRequest extends JDIScriptEventRequest {

	//Chaining EventRequest methods
	ChainingMonitorWaitedRequest addCountFilter(int count);
	ChainingMonitorWaitedRequest disable();
	ChainingMonitorWaitedRequest enable();
	ChainingMonitorWaitedRequest putProperty(Object key, Object value);
	ChainingMonitorWaitedRequest setEnabled(boolean val);
	ChainingMonitorWaitedRequest setSuspendPolicy(int policy);
	
	//Chaining MonitorWaitedRequest methods
	ChainingMonitorWaitedRequest addClassExclusionFilter(String classPattern);
	ChainingMonitorWaitedRequest addClassFilter(ReferenceType refType);
	ChainingMonitorWaitedRequest addClassFilter(String classPattern);
	ChainingMonitorWaitedRequest addInstanceFilter(ObjectReference instance);
	ChainingMonitorWaitedRequest addThreadFilter(ThreadReference thread);

	ChainingMonitorWaitedRequest addHandler(OnMonitorWaited handler);
}
