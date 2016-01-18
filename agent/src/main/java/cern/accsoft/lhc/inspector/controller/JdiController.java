package cern.accsoft.lhc.inspector.controller;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jdiscript.JDIScript;
import org.jdiscript.requests.ChainingStepRequest;
import org.jdiscript.util.VMLauncher;

import cern.accsoft.lhc.inspector.inspectable.InspectableInstanceListener;
import cern.accsoft.lhc.inspector.inspectable.InspectableMethod;
import cern.accsoft.lhc.inspector.inspectable.InterfaceImplementationListener;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.request.StepRequest;

import demo.Inspectable;

/**
 * A controller for a JDI instance that can
 */
public class JdiController implements Closeable {

    public static final String VOID_SIGNATURE = "()V";
    public static final String RUN_METHOD_NAME = "run";

    private final ExecutorService executorService;
    private final JDIScript jdi;
    private final JdiControllerEventHandler eventHandler;

    private JdiController(JDIScript jdi, JdiControllerEventHandler eventHandler, ExecutorService executorService) {
        this.jdi = jdi;
        this.eventHandler = eventHandler;
        this.executorService = executorService;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void clearStepCallbacks() {
        jdi.stepRequests(eventHandler).clear();
    }

    @Override
    public void close() throws IOException {
        try {
            jdi.vm().exit(0);
            executorService.shutdown();
        } catch (VMDisconnectedException e) {
            /* Already disconnected */
        }
    }

    public void prepareStepForward(ThreadReference threadReference) {
        clearStepCallbacks();
        ChainingStepRequest stepRequest = jdi
                .stepRequest(threadReference, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
        stepRequest.enable();
    }

    public static class Builder {

        private VMLauncher launcher;
        private InspectableMethod inspectableMethod;
        private InspectableInstanceListener inspectableListener;

        public JdiController build() throws IOException, IllegalConnectorArgumentsException, VMStartException {
            VirtualMachine virtualMachine = launcher.safeStart();
            JDIScript jdi = new JDIScript(virtualMachine);
            JdiControllerEventHandler eventHandler = new JdiControllerEventHandler(jdi, inspectableListener);

            ExecutorService executorService = Executors.newFixedThreadPool(1);
            executorService.execute(() -> {
                InterfaceImplementationListener interfaceImplementationCounter = new InterfaceImplementationListener(
                        Inspectable.class, classType -> {
                            registerInspectable(jdi, eventHandler, classType, inspectableMethod);
                        });
                jdi.onClassPrep(interfaceImplementationCounter);

                jdi.run(eventHandler);
            });

            return new JdiController(jdi, eventHandler, executorService);
        }

        private static void registerInspectable(JDIScript jdi, JdiControllerEventHandler eventHandler,
                ClassType classType, InspectableMethod inspectableMethod) {
            Method runMethod = classType.methodsByName(inspectableMethod.getMethodName()).get(0);
            try {
                List<Location> lineList = new ArrayList<>(runMethod.allLineLocations());
                lineList.sort((line1, line2) -> Integer.compare(line1.lineNumber(), line2.lineNumber()));
                setBreakpoint(jdi, eventHandler, lineList.get(0));
            } catch (AbsentInformationException e) {
                throw new RuntimeException(e);
            }
        }

        private static void setBreakpoint(JDIScript jdi, JdiControllerEventHandler eventHandler, Location location) {
            jdi.breakpointRequest(location, eventHandler).enable();
        }

        public Builder setLauncher(VMLauncher launcher) {
            this.launcher = launcher;
            return this;
        }

        public Builder setInspectableMethod(InspectableMethod method) {
            this.inspectableMethod = method;
            return this;
        }

        public Builder setInspectableListener(InspectableInstanceListener inspectableListener) {
            this.inspectableListener = inspectableListener;
            return this;
        }

    }

}
