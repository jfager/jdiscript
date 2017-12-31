/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdiscript.example;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.BreakpointRequest;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnAccessWatchpoint;
import org.jdiscript.util.Utils;
import org.jdiscript.util.VMLauncher;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnClassPrepare;
import org.jdiscript.handlers.OnVMStart;
import static org.jdiscript.util.Utils.println;
import static org.jdiscript.util.Utils.repeat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bonnie
 */
public class HelloWorldTest {

    public HelloWorldTest() {
        /*String OPTIONS = "-cp ./target/classes/";
        String MAIN = "org.jdiscript.example.HelloWorld";

        JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());

        Stack<Method> stack = new Stack<>();

        //This references itself and so the compiler complains "Cannot 
        //reference a field before it is defined" if we try to initialize directly.
        OnBreakpoint breakpoint;
        {
            breakpoint = new OnBreakpoint() {
                @Override
                public void breakpoint(BreakpointEvent be) {
                    println(repeat("  ", stack.size()) + "new " + be.location().declaringType().name());
                    stack.push(be.location().method());

                    //We set breakpoints on every constructor but initially only
                    //enabled them for ones we care about (those in the org.jdiscript
                    //package), so we're somewhere inside the constructor call chain
                    //of a constructor we care about. If we're in that initial constructor,
                    //ensure that all of the other constructor breakpoints are enabled for
                    //the current thread, and then disabled when the original method call exits.
                    //Otherwise, just pop the constructor call off the stack when its done.
                    if (stack.size() == 1) {
                        j.breakpointRequests(breakpoint).stream().filter(new Predicate<BreakpointRequest>() {
                            @Override
                            public boolean test(BreakpointRequest bpr) {
                                return !bpr.isEnabled();
                            }
                        }).forEach(new Consumer<BreakpointRequest>() {
                            @Override
                            public void accept(BreakpointRequest bpr) {
                                bpr.addThreadFilter(be.thread());
                                bpr.enable();
                            }
                        });

                        //Make sure that when we leave the interesting method call, we turn
                        //all of the constructor breakpoints back off.
                        Utils.unchecked(new Utils.Block() {
                            @Override
                            public void go() throws Exception {
                                j.onCurrentMethodExit(be.thread(), new OnBreakpoint() {
                                    @Override
                                    public void breakpoint(BreakpointEvent e) {
                                        stack.pop();
                                        if (stack.isEmpty()) {
                                            j.breakpointRequests(breakpoint).forEach(new Consumer<BreakpointRequest>() {
                                                @Override
                                                public void accept(BreakpointRequest bp) {
                                                    bp.setEnabled(bp.location().declaringType().name().startsWith("org.jdiscript"));
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        Utils.unchecked(new Utils.Block() {
                            @Override
                            public void go() throws Exception {
                                j.onCurrentMethodExit(be.thread(), new OnBreakpoint() {
                                    @Override
                                    public void breakpoint(BreakpointEvent e) {
                                        stack.pop();
                                    }
                                });
                            }
                        });
                    }
                }
            };
        }

        OnVMStart start = new OnVMStart() {
            @Override
            public void vmStart(VMStartEvent se) {
                Consumer<ReferenceType> setConstructBrks = new Consumer<ReferenceType>() {
                    @Override
                    public void accept(ReferenceType rt) {
                        rt.methodsByName("<init>").stream()
                                .filter(new Predicate<Method>() {
                                    @Override
                                    public boolean test(Method m) {
                                        return !m.location().declaringType().name().equals("java.lang.Object");
                                    }
                                })
                                .forEach(new Consumer<Method>() {
                                    @Override
                                    public void accept(Method m) {
                                        j.breakpointRequest(m.location(), breakpoint).setEnabled(rt.name().startsWith("org.jdiscript"));
                                    }
                                });
                    }
                };

                j.vm().allClasses().forEach(new Consumer<ReferenceType>() {
                    @Override
                    public void accept(ReferenceType c) {
                        setConstructBrks.accept(c);
                    }
                });
                j.onClassPrep(new OnClassPrepare() {
                    @Override
                    public void classPrepare(ClassPrepareEvent cp) {
                        setConstructBrks.accept(cp.referenceType());
                    }
                });
            }
        };*/
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of sayHello method, of class HelloWorld.
     */
    @Test
    public void testSayHello() {
        /*        System.out.println("sayHello");*/
        //HelloWorld instance = new HelloWorld();
        //String expResult = "";
        //String result = instance.sayHello();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of setHelloTo method, of class HelloWorld.
     */
    @Test
    public void testSetHelloTo() {
        /*String OPTIONS = "-cp ./build/classes/";
        String MAIN = "org.jdiscript.example.HelloWorld";
        String CLASSREF = "org.jdiscript.example.HelloWorld";
        String METHOD = "helloto";

        JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());

        j.onFieldAccess(CLASSREF, METHOD, new OnAccessWatchpoint() {

            @Override
            public void accessWatchpoint(AccessWatchpointEvent e) {
                ThreadReference thread = e.thread();
                //StackFrame stackFrame = null;
                //LocalVariable localVar;
                //String varName = null;

                try {
                    for (StackFrame frame : thread.frames()) {
                        //System.out.println(frame);
                    }
                //tackFrame = thread.frame(0);
                //localVar = stackFrame.visibleVariableByName(varName);
                //Value val = stackFrame.getValue(localVar);
                //System.out.println(val);
                } catch (IncompatibleThreadStateException ex) {
                    Logger.getLogger(HelloWorldTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        j.run();*/
    }

    /**
     * Test of main method, of class HelloWorld.
     */
    @Test
    public void testMain() {
        String OPTIONS = "-cp ./build/classes/";
        String MAIN = "org.jdiscript.example.HelloWorld";
        String CLASSREF = "org.jdiscript.example.HelloWorld";
        String METHOD = "main";

        JDIScript j = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());
        /*j.onMethodInvocation(CLASSREF, METHOD, new OnBreakpoint() {
            @Override
            public void breakpoint(BreakpointEvent event) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });*/

        j.onMethodExit(CLASSREF, METHOD, new OnBreakpoint() {
            @Override
            public void breakpoint(BreakpointEvent e) {
                e.request().disable();
                ThreadReference thread = e.thread();
                
                try {
                    StackFrame stackFrame = thread.frame(0);
                    Map<LocalVariable,Value> visibleVariables = (Map<LocalVariable,Value>) stackFrame.getValues(stackFrame.visibleVariables());
                    for(Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()){
                        System.out.println(entry.getKey() + ":" + entry.getValue());
                    }
                } catch (IncompatibleThreadStateException | AbsentInformationException ex) {
                    Logger.getLogger(HelloWorldTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        j.run();

    }
}
