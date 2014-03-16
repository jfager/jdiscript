package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMSocketAttacher;

import com.sun.jdi.VirtualMachine;

class ContentionPrinter {   
    public static void main(String[] args) {
        VirtualMachine vm = new VMSocketAttacher(12345).attach();
        JDIScript j = new JDIScript(vm);

        j.monitorContendedEnterRequest(e -> {
            j.printTrace(e, "ContendedEnter for "+e.monitor());
        }).enable();

        j.monitorContendedEnteredRequest(e -> {
            long timestamp = System.currentTimeMillis();
            println(timestamp+": "+e.thread()+": ContendedEntered for "+e.monitor());
        }).enable();
        
        j.run((OnVMStart) e -> { println("Got StartEvent"); });

        println("Shutting down");
    }
}


