package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;

import java.util.List;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.ReferenceType;

class JustIterators {   
        
    static String OPTIONS = "-cp ./build/classes/java/example";
    static String MAIN = "org.jdiscript.example.HelloWorld";       

    public static void main(String[] args) {
        JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());
        
        OnVMStart start = se -> {
            List<ReferenceType> rts = j.vm().classesByName("java.util.Iterator");
            j.methodEntryRequest(me -> {
                println("Found Iterator: " + me.location().method().name() + " line " + me.location().lineNumber());
            }).addClassFilter(rts.get(0))
              .enable();
        };
        
        j.run(start);
    }
}


