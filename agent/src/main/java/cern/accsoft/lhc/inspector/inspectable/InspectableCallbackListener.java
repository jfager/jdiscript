package cern.accsoft.lhc.inspector.inspectable;

import cern.accsoft.lhc.inspector.controller.ThreadState;

/**
 * Handles callbacks from the running VM and feeds them into the GUI, thus updating information such as current
 * breakpoint {@link com.sun.jdi.Location}.
 */
public interface InspectableCallbackListener {

    void onLocationChange(ThreadState state);

    void onInspectionEnd();

}
