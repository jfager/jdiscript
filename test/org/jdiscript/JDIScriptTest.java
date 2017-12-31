/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdiscript;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import java.io.IOException;
import org.jdiscript.util.VMLauncher;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author satnam-sandhu
 */
public class JDIScriptTest {

    String OPTIONS;
    String MAIN;
    VMLauncher instance;
    JDIScript jdiscript;

    public JDIScriptTest() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        OPTIONS = "-cp ./build/classes/";
        MAIN = "org.jdiscript.example.HelloWorld";
        instance = new VMLauncher(OPTIONS, MAIN);
        jdiscript = new JDIScript(new VMLauncher(OPTIONS, MAIN).start());
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
     * Test of vm method, of class JDIScript.
     */
    @Test
    public void testVm() {
        /*System.out.println("vm");
        JDIScript instance = null;
        VirtualMachine expResult = null;
        VirtualMachine result = instance.vm();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    /**
     * Test of run method, of class JDIScript.
     */
    @Test
    public void testRun_0args() {
        /*        System.out.println("run");*/
        //JDIScript instance = null;
        //instance.run();
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of run method, of class JDIScript.
     */
    @Test
    public void testRun_long() {
        /*        System.out.println("run");*/
        //long millis = 0L;
        //JDIScript instance = null;
        //instance.run(millis);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of run method, of class JDIScript.
     */
    @Test
    public void testRun_DebugEventHandler() {
        /*        System.out.println("run");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //instance.run(handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of run method, of class JDIScript.
     */
    @Test
    public void testRun_List() {
        /*        System.out.println("run");*/
        //List<DebugEventHandler> handlers = null;
        //JDIScript instance = null;
        //instance.run(handlers);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of run method, of class JDIScript.
     */
    @Test
    public void testRun_DebugEventHandler_long() {
        /*        System.out.println("run");*/
        //DebugEventHandler handler = null;
        //long millis = 0L;
        //JDIScript instance = null;
        //instance.run(handler, millis);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of run method, of class JDIScript.
     */
    @Test
    public void testRun_List_long() {
        /*        System.out.println("run");*/
        //List<DebugEventHandler> handlers = null;
        //long millis = 0L;
        //JDIScript instance = null;
        //instance.run(handlers, millis);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of accessWatchpointRequest method, of class JDIScript.
     */
    @Test
    public void testAccessWatchpointRequest_Field() {
        /*        System.out.println("accessWatchpointRequest");*/
        //Field field = null;
        //JDIScript instance = null;
        //ChainingAccessWatchpointRequest expResult = null;
        //ChainingAccessWatchpointRequest result = instance.accessWatchpointRequest(field);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of accessWatchpointRequest method, of class JDIScript.
     */
    @Test
    public void testAccessWatchpointRequest_Field_OnAccessWatchpoint() {
        /*        System.out.println("accessWatchpointRequest");*/
        //Field field = null;
        //OnAccessWatchpoint handler = null;
        //JDIScript instance = null;
        //ChainingAccessWatchpointRequest expResult = null;
        //ChainingAccessWatchpointRequest result = instance.accessWatchpointRequest(field, handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of breakpointRequest method, of class JDIScript.
     */
    @Test
    public void testBreakpointRequest_Location() {
        /*        System.out.println("breakpointRequest");*/
        //Location location = null;
        //JDIScript instance = null;
        //ChainingBreakpointRequest expResult = null;
        //ChainingBreakpointRequest result = instance.breakpointRequest(location);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of breakpointRequest method, of class JDIScript.
     */
    @Test
    public void testBreakpointRequest_Location_OnBreakpoint() {
        /*        System.out.println("breakpointRequest");*/
        //Location location = null;
        //OnBreakpoint handler = null;
        //JDIScript instance = null;
        //ChainingBreakpointRequest expResult = null;
        //ChainingBreakpointRequest result = instance.breakpointRequest(location, handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of classPrepareRequest method, of class JDIScript.
     */
    @Test
    public void testClassPrepareRequest_0args() {
        /*        System.out.println("classPrepareRequest");*/
        //JDIScript instance = null;
        //ChainingClassPrepareRequest expResult = null;
        //ChainingClassPrepareRequest result = instance.classPrepareRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of classPrepareRequest method, of class JDIScript.
     */
    @Test
    public void testClassPrepareRequest_OnClassPrepare() {
        /*        System.out.println("classPrepareRequest");*/
        //OnClassPrepare handler = null;
        //JDIScript instance = null;
        //ChainingClassPrepareRequest expResult = null;
        //ChainingClassPrepareRequest result = instance.classPrepareRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of classUnloadRequest method, of class JDIScript.
     */
    @Test
    public void testClassUnloadRequest_0args() {
        /*        System.out.println("classUnloadRequest");*/
        //JDIScript instance = null;
        //ChainingClassUnloadRequest expResult = null;
        //ChainingClassUnloadRequest result = instance.classUnloadRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of classUnloadRequest method, of class JDIScript.
     */
    @Test
    public void testClassUnloadRequest_OnClassUnload() {
        /*        System.out.println("classUnloadRequest");*/
        //OnClassUnload handler = null;
        //JDIScript instance = null;
        //ChainingClassUnloadRequest expResult = null;
        //ChainingClassUnloadRequest result = instance.classUnloadRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of exceptionRequest method, of class JDIScript.
     */
    @Test
    public void testExceptionRequest_3args() {
        /*        System.out.println("exceptionRequest");*/
        //ReferenceType refType = null;
        //boolean notifyCaught = false;
        //boolean notifyUncaught = false;
        //JDIScript instance = null;
        //ChainingExceptionRequest expResult = null;
        //ChainingExceptionRequest result = instance.exceptionRequest(refType, notifyCaught, notifyUncaught);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of exceptionRequest method, of class JDIScript.
     */
    @Test
    public void testExceptionRequest_4args() {
        /*        System.out.println("exceptionRequest");*/
        //ReferenceType refType = null;
        //boolean notifyCaught = false;
        //boolean notifyUncaught = false;
        //OnException handler = null;
        //JDIScript instance = null;
        //ChainingExceptionRequest expResult = null;
        //ChainingExceptionRequest result = instance.exceptionRequest(refType, notifyCaught, notifyUncaught, handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of methodEntryRequest method, of class JDIScript.
     */
    @Test
    public void testMethodEntryRequest_0args() {
        /*        System.out.println("methodEntryRequest");*/
        //JDIScript instance = null;
        //ChainingMethodEntryRequest expResult = null;
        //ChainingMethodEntryRequest result = instance.methodEntryRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of methodEntryRequest method, of class JDIScript.
     */
    @Test
    public void testMethodEntryRequest_OnMethodEntry() {
        /*        System.out.println("methodEntryRequest");*/
        //OnMethodEntry handler = null;
        //JDIScript instance = null;
        //ChainingMethodEntryRequest expResult = null;
        //ChainingMethodEntryRequest result = instance.methodEntryRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of methodExitRequest method, of class JDIScript.
     */
    @Test
    public void testMethodExitRequest_0args() {
        /*        System.out.println("methodExitRequest");*/
        //JDIScript instance = null;
        //ChainingMethodExitRequest expResult = null;
        //ChainingMethodExitRequest result = instance.methodExitRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of methodExitRequest method, of class JDIScript.
     */
    @Test
    public void testMethodExitRequest_OnMethodExit() {
        /*        System.out.println("methodExitRequest");*/
        //OnMethodExit handler = null;
        //JDIScript instance = null;
        //ChainingMethodExitRequest expResult = null;
        //ChainingMethodExitRequest result = instance.methodExitRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of modificationWatchpointRequest method, of class JDIScript.
     */
    @Test
    public void testModificationWatchpointRequest_Field() {
        /*        System.out.println("modificationWatchpointRequest");*/
        //Field field = null;
        //JDIScript instance = null;
        //ChainingModificationWatchpointRequest expResult = null;
        //ChainingModificationWatchpointRequest result = instance.modificationWatchpointRequest(field);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of modificationWatchpointRequest method, of class JDIScript.
     */
    @Test
    public void testModificationWatchpointRequest_Field_OnModificationWatchpoint() {
        /*        System.out.println("modificationWatchpointRequest");*/
        //Field field = null;
        //OnModificationWatchpoint handler = null;
        //JDIScript instance = null;
        //ChainingModificationWatchpointRequest expResult = null;
        //ChainingModificationWatchpointRequest result = instance.modificationWatchpointRequest(field, handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorContendedEnteredRequest method, of class JDIScript.
     */
    @Test
    public void testMonitorContendedEnteredRequest_0args() {
        /*        System.out.println("monitorContendedEnteredRequest");*/
        //JDIScript instance = null;
        //ChainingMonitorContendedEnteredRequest expResult = null;
        //ChainingMonitorContendedEnteredRequest result = instance.monitorContendedEnteredRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorContendedEnteredRequest method, of class JDIScript.
     */
    @Test
    public void testMonitorContendedEnteredRequest_OnMonitorContendedEntered() {
        /*        System.out.println("monitorContendedEnteredRequest");*/
        //OnMonitorContendedEntered handler = null;
        //JDIScript instance = null;
        //ChainingMonitorContendedEnteredRequest expResult = null;
        //ChainingMonitorContendedEnteredRequest result = instance.monitorContendedEnteredRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorContendedEnterRequest method, of class JDIScript.
     */
    @Test
    public void testMonitorContendedEnterRequest_0args() {
        /*        System.out.println("monitorContendedEnterRequest");*/
        //JDIScript instance = null;
        //ChainingMonitorContendedEnterRequest expResult = null;
        //ChainingMonitorContendedEnterRequest result = instance.monitorContendedEnterRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorContendedEnterRequest method, of class JDIScript.
     */
    @Test
    public void testMonitorContendedEnterRequest_OnMonitorContendedEnter() {
        /*        System.out.println("monitorContendedEnterRequest");*/
        //OnMonitorContendedEnter handler = null;
        //JDIScript instance = null;
        //ChainingMonitorContendedEnterRequest expResult = null;
        //ChainingMonitorContendedEnterRequest result = instance.monitorContendedEnterRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorWaitedRequest method, of class JDIScript.
     */
    @Test
    public void testMonitorWaitedRequest_0args() {
        /*        System.out.println("monitorWaitedRequest");*/
        //JDIScript instance = null;
        //ChainingMonitorWaitedRequest expResult = null;
        //ChainingMonitorWaitedRequest result = instance.monitorWaitedRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorWaitedRequest method, of class JDIScript.
     */
    @Test
    public void testMonitorWaitedRequest_OnMonitorWaited() {
        /*        System.out.println("monitorWaitedRequest");*/
        //OnMonitorWaited handler = null;
        //JDIScript instance = null;
        //ChainingMonitorWaitedRequest expResult = null;
        //ChainingMonitorWaitedRequest result = instance.monitorWaitedRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorWaitRequest method, of class JDIScript.
     */
    @Test
    public void testMonitorWaitRequest_0args() {
        /*        System.out.println("monitorWaitRequest");*/
        //JDIScript instance = null;
        //ChainingMonitorWaitRequest expResult = null;
        //ChainingMonitorWaitRequest result = instance.monitorWaitRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorWaitRequest method, of class JDIScript.
     */
    @Test
    public void testMonitorWaitRequest_OnMonitorWait() {
        /*        System.out.println("monitorWaitRequest");*/
        //OnMonitorWait handler = null;
        //JDIScript instance = null;
        //ChainingMonitorWaitRequest expResult = null;
        //ChainingMonitorWaitRequest result = instance.monitorWaitRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of stepRequest method, of class JDIScript.
     */
    @Test
    public void testStepRequest_3args() {
        /*        System.out.println("stepRequest");*/
        //ThreadReference thread = null;
        //int size = 0;
        //int depth = 0;
        //JDIScript instance = null;
        //ChainingStepRequest expResult = null;
        //ChainingStepRequest result = instance.stepRequest(thread, size, depth);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of stepRequest method, of class JDIScript.
     */
    @Test
    public void testStepRequest_4args() {
        /*        System.out.println("stepRequest");*/
        //ThreadReference thread = null;
        //int size = 0;
        //int depth = 0;
        //OnStep handler = null;
        //JDIScript instance = null;
        //ChainingStepRequest expResult = null;
        //ChainingStepRequest result = instance.stepRequest(thread, size, depth, handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of threadDeathRequest method, of class JDIScript.
     */
    @Test
    public void testThreadDeathRequest_0args() {
        /*        System.out.println("threadDeathRequest");*/
        //JDIScript instance = null;
        //ChainingThreadDeathRequest expResult = null;
        //ChainingThreadDeathRequest result = instance.threadDeathRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of threadDeathRequest method, of class JDIScript.
     */
    @Test
    public void testThreadDeathRequest_OnThreadDeath() {
        /*        System.out.println("threadDeathRequest");*/
        //OnThreadDeath handler = null;
        //JDIScript instance = null;
        //ChainingThreadDeathRequest expResult = null;
        //ChainingThreadDeathRequest result = instance.threadDeathRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of threadStartRequest method, of class JDIScript.
     */
    @Test
    public void testThreadStartRequest_0args() {
        /*        System.out.println("threadStartRequest");*/
        //JDIScript instance = null;
        //ChainingThreadStartRequest expResult = null;
        //ChainingThreadStartRequest result = instance.threadStartRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of threadStartRequest method, of class JDIScript.
     */
    @Test
    public void testThreadStartRequest_OnThreadStart() {
        /*        System.out.println("threadStartRequest");*/
        //OnThreadStart handler = null;
        //JDIScript instance = null;
        //ChainingThreadStartRequest expResult = null;
        //ChainingThreadStartRequest result = instance.threadStartRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of vmDeathRequest method, of class JDIScript.
     */
    @Test
    public void testVmDeathRequest_0args() {
        /*        System.out.println("vmDeathRequest");*/
        //JDIScript instance = null;
        //ChainingVMDeathRequest expResult = null;
        //ChainingVMDeathRequest result = instance.vmDeathRequest();
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of vmDeathRequest method, of class JDIScript.
     */
    @Test
    public void testVmDeathRequest_OnVMDeath() {
        /*        System.out.println("vmDeathRequest");*/
        //OnVMDeath handler = null;
        //JDIScript instance = null;
        //ChainingVMDeathRequest expResult = null;
        //ChainingVMDeathRequest result = instance.vmDeathRequest(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of accessWatchpointRequests method, of class JDIScript.
     */
    @Test
    public void testAccessWatchpointRequests() {
        /*        System.out.println("accessWatchpointRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<AccessWatchpointRequest> expResult = null;
        //List<AccessWatchpointRequest> result = instance.accessWatchpointRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of breakpointRequests method, of class JDIScript.
     */
    @Test
    public void testBreakpointRequests() {
        /*        System.out.println("breakpointRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<BreakpointRequest> expResult = null;
        //List<BreakpointRequest> result = instance.breakpointRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of classPrepareRequests method, of class JDIScript.
     */
    @Test
    public void testClassPrepareRequests() {
        /*        System.out.println("classPrepareRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<ClassPrepareRequest> expResult = null;
        //List<ClassPrepareRequest> result = instance.classPrepareRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of classUnloadRequests method, of class JDIScript.
     */
    @Test
    public void testClassUnloadRequests() {
        /*        System.out.println("classUnloadRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<ClassUnloadRequest> expResult = null;
        //List<ClassUnloadRequest> result = instance.classUnloadRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of exceptionRequests method, of class JDIScript.
     */
    @Test
    public void testExceptionRequests() {
        /*        System.out.println("exceptionRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<ExceptionRequest> expResult = null;
        //List<ExceptionRequest> result = instance.exceptionRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of methodEntryRequests method, of class JDIScript.
     */
    @Test
    public void testMethodEntryRequests() {
        /*        System.out.println("methodEntryRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<MethodEntryRequest> expResult = null;
        //List<MethodEntryRequest> result = instance.methodEntryRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of methodExitRequests method, of class JDIScript.
     */
    @Test
    public void testMethodExitRequests() {
        /*        System.out.println("methodExitRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<MethodExitRequest> expResult = null;
        //List<MethodExitRequest> result = instance.methodExitRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of modificationWatchpointRequests method, of class JDIScript.
     */
    @Test
    public void testModificationWatchpointRequests() {
        /*        System.out.println("modificationWatchpointRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<ModificationWatchpointRequest> expResult = null;
        //List<ModificationWatchpointRequest> result = instance.modificationWatchpointRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorContendedEnteredRequests method, of class JDIScript.
     */
    @Test
    public void testMonitorContendedEnteredRequests() {
        /*        System.out.println("monitorContendedEnteredRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<MonitorContendedEnteredRequest> expResult = null;
        //List<MonitorContendedEnteredRequest> result = instance.monitorContendedEnteredRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorContendedEnterRequests method, of class JDIScript.
     */
    @Test
    public void testMonitorContendedEnterRequests() {
        /*        System.out.println("monitorContendedEnterRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<MonitorContendedEnterRequest> expResult = null;
        //List<MonitorContendedEnterRequest> result = instance.monitorContendedEnterRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorWaitedRequests method, of class JDIScript.
     */
    @Test
    public void testMonitorWaitedRequests() {
        /*        System.out.println("monitorWaitedRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<MonitorWaitedRequest> expResult = null;
        //List<MonitorWaitedRequest> result = instance.monitorWaitedRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of monitorWaitRequests method, of class JDIScript.
     */
    @Test
    public void testMonitorWaitRequests() {
        /*        System.out.println("monitorWaitRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<MonitorWaitRequest> expResult = null;
        //List<MonitorWaitRequest> result = instance.monitorWaitRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of stepRequests method, of class JDIScript.
     */
    @Test
    public void testStepRequests() {
        /*        System.out.println("stepRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<StepRequest> expResult = null;
        //List<StepRequest> result = instance.stepRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of threadDeathRequests method, of class JDIScript.
     */
    @Test
    public void testThreadDeathRequests() {
        /*        System.out.println("threadDeathRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<ThreadDeathRequest> expResult = null;
        //List<ThreadDeathRequest> result = instance.threadDeathRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of threadStartRequests method, of class JDIScript.
     */
    @Test
    public void testThreadStartRequests() {
        /*        System.out.println("threadStartRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<ThreadStartRequest> expResult = null;
        //List<ThreadStartRequest> result = instance.threadStartRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of vmDeathRequests method, of class JDIScript.
     */
    @Test
    public void testVmDeathRequests() {
        /*        System.out.println("vmDeathRequests");*/
        //DebugEventHandler handler = null;
        //JDIScript instance = null;
        //List<VMDeathRequest> expResult = null;
        //List<VMDeathRequest> result = instance.vmDeathRequests(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of filter method, of class JDIScript.
     */
    @Test
    public void testFilter() {
        /*        System.out.println("filter");*/
        //JDIScript instance = null;
        //List expResult = null;
        //List result = instance.filter(null);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of deleteEventRequest method, of class JDIScript.
     */
    @Test
    public void testDeleteEventRequest() {
        /*        System.out.println("deleteEventRequest");*/
        //EventRequest eventRequest = null;
        //JDIScript instance = null;
        //instance.deleteEventRequest(eventRequest);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of deleteEventRequests method, of class JDIScript.
     */
    @Test
    public void testDeleteEventRequests() {
        /*        System.out.println("deleteEventRequests");*/
        //List<? extends EventRequest> eventRequests = null;
        //JDIScript instance = null;
        //instance.deleteEventRequests(eventRequests);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of printTrace method, of class JDIScript.
     */
    @Test
    public void testPrintTrace_LocatableEvent() {
        /*        System.out.println("printTrace");*/
        //LocatableEvent event = null;
        //JDIScript instance = null;
        //instance.printTrace(event);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of printTrace method, of class JDIScript.
     */
    @Test
    public void testPrintTrace_LocatableEvent_String() {
        /*        System.out.println("printTrace");*/
        //LocatableEvent event = null;
        //String msg = "";
        //JDIScript instance = null;
        //instance.printTrace(event, msg);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of printTrace method, of class JDIScript.
     */
    @Test
    public void testPrintTrace_3args() {
        /*        System.out.println("printTrace");*/
        //LocatableEvent event = null;
        //String msg = "";
        //PrintStream ps = null;
        //JDIScript instance = null;
        //instance.printTrace(event, msg, ps);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onClassPrep method, of class JDIScript.
     */
    @Test
    public void testOnClassPrep_OnClassPrepare() {
        /*        System.out.println("onClassPrep");*/
        //OnClassPrepare handler = null;
        //JDIScript instance = null;
        //instance.onClassPrep(handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onClassPrep method, of class JDIScript.
     */
    @Test
    public void testOnClassPrep_String_OnClassPrepare() {
        /*        System.out.println("onClassPrep");*/
        //String className = "";
        //OnClassPrepare handler = null;
        //JDIScript instance = null;
        //instance.onClassPrep(className, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onFieldAccess method, of class JDIScript.
     */
    @Test
    public void testOnFieldAccess() {
        /*        System.out.println("onFieldAccess");*/
        //String className = "";
        //String fieldName = "";
        //OnAccessWatchpoint handler = null;
        //JDIScript instance = null;
        //instance.onFieldAccess(className, fieldName, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onFieldModification method, of class JDIScript.
     */
    @Test
    public void testOnFieldModification() {
        /*        System.out.println("onFieldModification");*/
        //String className = "";
        //String fieldName = "";
        //OnModificationWatchpoint handler = null;
        //JDIScript instance = null;
        //instance.onFieldModification(className, fieldName, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onMethodInvocation method, of class JDIScript.
     */
    @Test
    public void testOnMethodInvocation_3args() {
        /*        System.out.println("onMethodInvocation");*/
        //String className = "";
        //String methodName = "";
        //OnBreakpoint handler = null;
        //JDIScript instance = null;
        //instance.onMethodInvocation(className, methodName, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onMethodInvocation method, of class JDIScript.
     */
    @Test
    public void testOnMethodInvocation_4args() {
        /*        System.out.println("onMethodInvocation");*/
        //String className = "";
        //String methodName = "";
        //String methodSig = "";
        //OnBreakpoint handler = null;
        //JDIScript instance = null;
        //instance.onMethodInvocation(className, methodName, methodSig, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onCurrentMethodExit method, of class JDIScript.
     */
    @Test
    public void testOnCurrentMethodExit() throws Exception {
        /*        System.out.println("onCurrentMethodExit");*/
        //ThreadReference thread = null;
        //OnBreakpoint handler = null;
        //JDIScript instance = null;
        //instance.onCurrentMethodExit(thread, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onStep method, of class JDIScript.
     */
    @Test
    public void testOnStep() {
        /*        System.out.println("onStep");*/
        //ThreadReference thread = null;
        //int size = 0;
        //int depth = 0;
        //OnStep handler = null;
        //JDIScript instance = null;
        //instance.onStep(thread, size, depth, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onStepInto method, of class JDIScript.
     */
    @Test
    public void testOnStepInto() {
        /*        System.out.println("onStepInto");*/
        //ThreadReference thread = null;
        //OnStep handler = null;
        //JDIScript instance = null;
        //instance.onStepInto(thread, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onStepOver method, of class JDIScript.
     */
    @Test
    public void testOnStepOver() {
        /*        System.out.println("onStepOver");*/
        //ThreadReference thread = null;
        //OnStep handler = null;
        //JDIScript instance = null;
        //instance.onStepOver(thread, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of onStepOut method, of class JDIScript.
     */
    @Test
    public void testOnStepOut() {
        /*        System.out.println("onStepOut");*/
        //ThreadReference thread = null;
        //OnStep handler = null;
        //JDIScript instance = null;
        //instance.onStepOut(thread, handler);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of once method, of class JDIScript.
     */
    @Test
    public void testOnce() {
        /*        System.out.println("once");*/
        //Object handler = null;
        //JDIScript instance = null;
        //Object expResult = null;
        //Object result = instance.once(handler);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

    /**
     * Test of fullName method, of class JDIScript.
     */
    @Test
    public void testFullName() {
        /*        System.out.println("fullName");*/
        //Method method = null;
        //JDIScript instance = null;
        //String expResult = "";
        //String result = instance.fullName(method);
        //assertEquals(expResult, result);
        //// TODO review the generated test code and remove the default call to fail.
        /*fail("The test case is a prototype.");*/
    }

}
