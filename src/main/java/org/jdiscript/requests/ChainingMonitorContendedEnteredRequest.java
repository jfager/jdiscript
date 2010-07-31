package org.jdiscript.requests;

import org.jdiscript.handlers.OnMonitorContendedEntered;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ChainingMonitorContendedEnteredRequest extends JDIScriptEventRequest {

	//Chaining EventRequest methods
	ChainingMonitorContendedEnteredRequest addCountFilter(int count);
	ChainingMonitorContendedEnteredRequest disable();
	ChainingMonitorContendedEnteredRequest enable();
	ChainingMonitorContendedEnteredRequest putProperty(Object key, Object value);
	ChainingMonitorContendedEnteredRequest setEnabled(boolean val);
	ChainingMonitorContendedEnteredRequest setSuspendPolicy(int policy);
	
	//Chaining MonitorContendedEnteredRequest methods
	ChainingMonitorContendedEnteredRequest addClassExclusionFilter(String classPattern);
	ChainingMonitorContendedEnteredRequest addClassFilter(ReferenceType refType);
	ChainingMonitorContendedEnteredRequest addClassFilter(String classPattern);
	ChainingMonitorContendedEnteredRequest addInstanceFilter(ObjectReference instance);
	ChainingMonitorContendedEnteredRequest addThreadFilter(ThreadReference thread);

	ChainingMonitorContendedEnteredRequest addHandler(OnMonitorContendedEntered handler);
}
