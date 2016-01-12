/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.accsoft.lhc.inspector.inspectable;

import java.util.List;

import cern.accsoft.lhc.inspector.controller.ThreadState;

import com.sun.jdi.ThreadReference;

/**
 * A listener for new inspectable instances.
 * 
 * @author jepeders
 */
public interface InspectableInstanceListener {

    public InspectableCallbackListener onNewInspectableInstance(ThreadReference thread, ThreadState state);

}
