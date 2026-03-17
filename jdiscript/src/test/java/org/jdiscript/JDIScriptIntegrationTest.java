package org.jdiscript;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;
import org.junit.jupiter.api.Test;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;

/**
 * Integration tests that launch a real target VM and verify JDI event
 * handling works end-to-end.
 *
 * The target class ({@link Target}) is compiled alongside these tests
 * into build/classes/java/test/.
 */
class JDIScriptIntegrationTest {

    /** Simple target program used by integration tests. */
    public static class Target {
        static String greeting = "hello";

        public static void sayHello() {
            System.out.println(greeting);
        }

        public static void main(String[] args) {
            sayHello();
            sayHello();
        }
    }

    private JDIScript launchTarget() {
        String cp = System.getProperty("java.class.path");
        String main = Target.class.getName();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        VirtualMachine vm = new VMLauncher("-cp " + cp, main, false, out, err).start();
        return new JDIScript(vm);
    }

    @Test
    void run_receives_VMStart_event() {
        JDIScript j = launchTarget();
        AtomicBoolean started = new AtomicBoolean();

        OnVMStart handler = e -> started.set(true);
        j.run(handler);
        assertTrue(started.get(), "VMStart event should have been received");
    }

    @Test
    void breakpoint_fires_on_target_method() {
        JDIScript j = launchTarget();
        List<BreakpointEvent> events = Collections.synchronizedList(new ArrayList<>());

        OnVMStart start = se -> {
            j.onClassPrep(Target.class.getName(), cp -> {
                cp.referenceType().methodsByName("sayHello").forEach(m ->
                    j.breakpointRequest(m.location(), e -> events.add(e)).enable()
                );
            });
        };
        j.run(start);

        assertEquals(2, events.size(), "sayHello is called twice, so 2 breakpoint events expected");
    }

    @Test
    void onClassPrep_fires_for_target_class() {
        JDIScript j = launchTarget();
        AtomicReference<String> preparedClass = new AtomicReference<>();

        OnVMStart start = se -> {
            j.onClassPrep(Target.class.getName(), cp ->
                preparedClass.set(cp.referenceType().name())
            );
        };
        j.run(start);

        assertEquals(Target.class.getName(), preparedClass.get());
    }

    @Test
    void onThreadStart_fires() {
        JDIScript j = launchTarget();
        List<String> threadNames = Collections.synchronizedList(new ArrayList<>());

        OnVMStart start = se -> {
            j.onThreadStart(e -> threadNames.add(e.thread().name()));
        };
        j.run(start);

        assertFalse(threadNames.isEmpty(), "At least one thread start event should fire");
    }

    @Test
    void fullName_formats_method_signature() {
        JDIScript j = launchTarget();
        AtomicReference<String> name = new AtomicReference<>();

        OnVMStart start = se -> {
            j.onClassPrep(Target.class.getName(), cp -> {
                cp.referenceType().methodsByName("sayHello").forEach(m ->
                    name.set(j.fullName(m))
                );
            });
        };
        j.run(start);

        assertEquals(Target.class.getName() + ".sayHello()", name.get());
    }
}
