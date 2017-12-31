/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdiscript.util;

import com.sun.jdi.VirtualMachine;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author satnam-sandhu
 */
public class VMLauncherTest {

    String OPTIONS;
    String MAIN;
    VMLauncher instance;

    public VMLauncherTest() {
        OPTIONS = "-cp ./build/classes/";
        MAIN = "org.jdiscript.example.HelloWorld";
        instance = new VMLauncher(OPTIONS, MAIN);
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
     * Test of safeStart method, of class VMLauncher.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSafeStart() throws Exception {
        System.out.println("safeStart");
        VirtualMachine result = instance.safeStart();
	System.out.println(result);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of start method, of class VMLauncher.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        VirtualMachine result = instance.start();
	System.out.println(result);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

}
