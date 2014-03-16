package org.jdiscript.example;

import static org.jdiscript.util.Utils.unchecked;

import org.jdiscript.JDIScript;
import org.jdiscript.util.VMLauncher;

public class HelloWorldExample {
    
    public static void main(final String[] args) {
        String OPTIONS = "-cp ./target/classes/";
        String MAIN = "org.jdiscript.example.HelloWorld";

        JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());
        
        j.onFieldAccess("org.jdiscript.example.HelloWorld", "helloTo", e -> {
            j.onStepInto(e.thread(), j.once(se -> {
                unchecked(() -> e.object().setValue(e.field(), 
                                                 j.vm().mirrorOf("JDIScript!")));
            }));
        });

        j.run();
    }
}
