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

public class VMSocketAttacher {
    private final String host;
    private final int port;
    private final int timeout;

    public VMSocketAttacher(int port) {
        this(null, port);
    }

    public VMSocketAttacher(int port, int timeout) {
        this(null, port, timeout);
    }

    public VMSocketAttacher(String host, int port) {
        this(host, port, 0);
    }

    public VMSocketAttacher(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public VirtualMachine attach()
        throws IOException, IllegalConnectorArgumentsException
    {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<AttachingConnector> connectors = vmm.attachingConnectors();
        AttachingConnector connector = null;
        for(AttachingConnector c: connectors) {
            if(c.defaultArguments().get("hostname") != null) {
                connector = c;
            }
        }
        Map<String, Argument> cArgs = connector.defaultArguments();
        cArgs.get("port").setValue(Integer.toString(port));
        cArgs.get("timeout").setValue(Integer.toString(timeout));
        if(host != null) {
            cArgs.get("hostname").setValue(host);
        }
        final VirtualMachine vm = connector.attach(cArgs);
        return vm;
    }
}
