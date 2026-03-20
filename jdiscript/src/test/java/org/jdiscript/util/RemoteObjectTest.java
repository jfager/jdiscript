package org.jdiscript.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sun.jdi.*;
import org.junit.jupiter.api.Test;

class RemoteObjectTest {

    // --- remoteToString ---

    @Test
    void remoteToString_null_object_returns_null_string() {
        ThreadReference thread = mock(ThreadReference.class);
        assertEquals("null", RemoteObject.remoteToString(null, thread));
    }

    // --- valueToString ---

    @Test
    void valueToString_null_returns_null_string() {
        ThreadReference thread = mock(ThreadReference.class);
        assertEquals("null", RemoteObject.valueToString(null, thread));
    }

    @Test
    void valueToString_StringReference_returns_value_directly() {
        ThreadReference thread = mock(ThreadReference.class);
        StringReference sr = mock(StringReference.class);
        when(sr.value()).thenReturn("hello world");
        assertEquals("hello world", RemoteObject.valueToString(sr, thread));
    }

    @Test
    void valueToString_primitive_returns_toString() {
        // A plain Value mock that is neither StringReference nor ObjectReference
        // falls through to the primitive branch (v.toString()).
        ThreadReference thread = mock(ThreadReference.class);
        Value v = mock(Value.class);
        when(v.toString()).thenReturn("42");
        assertEquals("42", RemoteObject.valueToString(v, thread));
    }

    // --- argToString ---

    @Test
    void argToString_returns_unknown_when_thread_not_suspended() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        when(thread.frame(0)).thenThrow(new IncompatibleThreadStateException());
        assertEquals("<unknown>", RemoteObject.argToString(thread, 0));
    }

    @Test
    void argToString_returns_unknown_when_index_out_of_bounds() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        StackFrame frame = mock(StackFrame.class);
        when(thread.frame(0)).thenReturn(frame);
        when(frame.getArgumentValues()).thenReturn(java.util.Collections.emptyList());
        assertEquals("<unknown>", RemoteObject.argToString(thread, 0));
    }

    @Test
    void argToString_returns_string_value_for_StringReference_arg() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        StackFrame frame = mock(StackFrame.class);
        StringReference sr = mock(StringReference.class);
        when(sr.value()).thenReturn("foo");
        when(thread.frame(0)).thenReturn(frame);
        when(frame.getArgumentValues()).thenReturn(java.util.List.of(sr));
        assertEquals("foo", RemoteObject.argToString(thread, 0));
    }

    // --- thisObject ---

    @Test
    void thisObject_returns_null_when_thread_not_suspended() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        when(thread.frame(0)).thenThrow(new IncompatibleThreadStateException());
        assertNull(RemoteObject.thisObject(thread));
    }

    @Test
    void thisObject_returns_object_from_frame() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        StackFrame frame = mock(StackFrame.class);
        ObjectReference obj = mock(ObjectReference.class);
        when(thread.frame(0)).thenReturn(frame);
        when(frame.thisObject()).thenReturn(obj);
        assertSame(obj, RemoteObject.thisObject(thread));
    }
}
