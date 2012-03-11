package org.jdiscript.util;

import java.io.IOException;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;

public class VMListener {

	private Map<String, ? extends Connector.Argument> args;
	private ListeningConnector lc;
	
	public VMListener(int port) {
		VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
		this.lc = vmm.listeningConnectors().get(0);
		this.args = lc.defaultArguments();
		args.get("port").setValue(Integer.toString(port));
	}
	
	public String start()
		throws IOException,
		       IllegalConnectorArgumentsException
	{
		String connectString = lc.startListening(args);
		return connectString;
	}
	
	public VirtualMachine next()
		throws IOException,
		       IllegalConnectorArgumentsException
	{
		return lc.accept(args);
	}
}
