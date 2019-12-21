package org.jdiscript.requests;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.MethodExitRequest;
import java.lang.Object;
import java.lang.String;
import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.handlers.OnMethodExit;

/**
 * Generated chainable wrapper class for {@link MethodExitRequest}
 */
public class ChainingMethodExitRequest {
  private final MethodExitRequest wrapped;

  public ChainingMethodExitRequest(MethodExitRequest wrapped) {
    this.wrapped = wrapped;
  }

  public ChainingMethodExitRequest addHandler(OnMethodExit handler) {
    if (handler != null) {
      DebugEventDispatcher.addHandler(wrapped, handler);
    }
    return this;
  }

  /**
   * @see MethodExitRequest#addThreadFilter(ThreadReference)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest addThreadFilter(ThreadReference arg0) {
    wrapped.addThreadFilter(arg0);
    return this;
  }

  /**
   * @see MethodExitRequest#addClassFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest addClassFilter(String arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see MethodExitRequest#addClassFilter(ReferenceType)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest addClassFilter(ReferenceType arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see MethodExitRequest#addClassExclusionFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest addClassExclusionFilter(String arg0) {
    wrapped.addClassExclusionFilter(arg0);
    return this;
  }

  /**
   * @see MethodExitRequest#addInstanceFilter(ObjectReference)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest addInstanceFilter(ObjectReference arg0) {
    wrapped.addInstanceFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#getProperty(Object)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return The Object returned by the wrapped com.sun.jdi.request.EventRequest
   */
  public Object getProperty(Object arg0) {
    return wrapped.getProperty(arg0);
  }

  /**
   * @see com.sun.jdi.request.EventRequest#addCountFilter(int)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest addCountFilter(int arg0) {
    wrapped.addCountFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setSuspendPolicy(int)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest setSuspendPolicy(int arg0) {
    wrapped.setSuspendPolicy(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#suspendPolicy()
   * @return The int returned by the wrapped com.sun.jdi.request.EventRequest
   */
  public int suspendPolicy() {
    return wrapped.suspendPolicy();
  }

  /**
   * @see com.sun.jdi.request.EventRequest#putProperty(Object, Object)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @param arg1 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest putProperty(Object arg0, Object arg1) {
    wrapped.putProperty(arg0, arg1);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#disable()
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest disable() {
    wrapped.disable();
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setEnabled(boolean)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest setEnabled(boolean arg0) {
    wrapped.setEnabled(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#enable()
   * @return This ChainingMethodExitRequest for chainable method calls
   */
  public ChainingMethodExitRequest enable() {
    wrapped.enable();
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#isEnabled()
   * @return The boolean returned by the wrapped com.sun.jdi.request.EventRequest
   */
  public boolean isEnabled() {
    return wrapped.isEnabled();
  }

  /**
   * @see com.sun.jdi.Mirror#toString()
   * @return The String returned by the wrapped com.sun.jdi.Mirror
   */
  public String toString() {
    return wrapped.toString();
  }

  /**
   * @see com.sun.jdi.Mirror#virtualMachine()
   * @return The VirtualMachine returned by the wrapped com.sun.jdi.Mirror
   */
  public VirtualMachine virtualMachine() {
    return wrapped.virtualMachine();
  }
}
