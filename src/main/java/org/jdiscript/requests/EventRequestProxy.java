package org.jdiscript.requests;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

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
    public static Object proxy(EventRequest origRequest, Class<?> iface) {
        return Proxy.newProxyInstance(iface.getClassLoader(),
                                      new Class<?>[]{ iface },
                                      new EventRequestProxy(origRequest));
    }

    private final EventRequest proxiedRequest;
    private final Map<String, Method> methodCache
        = new HashMap<String, Method>();

    private EventRequestProxy(EventRequest request) {
        this.proxiedRequest = request;
        for(Method m: request.getClass().getMethods()) {
            m.setAccessible(true);
            methodCache.put(m.getName(), m);
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
        String name = method.getName();
        Method m = methodCache.get(name);
        if(m != null) {
            Object out = m.invoke(proxiedRequest, args);
            if(out == null) {
                return proxy;
            } else {
                return out;
            }
        } else if (name.equals("addHandler")) {
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
