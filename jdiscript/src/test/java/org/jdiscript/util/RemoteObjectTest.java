package org.jdiscript.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sun.jdi.*;
import org.junit.jupiter.api.Test;

class RemoteObjectTest {

    // --- remoteToString ---

    @Test
    void remoteToString_null_object_returns_null_string() {
        ThreadReference thread = mock(ThreadReference.class);
        assertEquals("null", RemoteObject.remoteToString(null, thread));
    }

    @Test
    void remoteToString_invokes_remote_toString_via_ClassType() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        ObjectReference obj = mock(ObjectReference.class);
        ClassType classType = mock(ClassType.class);
        Method method = mock(Method.class);
        StringReference result = mock(StringReference.class);

        when(obj.referenceType()).thenReturn(classType);
        when(classType.concreteMethodByName("toString", "()Ljava/lang/String;")).thenReturn(method);
        when(obj.invokeMethod(thread, method, Collections.emptyList(),
                ObjectReference.INVOKE_SINGLE_THREADED)).thenReturn(result);
        when(result.value()).thenReturn("remote-hello");

        assertEquals("remote-hello", RemoteObject.remoteToString(obj, thread));
    }

    @Test
    void remoteToString_invokes_remote_toString_via_InterfaceType() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        ObjectReference obj = mock(ObjectReference.class);
        InterfaceType interfaceType = mock(InterfaceType.class);
        VirtualMachine vm = mock(VirtualMachine.class);
        ClassType objectClassType = mock(ClassType.class);
        Method method = mock(Method.class);
        StringReference result = mock(StringReference.class);

        when(obj.referenceType()).thenReturn(interfaceType);
        when(obj.virtualMachine()).thenReturn(vm);
        when(vm.classesByName("java.lang.Object")).thenReturn(List.of(objectClassType));
        when(objectClassType.concreteMethodByName("toString", "()Ljava/lang/String;")).thenReturn(method);
        when(obj.invokeMethod(thread, method, Collections.emptyList(),
                ObjectReference.INVOKE_SINGLE_THREADED)).thenReturn(result);
        when(result.value()).thenReturn("interface-result");

        assertEquals("interface-result", RemoteObject.remoteToString(obj, thread));
    }

    @Test
    void remoteToString_falls_back_when_result_is_not_StringReference() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        ObjectReference obj = mock(ObjectReference.class);
        ClassType classType = mock(ClassType.class);
        Method method = mock(Method.class);
        Value nonStringResult = mock(Value.class);  // not a StringReference

        when(obj.referenceType()).thenReturn(classType);
        when(classType.concreteMethodByName("toString", "()Ljava/lang/String;")).thenReturn(method);
        when(obj.invokeMethod(thread, method, Collections.emptyList(),
                ObjectReference.INVOKE_SINGLE_THREADED)).thenReturn(nonStringResult);
        when(obj.type()).thenReturn(classType);
        when(classType.name()).thenReturn("SomeClass");
        when(obj.uniqueID()).thenReturn(99L);

        assertEquals("SomeClass@99", RemoteObject.remoteToString(obj, thread));
    }

    // --- invokeRemote ---

    @Test
    void invokeRemote_with_args_passes_args_to_invocation() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        ObjectReference obj = mock(ObjectReference.class);
        ClassType classType = mock(ClassType.class);
        Method method = mock(Method.class);
        StringReference result = mock(StringReference.class);
        Value arg = mock(Value.class);

        when(obj.referenceType()).thenReturn(classType);
        when(classType.concreteMethodByName("format", "(Ljava/lang/Object;)Ljava/lang/String;"))
            .thenReturn(method);
        when(obj.invokeMethod(thread, method, List.of(arg),
                ObjectReference.INVOKE_SINGLE_THREADED)).thenReturn(result);
        when(result.value()).thenReturn("formatted");

        Optional<Value> out = RemoteObject.invokeRemote(
            obj, "format", "(Ljava/lang/Object;)Ljava/lang/String;", thread, List.of(arg));
        assertTrue(out.isPresent());
        assertSame(result, out.get());
    }

    @Test
    void invokeRemote_no_arg_overload_still_works() throws Exception {
        // The existing no-arg overload must keep working after refactoring to delegate.
        ThreadReference thread = mock(ThreadReference.class);
        ObjectReference obj = mock(ObjectReference.class);
        ClassType classType = mock(ClassType.class);
        Method method = mock(Method.class);
        StringReference result = mock(StringReference.class);

        when(obj.referenceType()).thenReturn(classType);
        when(classType.concreteMethodByName("toString", "()Ljava/lang/String;")).thenReturn(method);
        when(obj.invokeMethod(thread, method, Collections.emptyList(),
                ObjectReference.INVOKE_SINGLE_THREADED)).thenReturn(result);
        when(result.value()).thenReturn("str");

        Optional<Value> out = RemoteObject.invokeRemote(obj, "toString", "()Ljava/lang/String;", thread);
        assertTrue(out.isPresent());
    }

    @Test
    void invokeRemote_returns_empty_when_method_not_found() {
        ThreadReference thread = mock(ThreadReference.class);
        ObjectReference obj = mock(ObjectReference.class);
        ClassType classType = mock(ClassType.class);

        when(obj.referenceType()).thenReturn(classType);
        when(classType.concreteMethodByName(anyString(), anyString())).thenReturn(null);

        assertEquals(Optional.empty(),
            RemoteObject.invokeRemote(obj, "missing", "()V", thread));
    }

    @Test
    void invokeRemote_returns_empty_on_exception() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        ObjectReference obj = mock(ObjectReference.class);
        ClassType classType = mock(ClassType.class);
        Method method = mock(Method.class);

        when(obj.referenceType()).thenReturn(classType);
        when(classType.concreteMethodByName(anyString(), anyString())).thenReturn(method);
        when(obj.invokeMethod(any(), any(), any(), anyInt()))
            .thenThrow(new RuntimeException("invocation failed"));

        assertEquals(Optional.empty(),
            RemoteObject.invokeRemote(obj, "toString", "()Ljava/lang/String;", thread));
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
    void valueToString_ObjectReference_delegates_to_remoteToString() throws Exception {
        ThreadReference thread = mock(ThreadReference.class);
        ObjectReference obj = mock(ObjectReference.class);
        ClassType classType = mock(ClassType.class);
        Method method = mock(Method.class);
        StringReference result = mock(StringReference.class);

        when(obj.referenceType()).thenReturn(classType);
        when(classType.concreteMethodByName("toString", "()Ljava/lang/String;")).thenReturn(method);
        when(obj.invokeMethod(thread, method, Collections.emptyList(),
                ObjectReference.INVOKE_SINGLE_THREADED)).thenReturn(result);
        when(result.value()).thenReturn("obj-string");

        assertEquals("obj-string", RemoteObject.valueToString(obj, thread));
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
