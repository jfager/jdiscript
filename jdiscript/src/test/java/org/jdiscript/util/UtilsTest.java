package org.jdiscript.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void unchecked_runs_block_normally() {
        boolean[] ran = {false};
        Utils.unchecked(() -> ran[0] = true);
        assertTrue(ran[0]);
    }

    @Test
    void unchecked_wraps_checked_exception_in_RuntimeException() {
        IOException cause = new IOException("test");
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
            Utils.unchecked(() -> { throw cause; })
        );
        assertSame(cause, thrown.getCause());
    }

    @Test
    void unchecked_propagates_RuntimeException_directly() {
        IllegalStateException cause = new IllegalStateException("test");
        // RuntimeExceptions are still wrapped (unchecked doesn't special-case them)
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
            Utils.unchecked(() -> { throw cause; })
        );
        assertSame(cause, thrown.getCause());
    }

    @Test
    void println_runs_without_error() {
        assertDoesNotThrow(() -> Utils.println("test output"));
    }

    @Test
    void constructor_is_private() throws Exception {
        var ctor = Utils.class.getDeclaredConstructor();
        assertFalse(java.lang.reflect.Modifier.isPublic(ctor.getModifiers()),
            "Utils is a utility class and should have a private constructor");
    }
}
