package cern.accsoft.lhc.inspector.controller;

import java.util.HashMap;
import java.util.Map;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.BaseEventHandler;
import org.jdiscript.requests.ChainingStepRequest;

import cern.accsoft.lhc.inspector.inspectable.InspectableCallbackListener;
import cern.accsoft.lhc.inspector.inspectable.InspectableInstanceListener;
import cern.accsoft.lhc.inspector.inspectable.LocationRange;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.StepRequest;

/**
 * An event handler for the {@link cern.accsoft.lhc.inspector.Inspector} main class.
 */
public class JdiControllerEventHandler extends BaseEventHandler {

    private final JDIScript jdi;
    private final InspectableInstanceListener callbackHandler;
    private final Map<ThreadReference, InspectableState> threads = new HashMap<>();

    public JdiControllerEventHandler(JDIScript jdi, InspectableInstanceListener callbackHandler) {
        super(jdi.vm());
        this.jdi = jdi;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public void breakpoint(BreakpointEvent event) {
        try {
            final ThreadReference threadReference = event.thread();
            threadReference.suspend();
            final LocationRange range = LocationRange.ofMethod(event.location().method());
            final ChainingStepRequest request = jdi.stepRequest(threadReference, StepRequest.STEP_LINE,
                    StepRequest.STEP_OVER);
            request.addHandler(this);
            request.enable();

            final InspectableCallbackListener callbackListener = callbackHandler.onNewInspectableInstance(
                    event.thread(), new ThreadState(ThreadState.StepDirection.FORWARD, range, event.location()));
            final InspectableState state = new InspectableState(callbackListener, range);
            threads.put(event.thread(), state);
        } catch (AbsentInformationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void step(StepEvent e) {
        InspectableState state = threads.get(e.thread());
        if (state != null) {
            if (state.methodRange.isWithin(e.location())) {
                threads.get(e.thread()).listener.onLocationChange(new ThreadState(ThreadState.StepDirection.FORWARD,
                        state.methodRange, e.location()));
                e.thread().suspend();
            } else {
                threads.remove(e.thread()).listener.onInspectionEnd();
            }
        }
    }

    @Override
    public void vmStart(VMStartEvent e) {
        // Do nothing
    }

    private static class InspectableState {

        private final InspectableCallbackListener listener;
        private final LocationRange methodRange;

        InspectableState(InspectableCallbackListener listener, LocationRange methodRange) {
            this.listener = listener;
            this.methodRange = methodRange;
        }

    }

}
