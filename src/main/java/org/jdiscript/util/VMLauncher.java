package org.jdiscript.util;

import java.io.IOException;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;

public class VMLauncher {
    private final String options, main;
    private final boolean killOnShutdown;
    private Process process;

    /**
     * Create a VMStarter that uses the given options and main class to launch a process for debugging. By default, this
     * initializes a shutdown hook that attempts to kill the created VM when the debugging VM dies. To override this
     * behavior, use the alternate constructor.
     *
     * @param options
     * @param main
     */
    public VMLauncher(String options, String main) {
        this(options, main, true);
    }

    /**
     * Alternate constructor giving control over the initialization of a shutdown hook that attempts to kill the created
     * VM when the debugging VM dies.
     *
     * @param options
     * @param main
     * @param killOnShutdown
     */
    public VMLauncher(String options, String main, boolean killOnShutdown) {
        this.options = options;
        this.main = main;
        this.killOnShutdown = killOnShutdown;
    }

    public VirtualMachine safeStart() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = vmm.defaultConnector();
        Map<String, Argument> cArgs = connector.defaultArguments();
        cArgs.get("options").setValue(options);
        cArgs.get("main").setValue(main);
        final VirtualMachine vm = connector.launch(cArgs);

        process = vm.process();

        if (killOnShutdown) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    vm.process().destroy();
                }
            });
        }

        return vm;
    }

    public Process getProcess() {
        return process;
    }

    /**
     * safeStart wrapped to throw RuntimeException.
     */
    public VirtualMachine start() {
        try {
            return safeStart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
