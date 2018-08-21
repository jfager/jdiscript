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
     * Create a VMStarter that uses the given options and main class to launch
     * a process for debugging.  By default, this initializes a shutdown hook
     * that attempts to kill the created VM when the debugging VM dies.
     * To override this behavior, use the alternate constructor.  This pumps VM
     * output to {@link System#out} and {@link System#err} by default.
     *
     * @param options Options to pass to VM on launch.
     * @param main Name of main class to run.
     */
    public VMLauncher(String options, String main) {
        this(options, main, true, System.out, System.err);
    }

    /**
     * Alternate constructor giving control over the initialization of a
     * shutdown hook that attempts to kill the created VM when the debugging
     * VM dies.
     *
     * @param options Options to pass to VM on launch.
     * @param main Name of main class to run.
     * @param killOnShutdown True to attempt to kill created VM when debugging VM dies.
     * @param out Sink for the launched VM's stdout output.
     * @param err Sink for the launched VM's stderr output.
     */
    public VMLauncher(String options, String main, boolean killOnShutdown,
                      OutputStream out, OutputStream err)
    {
        this.options = options;
        this.main = main;
        this.killOnShutdown = killOnShutdown;
        this.out = out;
        this.err = err;
    }

    /**
     * Start the VirtualMachine w/ exceptions you can catch.
     *
     * @throws java.io.IOException when unable to launch.
     * Specific exceptions are dependent on the Connector implementation
     * in use.
     * @throws IllegalConnectorArgumentsException when one of the
     * connector arguments is invalid.
     * @throws VMStartException when the VM was successfully launched, but
     * terminated with an error before a connection could be established.
     * @return The started VirtualMachine
     */
    public VirtualMachine safeStart()
        throws IOException,
               IllegalConnectorArgumentsException,
               VMStartException
    {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = vmm.defaultConnector();
        Map<String, Argument> cArgs = connector.defaultArguments();
        cArgs.get("options").setValue(options);
        cArgs.get("main").setValue(main);
        final VirtualMachine vm = connector.launch(cArgs);

        final Thread outThread = redirect("Subproc stdout",
                                          vm.process().getInputStream(),
                                          out);
        final Thread errThread = redirect("Subproc stderr",
                                          vm.process().getErrorStream(),
                                          err);
        if(killOnShutdown) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                outThread.interrupt();
                errThread.interrupt();
                vm.process().destroy();
            }));
        }

        return vm;
    }
    
    /**
     * {@link #safeStart} wrapped to throw RuntimeException.
     *
     * @throws RuntimeException If underlying {@link #safeStart} call throws a checked exception.
     * @return The started VirtualMachine.
     */
    public VirtualMachine start() {
        try {
            return safeStart();
        } catch(Exception e) {
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
