package org.jdiscript.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private final OutputStream out, err;

    /**
     * Create a VMStarter that uses the given options and main class to launch a
     * process for debugging. By default, this initializes a shutdown hook that
     * attempts to kill the created VM when the debugging VM dies. To override
     * this behavior, use the alternate constructor.
     *
     * @param options
     * @param main
     */
    public VMLauncher(String options, String main) {
        this(options, main, true, System.out, System.err);
    }

    /**
     * Alternate constructor giving control over the initialization of a
     * shutdown hook that attempts to kill the created VM when the debugging VM
     * dies.
     *
     * @param options
     * @param main
     * @param killOnShutdown
     * @param out
     * @param err
     */
    public VMLauncher(String options, String main, boolean killOnShutdown, OutputStream out, OutputStream err) {
        this.options = options;
        this.main = main;
        this.killOnShutdown = killOnShutdown;
        this.out = out;
        this.err = err;
    }

    public VirtualMachine safeStart() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = vmm.defaultConnector();
        Map<String, Argument> cArgs = connector.defaultArguments();
        cArgs.get("options").setValue(options);
        cArgs.get("main").setValue(main);
        final VirtualMachine vm = connector.launch(cArgs);

        final Thread outThread = redirect("Subproc stdout", vm.process().getInputStream(), out);
        final Thread errThread = redirect("Subproc stderr", vm.process().getErrorStream(), err);
        if (killOnShutdown) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    outThread.interrupt();
                    errThread.interrupt();
                    vm.process().destroy();
                }
            });
        }

        return vm;
    }

    /**
     * safeStart wrapped to throw RuntimeException.
     *
     * @return
     */
    public VirtualMachine start() {
        try {
            return safeStart();
        } catch (IllegalConnectorArgumentsException | VMStartException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Thread redirect(String name, InputStream in, OutputStream out) {
        Thread t = new StreamRedirectThread(name, in, out);
        t.setDaemon(true);
        t.start();
        return t;
    }
}
