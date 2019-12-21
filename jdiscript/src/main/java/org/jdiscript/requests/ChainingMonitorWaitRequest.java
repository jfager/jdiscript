package org.jdiscript.requests;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.MonitorWaitRequest;
import java.lang.Object;
import java.lang.String;
import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.handlers.OnMonitorWait;

/**
 * Generated chainable wrapper class for {@link MonitorWaitRequest}
 */
public class ChainingMonitorWaitRequest {
  private final MonitorWaitRequest wrapped;

  public ChainingMonitorWaitRequest(MonitorWaitRequest wrapped) {
    this.wrapped = wrapped;
  }

  public ChainingMonitorWaitRequest addHandler(OnMonitorWait handler) {
    if (handler != null) {
      DebugEventDispatcher.addHandler(wrapped, handler);
    }
    return this;
  }

  /**
   * @see MonitorWaitRequest#addThreadFilter(ThreadReference)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest addThreadFilter(ThreadReference arg0) {
    wrapped.addThreadFilter(arg0);
    return this;
  }

  /**
   * @see MonitorWaitRequest#addClassFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest addClassFilter(String arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see MonitorWaitRequest#addClassFilter(ReferenceType)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest addClassFilter(ReferenceType arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see MonitorWaitRequest#addClassExclusionFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest addClassExclusionFilter(String arg0) {
    wrapped.addClassExclusionFilter(arg0);
    return this;
  }

  /**
   * @see MonitorWaitRequest#addInstanceFilter(ObjectReference)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest addInstanceFilter(ObjectReference arg0) {
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
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest addCountFilter(int arg0) {
    wrapped.addCountFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setSuspendPolicy(int)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest setSuspendPolicy(int arg0) {
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
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest putProperty(Object arg0, Object arg1) {
    wrapped.putProperty(arg0, arg1);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#disable()
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest disable() {
    wrapped.disable();
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setEnabled(boolean)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest setEnabled(boolean arg0) {
    wrapped.setEnabled(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#enable()
   * @return This ChainingMonitorWaitRequest for chainable method calls
   */
  public ChainingMonitorWaitRequest enable() {
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
