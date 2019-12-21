package org.jdiscript.requests;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.StepRequest;
import java.lang.Object;
import java.lang.String;
import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.handlers.OnStep;

/**
 * Generated chainable wrapper class for {@link StepRequest}
 */
public class ChainingStepRequest {
  private final StepRequest wrapped;

  public ChainingStepRequest(StepRequest wrapped) {
    this.wrapped = wrapped;
  }

  public ChainingStepRequest addHandler(OnStep handler) {
    if (handler != null) {
      DebugEventDispatcher.addHandler(wrapped, handler);
    }
    return this;
  }

  /**
   * @see StepRequest#depth()
   * @return The int returned by the wrapped StepRequest
   */
  public int depth() {
    return wrapped.depth();
  }

  /**
   * @see StepRequest#size()
   * @return The int returned by the wrapped StepRequest
   */
  public int size() {
    return wrapped.size();
  }

  /**
   * @see StepRequest#thread()
   * @return The ThreadReference returned by the wrapped StepRequest
   */
  public ThreadReference thread() {
    return wrapped.thread();
  }

  /**
   * @see StepRequest#addClassFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest addClassFilter(String arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see StepRequest#addClassFilter(ReferenceType)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest addClassFilter(ReferenceType arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see StepRequest#addClassExclusionFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest addClassExclusionFilter(String arg0) {
    wrapped.addClassExclusionFilter(arg0);
    return this;
  }

  /**
   * @see StepRequest#addInstanceFilter(ObjectReference)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest addInstanceFilter(ObjectReference arg0) {
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
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest addCountFilter(int arg0) {
    wrapped.addCountFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setSuspendPolicy(int)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest setSuspendPolicy(int arg0) {
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
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest putProperty(Object arg0, Object arg1) {
    wrapped.putProperty(arg0, arg1);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#disable()
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest disable() {
    wrapped.disable();
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setEnabled(boolean)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest setEnabled(boolean arg0) {
    wrapped.setEnabled(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#enable()
   * @return This ChainingStepRequest for chainable method calls
   */
  public ChainingStepRequest enable() {
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
