package cern.accsoft.lhc.inspector.handlers;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.StepRequest;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.BaseEventHandler;

/**
 * An event handler for the {@link cern.accsoft.lhc.inspector.Inspector} main class.
 */
public class InspectorEventHandler extends BaseEventHandler {

    private final JDIScript jdi;
    private ThreadReference threadReference;

    public InspectorEventHandler(JDIScript jdi) {
        super(jdi.vm());
        this.jdi = jdi;
    }

    @Override
    public void breakpoint(BreakpointEvent e) {
        threadReference = e.thread();
        vm().suspend();
        System.out.println("suspended");
    }

    @Override
    public void step(StepEvent e) {
        System.out.println(e);
    }

    @Override
    public void vmStart(VMStartEvent e) {
        // Do nothing
    }

    public ThreadReference getThreadReference() {
        return threadReference;
    }
}
