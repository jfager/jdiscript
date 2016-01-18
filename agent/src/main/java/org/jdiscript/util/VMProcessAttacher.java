package org.jdiscript.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

public class VMProcessAttacher {
    private final int pid;
    private final int timeout;

    public VMProcessAttacher(int pid) {
        this(pid, 0);
    }

    public VMProcessAttacher(int pid, int timeout) {
        this.pid = pid;
        this.timeout = timeout;
    }

    public VirtualMachine attach()
        throws IOException, IllegalConnectorArgumentsException
    {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<AttachingConnector> connectors = vmm.attachingConnectors();
        AttachingConnector connector = null;
        for(AttachingConnector c: connectors) {
            if(c.defaultArguments().get("pid") != null) {
                connector = c;
            }
        }
        Map<String, Argument> cArgs = connector.defaultArguments();
        cArgs.get("pid").setValue(Integer.toString(pid));
        cArgs.get("timeout").setValue(Integer.toString(timeout));
        final VirtualMachine vm = connector.attach(cArgs);
        return vm;
    }
}
