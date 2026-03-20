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
    void run_no_args_completes_when_vm_exits() {
        JDIScript j = launchTarget();
        // Should return once the target VM finishes (no handler, no timeout)
        assertDoesNotThrow(() -> j.run());
    }

    @Test
    void run_with_timeout_completes_within_limit() {
        JDIScript j = launchTarget();
        assertDoesNotThrow(() -> j.run(10_000));
    }

    @Test
    void run_with_handler_list_receives_VMStart_event() {
        JDIScript j = launchTarget();
        AtomicBoolean started = new AtomicBoolean();

        OnVMStart handler = e -> started.set(true);
        j.run(java.util.List.of(handler));
        assertTrue(started.get(), "VMStart event should have been received");
    }

    @Test
    void onMethodInvocation_fires_breakpoint_on_named_method() {
        JDIScript j = launchTarget();
        List<String> hits = Collections.synchronizedList(new ArrayList<>());

        OnVMStart start = se -> {
            j.onMethodInvocation(Target.class.getName(), "sayHello",
                e -> hits.add(e.location().method().name()));
        };
        j.run(start);

        assertEquals(2, hits.size(), "sayHello is called twice");
        assertTrue(hits.stream().allMatch(n -> n.equals("sayHello")));
    }

    @Test
    void onMethodInvocation_with_signature_fires_breakpoint() {
        JDIScript j = launchTarget();
        List<String> hits = Collections.synchronizedList(new ArrayList<>());

        OnVMStart start = se -> {
            j.onMethodInvocation(Target.class.getName(), "sayHello", "()V",
                e -> hits.add(e.location().method().name()));
        };
        j.run(start);

        assertEquals(2, hits.size());
    }

    @Test
    void onFieldAccess_fires_when_field_is_read() {
        JDIScript j = launchTarget();
        List<String> fieldNames = Collections.synchronizedList(new ArrayList<>());

        OnVMStart start = se -> {
            j.onFieldAccess(Target.class.getName(), "greeting",
                e -> fieldNames.add(e.field().name()));
        };
        j.run(start);

        assertFalse(fieldNames.isEmpty(), "greeting field should have been accessed");
        assertTrue(fieldNames.stream().allMatch(n -> n.equals("greeting")));
    }

    @Test
    void onFieldModification_fires_when_field_is_written() {
        JDIScript j = launchTarget();
        List<String> fieldNames = Collections.synchronizedList(new ArrayList<>());

        OnVMStart start = se -> {
            // Trigger a field modification via the static initializer / class prep
            j.onFieldModification(Target.class.getName(), "greeting",
                e -> fieldNames.add(e.field().name()));
        };
        j.run(start);

        // The static field is initialized when the class loads, so at least one modification fires
        assertFalse(fieldNames.isEmpty(), "greeting field should have been modified");
    }

    @Test
    void onMethodExit_fires_when_method_exits() {
        JDIScript j = launchTarget();
        List<String> methodNames = Collections.synchronizedList(new ArrayList<>());

        OnVMStart start = se -> {
            j.onMethodExit(Target.class.getName(),
                e -> methodNames.add(e.method().name()));
        };
        j.run(start);

        assertFalse(methodNames.isEmpty(), "at least one method exit should fire");
    }

    @Test
    void nearestCaller_returns_caller_location_from_breakpoint() {
        JDIScript j = launchTarget();
        AtomicReference<String> callerTypeName = new AtomicReference<>();

        OnVMStart start = se -> {
            j.onClassPrep(Target.class.getName(), cp -> {
                cp.referenceType().methodsByName("sayHello").forEach(m ->
                    j.breakpointRequest(m.location(), e -> {
                        // Find the next frame after sayHello — should be Target.main
                        String sayHelloType = e.location().declaringType().name();
                        com.sun.jdi.Location caller = j.nearestCaller(
                            loc -> !loc.declaringType().name().equals(sayHelloType)
                                || !loc.method().name().equals("sayHello"),
                            e.thread());
                        if (caller != null) {
                            callerTypeName.set(caller.declaringType().name());
                        }
                    }).enable()
                );
            });
        };
        j.run(start);

        // Frame 1 is the caller of sayHello (Target.main or something in Target)
        assertNotNull(callerTypeName.get(), "nearestCaller should find a frame");
    }

    @Test
    void stacktraceKey_returns_non_empty_string_from_breakpoint() {
        JDIScript j = launchTarget();
        AtomicReference<String> key = new AtomicReference<>();

        OnVMStart start = se -> {
            j.onClassPrep(Target.class.getName(), cp -> {
                cp.referenceType().methodsByName("sayHello").forEach(m ->
                    j.breakpointRequest(m.location(), e -> {
                        key.set(j.stacktraceKey(e.thread()));
                    }).enable()
                );
            });
        };
        j.run(start);

        assertNotNull(key.get());
        assertTrue(key.get().contains("sayHello"), "stacktrace key should mention sayHello");
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
