package org.jdiscript.requests;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.DebugEventHandler;

import com.sun.jdi.request.EventRequest;

public class EventRequestProxy implements InvocationHandler {
	
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
			Set<DebugEventHandler> handlers 
				= JDIScript.getHandlers(proxiedRequest);
			if(handlers == null) {
				handlers = new HashSet<DebugEventHandler>();
				proxiedRequest.putProperty(JDIScript.PROP_KEY, handlers);
			}			
			DebugEventHandler handler = (DebugEventHandler)args[0];
			handlers.add(handler);
			return proxy;
		} else {
			throw new RuntimeException("Unexpected method: " + method.getName());
		}
	}
}
