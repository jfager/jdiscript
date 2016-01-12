package cern.accsoft.lhc.inspector.gui;

import cern.accsoft.lhc.inspector.controller.ThreadState;
import cern.accsoft.lhc.inspector.inspectable.InspectableCallbackListener;

/**
 * An implementation of the {@link InspectableCallbackListener} which handles event callbacks for the {@link Stepper}
 * gui.
 */
public class StepperCallbackHandler implements InspectableCallbackListener {

    private StepperTab tab;

    public StepperCallbackHandler(StepperTab tab) {
        this.tab = tab;
    }

    @Override
    public void onLocationChange(ThreadState state) {
        tab.highlight(state.getCurrentLocation().lineNumber());
    }

    @Override
    public void onInspectionEnd() {
        tab.close();
    }

}
