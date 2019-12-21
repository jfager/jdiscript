package org.jdiscript.requests;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.ExceptionRequest;
import java.lang.Object;
import java.lang.String;
import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.handlers.OnException;

/**
 * Generated chainable wrapper class for {@link ExceptionRequest}
 */
public class ChainingExceptionRequest {
  private final ExceptionRequest wrapped;

  public ChainingExceptionRequest(ExceptionRequest wrapped) {
    this.wrapped = wrapped;
  }

  public ChainingExceptionRequest addHandler(OnException handler) {
    if (handler != null) {
      DebugEventDispatcher.addHandler(wrapped, handler);
    }
    return this;
  }

  /**
   * @see ExceptionRequest#exception()
   * @return The ReferenceType returned by the wrapped ExceptionRequest
   */
  public ReferenceType exception() {
    return wrapped.exception();
  }

  /**
   * @see ExceptionRequest#notifyCaught()
   * @return The boolean returned by the wrapped ExceptionRequest
   */
  public boolean notifyCaught() {
    return wrapped.notifyCaught();
  }

  /**
   * @see ExceptionRequest#notifyUncaught()
   * @return The boolean returned by the wrapped ExceptionRequest
   */
  public boolean notifyUncaught() {
    return wrapped.notifyUncaught();
  }

  /**
   * @see ExceptionRequest#addThreadFilter(ThreadReference)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest addThreadFilter(ThreadReference arg0) {
    wrapped.addThreadFilter(arg0);
    return this;
  }

  /**
   * @see ExceptionRequest#addClassFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest addClassFilter(String arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see ExceptionRequest#addClassFilter(ReferenceType)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest addClassFilter(ReferenceType arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see ExceptionRequest#addClassExclusionFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest addClassExclusionFilter(String arg0) {
    wrapped.addClassExclusionFilter(arg0);
    return this;
  }

  /**
   * @see ExceptionRequest#addInstanceFilter(ObjectReference)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest addInstanceFilter(ObjectReference arg0) {
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
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest addCountFilter(int arg0) {
    wrapped.addCountFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setSuspendPolicy(int)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest setSuspendPolicy(int arg0) {
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
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest putProperty(Object arg0, Object arg1) {
    wrapped.putProperty(arg0, arg1);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#disable()
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest disable() {
    wrapped.disable();
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setEnabled(boolean)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest setEnabled(boolean arg0) {
    wrapped.setEnabled(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#enable()
   * @return This ChainingExceptionRequest for chainable method calls
   */
  public ChainingExceptionRequest enable() {
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
