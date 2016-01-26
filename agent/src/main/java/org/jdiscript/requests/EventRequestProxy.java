package org.jdiscript.requests;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jdiscript.handlers.DebugEventHandler;
import org.jdiscript.events.DebugEventDispatcher;

import com.sun.jdi.request.EventRequest;

/**
 * An EventRequestProxy wraps a JDI {@link EventRequest} in order to make it
 * chainable, by returning the EventRequestProxy itself for void EventRequest
 * configuration methods (non-void EventRequest methods are unaffected).
 * <p>
 * Additionally, EventRequestProxy provides the common implementation of the
 * addHandler method specified by JDIScriptEventRequest subinterfaces.
 */
public class EventRequestProxy implements InvocationHandler {

    /**
     * Convenience method for creating a new dynamic proxy instance
     * using an EventRequestProxy.
     *
     * @param origRequest   The EventRequest to proxy.
     * @param iface         The interface to implement.  This should be one
     *                      of the {@link JDIScriptEventRequest} subinterfaces.
     * @return A dynamic proxy instance implementing the given interface
     *         using an EventRequestProxy.
     */
    @SuppressWarnings("unchecked")
    public static <T> T proxy(EventRequest origRequest, Class<T> iface) {
        return (T)Proxy.newProxyInstance(iface.getClassLoader(),
                                         new Class<?>[]{ iface },
                                         new EventRequestProxy(origRequest));
    }

    private final EventRequest proxiedRequest;
    private final Map<MethodKey, Method> methodCache = new HashMap<>();

    private static class MethodKey {
        public static MethodKey from(Method m) {
            return new MethodKey(m.getName(), Arrays.toString(m.getParameters()));
        }
        
        final String name;
        final String args;
        public MethodKey(String name, String args) {
            this.name = name;
            this.args = args;
        }
        @Override
        public boolean equals(Object that) {
            return that instanceof MethodKey
                && Objects.equals(this.name, ((MethodKey)that).name) 
                && Objects.equals(this.args, ((MethodKey)that).args);
        }
        public int hashCode() {
            return Objects.hash(name, args);
        }
        public String toString() {
            return name + args;
        }
    }
    private EventRequestProxy(EventRequest request) {
        this.proxiedRequest = request;
        for(Method m: request.getClass().getMethods()) {
            m.setAccessible(true);
            methodCache.put(MethodKey.from(m), m);
        }
    }

    /**
     * Dynamic proxy invocation method for chaining.
     * <p>
     * For all methods defined for the underlying EventRequest, the underlying
     * method is invoked.  If that method is void, return the proxy object for
     * chaining.  Otherwise, return the result of the underlying method.
     * <p>
     * For invocations of addHandler, add the given DebugEventHandler
     * according to jdiscript's handler conventions
     * ({@link DebugEventDispatcher#addHandler}).
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        MethodKey key = MethodKey.from(method);
        Method m = methodCache.get(key);
        if(m != null) {
            Object out = m.invoke(proxiedRequest, args);
            if(out == null) {
                return proxy;
            } else {
                return out;
            }
        } else if (method.getName().equals("addHandler")) {
            Object handlerObj = args[0];
            if(handlerObj == null) {
                return proxy;
            }

            DebugEventHandler handler = null;
            if(handlerObj instanceof DebugEventHandler) {
                handler = (DebugEventHandler)handlerObj;
            } else {
                //TODO: handler = new ReflectionHandler(handlerObj);
                throw new RuntimeException("Can't yet handle non-DebugEventHandlers");
            }

            DebugEventDispatcher.addHandler(proxiedRequest, handler);
            return proxy;
        } else {
            throw new RuntimeException("Unexpected method: " + method.getName());
        }
    }
}
