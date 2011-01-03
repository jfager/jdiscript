package org.jdiscript;

import org.jdiscript.handlers.OnAccessWatchpoint
import org.jdiscript.handlers.OnClassPrepare
import org.jdiscript.handlers.OnStep
import org.jdiscript.handlers.OnVMStart

import org.jdiscript.util.VMLauncher;
import com.sun.jdi.StringReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest

// The HelloWorld java example code prints out "Hello, <someone>" twice
// for 3 different values of <someone>. This scripts changes the second
// printout for each to "Hello, Groovy".

String OPTIONS = """
    -cp ../../../build/jdiscript.jar
"""
String MAIN = "org.jdiscript.example.HelloWorld"
VirtualMachine vm = new VMLauncher(OPTIONS, MAIN).start()

JDIScript j = new JDIScript(vm);

j.onFieldAccess("org.jdiscript.example.HelloWorld", "helloTo", {
    def obj = it.object()
    def field = it.field()
    j.stepRequest(it.thread(),
                  StepRequest.STEP_MIN,
                  StepRequest.STEP_OVER, {
        StringReference alttobe = j.vm().mirrorOf("Groovy")
        it.request().disable()
        obj.setValue(field, alttobe)
    } as OnStep).enable()
} as OnAccessWatchpoint)

j.run()




