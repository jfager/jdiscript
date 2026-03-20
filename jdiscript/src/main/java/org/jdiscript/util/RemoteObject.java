package org.jdiscript.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * Utilities for safely inspecting remote objects in a debugged JVM.
 * <p>
 * These methods handle the common boilerplate of invoking methods on
 * remote objects (type hierarchy navigation, exception handling, null
 * safety) so that debugger scripts can focus on their logic.
 *
 * <h2>Example usage</h2>
 * <pre>
 *   // In a breakpoint handler:
 *   String level = RemoteObject.remoteToString(args.get(0), thread);
 *   String loggerName = RemoteObject.invokeRemote(thisObj, "getName",
 *       "()Ljava/lang/String;", thread)
 *       .map(v -&gt; ((StringReference) v).value())
 *       .orElse("&lt;unknown&gt;");
 * </pre>
 */
public class RemoteObject {

    private RemoteObject() {} // utility class

    /**
     * Invoke {@code toString()} on a remote object and return the result.
     * <p>
     * Handles the full complexity of remote invocation: resolving
     * {@code toString()} through the class hierarchy (including interfaces
     * backed by Object), catching all JDI exceptions, and falling back to
     * {@code "TypeName@uniqueId"} if the invocation fails.
     *
     * @param obj    The remote object to stringify.
     * @param thread A suspended thread to use for the invocation.
     * @return The result of {@code obj.toString()} in the remote VM,
     *         or a fallback {@code "type@id"} string.
     */
    public static String remoteToString(ObjectReference obj, ThreadReference thread) {
        if (obj == null) return "null";
        return invokeRemote(obj, "toString", "()Ljava/lang/String;", thread)
            .filter(v -> v instanceof StringReference)
            .map(v -> ((StringReference) v).value())
            .orElseGet(() -> obj.type().name() + "@" + obj.uniqueID());
    }

    /**
     * Invoke a no-arg method on a remote object and return the result.
     * <p>
     * This resolves the method through the class hierarchy (handling both
     * ClassType and InterfaceType), invokes it with
     * {@link ObjectReference#INVOKE_SINGLE_THREADED}, and wraps the result
     * in an {@link Optional}.  Returns {@link Optional#empty()} if the
     * method is not found or any exception occurs.
     *
     * @param obj        The remote object to invoke the method on.
     * @param methodName The method name (e.g. {@code "getName"}).
     * @param methodSig  The JNI-style method signature
     *                   (e.g. {@code "()Ljava/lang/String;"}).
     * @param thread     A suspended thread to use for the invocation.
     * @return The return value, or {@link Optional#empty()} on failure.
     */
    public static Optional<Value> invokeRemote(ObjectReference obj,
                                               String methodName,
                                               String methodSig,
                                               ThreadReference thread) {
        return invokeRemote(obj, methodName, methodSig, thread, Collections.emptyList());
    }

    public static Optional<Value> invokeRemote(ObjectReference obj,
                                               String methodName,
                                               String methodSig,
                                               ThreadReference thread,
                                               List<Value> args) {
        try {
            Method method = resolveMethod(obj, methodName, methodSig);
            if (method == null) return Optional.empty();
            Value result = obj.invokeMethod(
                thread, method, args,
                ObjectReference.INVOKE_SINGLE_THREADED);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Safely convert a {@link Value} to a readable string.
     * <p>
     * For {@code null} values returns {@code "null"}.
     * For {@link StringReference} returns the string value directly.
     * For other {@link ObjectReference} types, invokes {@code toString()}
     * on the remote VM.
     * For primitives, returns {@link Value#toString()}.
     *
     * @param v      The value to convert (may be null).
     * @param thread A suspended thread (needed for remote invocations on objects).
     * @return A human-readable string representation of the value.
     */
    public static String valueToString(Value v, ThreadReference thread) {
        if (v == null) return "null";
        if (v instanceof StringReference sr) return sr.value();
        if (v instanceof ObjectReference obj) return remoteToString(obj, thread);
        return v.toString();
    }

    /**
     * Read an argument from the current stack frame and convert it to a
     * readable string using {@link #valueToString(Value, ThreadReference)}.
     *
     * @param thread A suspended thread.
     * @param index  The 0-based argument index.
     * @return The argument as a readable string, or {@code "<unknown>"}
     *         if the argument cannot be read.
     */
    public static String argToString(ThreadReference thread, int index) {
        try {
            StackFrame frame = thread.frame(0);
            List<Value> args = frame.getArgumentValues();
            if (args.size() > index) {
                return valueToString(args.get(index), thread);
            }
        } catch (IncompatibleThreadStateException e) {
            // thread not suspended
        }
        return "<unknown>";
    }

    /**
     * Read the {@code this} reference from the current stack frame.
     *
     * @param thread A suspended thread.
     * @return The {@code this} object, or {@code null} for static methods
     *         or if the thread is not suspended.
     */
    public static ObjectReference thisObject(ThreadReference thread) {
        try {
            return thread.frame(0).thisObject();
        } catch (IncompatibleThreadStateException e) {
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    /**
     * Resolve a method by name and signature, walking the class hierarchy.
     * Handles ClassType (uses concreteMethodByName) and InterfaceType
     * (falls back to java.lang.Object).
     */
    private static Method resolveMethod(ObjectReference obj,
                                        String methodName,
                                        String methodSig) {
        ReferenceType type = obj.referenceType();
        if (type instanceof ClassType ct) {
            return ct.concreteMethodByName(methodName, methodSig);
        }
        // For interface-typed references (e.g. a proxy object), JDI doesn't expose
        // concrete methods on the interface itself. Fall back to java.lang.Object, which
        // covers the common case of invoking toString()/hashCode()/equals(). For methods
        // declared only on the interface this will return null and invokeRemote will
        // return Optional.empty().
        if (type instanceof InterfaceType) {
            for (ReferenceType rt : obj.virtualMachine().classesByName("java.lang.Object")) {
                if (rt instanceof ClassType objType) {
                    return objType.concreteMethodByName(methodName, methodSig);
                }
            }
        }
        return null;
    }
}
