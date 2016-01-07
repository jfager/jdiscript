package cern.accsoft.lhc.inspector;

import cern.accsoft.lhc.inspector.gui.Stepper;
import cern.accsoft.lhc.inspector.handlers.ImplementorReferenceCounter;
import cern.accsoft.lhc.inspector.handlers.InspectorEventHandler;
import com.sun.jdi.*;
import com.sun.jdi.request.StepRequest;
import com.sun.org.apache.xerces.internal.impl.xpath.XPath;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.BaseEventHandler;
import org.jdiscript.requests.ChainingBreakpointRequest;
import org.jdiscript.util.VMLauncher;

import java.util.List;

/**
 * The main entry point to inspect a Java application.
 */
public class Inspector {

    public static final String VOID_SIGNATURE = "()V";
    public static final String RUN_METHOD_NAME = "run";
    private final JDIScript jdi;
    private final InspectorEventHandler eventHandler;

    private Inspector(JDIScript jdi, InspectorEventHandler eventHandler) {
        this.jdi = jdi;
        this.eventHandler = eventHandler;
    }

    public static void main(String[] args) {
        try {
            VMLauncher launcher = new VMLauncher("-cp /home/jens/workspace/cern/inspector/out/production/inspector/", "cern.accsoft.lhc.inspector.HelloWorld");
            VirtualMachine virtualMachine = launcher.safeStart();
            JDIScript jdi = new JDIScript(virtualMachine);
            InspectorEventHandler eventHandler = new InspectorEventHandler(jdi);
            Inspector inspector = new Inspector(jdi, eventHandler);

            Stepper.start(inspector);

            ImplementorReferenceCounter implementorReferenceCounter =
                    new ImplementorReferenceCounter(Inspectable.class,
                            classType -> registerInspectable(jdi, eventHandler, classType));
            jdi.onClassPrep(implementorReferenceCounter);

            jdi.run(eventHandler);
        } catch (VMDisconnectedException e) {
            System.out.println("VM disconnected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stepOver() {
        ThreadReference threadReference = eventHandler.getThreadReference();
        if (threadReference == null) {
            jdi.vm().resume();
            System.out.println("resuming");
        } else {
            System.out.println("stepping");
            jdi.stepRequest(eventHandler.getThreadReference(), StepRequest.STEP_LINE, StepRequest.STEP_OVER);
        }
    }

    private static void registerInspectable(JDIScript jdi, BaseEventHandler eventHandler, ClassType classType) {
        Method runMethod = classType.concreteMethodByName(RUN_METHOD_NAME, VOID_SIGNATURE);
        Location line = runMethod.locationOfCodeIndex(0);
        System.out.println(line);
        ChainingBreakpointRequest chainingBreakpointRequest = jdi.breakpointRequest(line, eventHandler);
            chainingBreakpointRequest.enable();
    }

}
