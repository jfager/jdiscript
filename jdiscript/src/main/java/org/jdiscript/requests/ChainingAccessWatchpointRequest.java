package org.jdiscript.requests;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.AccessWatchpointRequest;
import java.lang.Object;
import java.lang.String;
import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.handlers.OnAccessWatchpoint;

/**
 * Generated chainable wrapper class for {@link AccessWatchpointRequest}
 */
public class ChainingAccessWatchpointRequest {
  private final AccessWatchpointRequest wrapped;

  public ChainingAccessWatchpointRequest(AccessWatchpointRequest wrapped) {
    this.wrapped = wrapped;
  }

  public ChainingAccessWatchpointRequest addHandler(OnAccessWatchpoint handler) {
    if (handler != null) {
      DebugEventDispatcher.addHandler(wrapped, handler);
    }
    return this;
  }

  /**
   * @see com.sun.jdi.request.WatchpointRequest#field()
   * @return The Field returned by the wrapped com.sun.jdi.request.WatchpointRequest
   */
  public Field field() {
    return wrapped.field();
  }

  /**
   * @see com.sun.jdi.request.WatchpointRequest#addThreadFilter(ThreadReference)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest addThreadFilter(ThreadReference arg0) {
    wrapped.addThreadFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.WatchpointRequest#addClassFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest addClassFilter(String arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.WatchpointRequest#addClassFilter(ReferenceType)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest addClassFilter(ReferenceType arg0) {
    wrapped.addClassFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.WatchpointRequest#addClassExclusionFilter(String)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest addClassExclusionFilter(String arg0) {
    wrapped.addClassExclusionFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.WatchpointRequest#addInstanceFilter(ObjectReference)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest addInstanceFilter(ObjectReference arg0) {
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
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest addCountFilter(int arg0) {
    wrapped.addCountFilter(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setSuspendPolicy(int)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest setSuspendPolicy(int arg0) {
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
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest putProperty(Object arg0, Object arg1) {
    wrapped.putProperty(arg0, arg1);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#disable()
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest disable() {
    wrapped.disable();
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#setEnabled(boolean)
   * @param arg0 Unable to match param name, positionally same as wrapped class.
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest setEnabled(boolean arg0) {
    wrapped.setEnabled(arg0);
    return this;
  }

  /**
   * @see com.sun.jdi.request.EventRequest#enable()
   * @return This ChainingAccessWatchpointRequest for chainable method calls
   */
  public ChainingAccessWatchpointRequest enable() {
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
