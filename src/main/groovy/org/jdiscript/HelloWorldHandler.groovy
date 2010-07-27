package org.jdiscript;

import org.jdiscript.example.HelloWorld;

import org.jdiscript.util.DebugRunner;
import org.jdiscript.util.VMStarter;
import com.sun.jdi.StringReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;



public class HelloWorldHandler extends BaseDebugEventHandler {
	static final String OPTIONS = """
		-D${HelloWorld.HELLO_PROP_KEY}=Property
		-Dlog4j.configuration=file:/Users/Jason/java_shite/log4j.properties
		-cp /Users/jason/projects/jdiscript/target/classes/
	"""
	
	static final String MAIN = "com.jasonfager.debug.HelloWorld"
	
	boolean seenProperty = false
	
	void vmStart( VMStartEvent e ) {
		EventRequestManager erm = e.virtualMachine().eventRequestManager()
		
		//HelloWorld isn't loaded yet, so we can't directly ask to watch
		//any of its fields, yet.  We can ask to be notified when the class
		//gets loaded, though; we'll add the watchpoint then.
		ClassPrepareRequest classprep = erm.createClassPrepareRequest()
		classprep.addClassFilter("com.jasonfager.debug.HelloWorld")
		classprep.enable()
	}
	
	void classPrepare(ClassPrepareEvent e) {
		def field = e.referenceType().fieldByName("helloTo")
		def erm = e.virtualMachine().eventRequestManager()
		erm.createModificationWatchpointRequest(field).enable()
		erm.createAccessWatchpointRequest(field).enable()
	}
	
	void modificationWatchpoint(ModificationWatchpointEvent e) {
		def tobe = e.valueToBe()
		seenProperty = tobe.value().equals("Property")
	}
	
	void accessWatchpoint(AccessWatchpointEvent e) {
		if(seenProperty) {
			StringReference alttobe = e.virtualMachine().mirrorOf("Groovy")
			e.object().setValue(e.field(), alttobe)
		}
	}
	
	static void main( args ) {
		VirtualMachine vm = new VMStarter(OPTIONS, MAIN).start() 
		new DebugRunner(vm, new HelloWorldHandler()).run()
	} 		
}
