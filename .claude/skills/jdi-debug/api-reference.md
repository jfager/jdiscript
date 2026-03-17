# jdiscript API Reference

## Connecting to a JVM

All connectors are in `org.jdiscript.util`.

```java
// Attach via socket (most common)
VirtualMachine vm = new VMSocketAttacher(port).attach();
VirtualMachine vm = new VMSocketAttacher(host, port).attach();
VirtualMachine vm = new VMSocketAttacher(host, port, timeout).attach();

// Attach via process ID
VirtualMachine vm = new VMProcessAttacher(pid).attach();

// Launch a new debuggee process
VirtualMachine vm = new VMLauncher(options, mainClass).start();

// Listen for incoming connections
VMListener listener = new VMListener(port);
listener.start();
VirtualMachine vm = listener.next();
```

## JDIScript — Main Entry Point

Constructor: `new JDIScript(VirtualMachine vm)`

### Convenience methods (recommended starting points)

These handle class loading, null locations, and event plumbing automatically.

| Method | Description |
|--------|-------------|
| `onMethodInvocation(className, methodName, handler)` | Break on method entry. Handles abstract/native methods safely. |
| `onMethodInvocation(className, methodName, methodSig, handler)` | Same, for overloaded methods (JNI signature). |
| `onClassPrep(className, handler)` | Trigger handler when a class is first loaded. |
| `onClassPrep(handler)` | Trigger handler on any class preparation. |
| `onFieldAccess(className, fieldName, handler)` | Break on field read. |
| `onFieldModification(className, fieldName, handler)` | Break on field write. |
| `onException(notifyCaught, notifyUncaught, handler)` | Track exceptions (excludes java.*/sun.*/jdk.*). |
| `onThreadStart(handler)` | Monitor thread creation. |
| `onThreadDeath(handler)` | Monitor thread termination. |
| `onMethodExit(className, handler)` | Monitor method exits in a class. |
| `onStepInto(thread, handler)` | Single-step into calls. |
| `onStepOver(thread, handler)` | Single-step over calls. |
| `onStepOut(thread, handler)` | Step out of current method. |
| `onCurrentMethodExitUnchecked(thread, handler)` | Break when current method returns. |

### Low-level request builders

Return chainable `Chaining*Request` objects. Call `.enable()` to activate.

| Method | Returns |
|--------|---------|
| `breakpointRequest(Location, OnBreakpoint)` | `ChainingBreakpointRequest` |
| `classPrepareRequest(OnClassPrepare)` | `ChainingClassPrepareRequest` |
| `exceptionRequest(refType, caught, uncaught, OnException)` | `ChainingExceptionRequest` |
| `methodEntryRequest(OnMethodEntry)` | `ChainingMethodEntryRequest` |
| `methodExitRequest(OnMethodExit)` | `ChainingMethodExitRequest` |
| `stepRequest(thread, size, depth, OnStep)` | `ChainingStepRequest` |
| `accessWatchpointRequest(Field, OnAccessWatchpoint)` | `ChainingAccessWatchpointRequest` |
| `modificationWatchpointRequest(Field, OnModificationWatchpoint)` | `ChainingModificationWatchpointRequest` |
| `monitorContendedEnterRequest(OnMonitorContendedEnter)` | `ChainingMonitorContendedEnterRequest` |
| `monitorContendedEnteredRequest(OnMonitorContendedEntered)` | `ChainingMonitorContendedEnteredRequest` |
| `monitorWaitRequest(OnMonitorWait)` | `ChainingMonitorWaitRequest` |
| `monitorWaitedRequest(OnMonitorWaited)` | `ChainingMonitorWaitedRequest` |
| `threadStartRequest(OnThreadStart)` | `ChainingThreadStartRequest` |
| `threadDeathRequest(OnThreadDeath)` | `ChainingThreadDeathRequest` |
| `vmDeathRequest(OnVMDeath)` | `ChainingVMDeathRequest` |

All request builders also have a no-handler overload (add handler later with
`.addHandler()`).

### Stack analysis utilities

| Method | Description |
|--------|-------------|
| `nearestCaller(packagePrefix, thread)` | Find nearest frame matching a package prefix. |
| `nearestCaller(Predicate<Location>, thread)` | Find nearest frame matching a predicate. |
| `stacktraceKey(thread)` | Generate histogram-friendly stacktrace string. |
| `printTrace(event)` | Print stacktrace to stdout. |
| `printTrace(event, msg)` | Print stacktrace with a message prefix. |
| `printTrace(event, msg, printStream)` | Print stacktrace to a given stream. |

### Event loop

| Method | Description |
|--------|-------------|
| `run()` | Run until VM exits. |
| `run(millis)` | Run with timeout. |
| `run(List<DebugEventHandler>)` | Run with VM death/disconnect handlers. |
| `run(List<DebugEventHandler>, millis)` | Run with handlers and timeout. |

### Other utilities

| Method | Description |
|--------|-------------|
| `vm()` | Get the underlying `VirtualMachine`. |
| `once(handler)` | Wrap handler to fire only once (disables request after first hit). |
| `fullName(Method)` | Format as `"Type.method(arg, arg, ...)"`. |
| `deleteEventRequest(request)` | Remove an event request. |

## RemoteObject — Safe Remote Inspection

In `org.jdiscript.util.RemoteObject`. All methods are static.

| Method | Description |
|--------|-------------|
| `remoteToString(obj, thread)` | Invoke `toString()` on a remote object. Falls back to `"type@id"`. |
| `invokeRemote(obj, methodName, methodSig, thread)` | Invoke any no-arg method. Returns `Optional<Value>`. |
| `valueToString(value, thread)` | Convert any JDI `Value` to a readable string. |
| `argToString(thread, index)` | Read argument at index, convert to readable string. |
| `thisObject(thread)` | Get `this` reference from current frame. |

**Method signatures use JNI format:**
- `()Ljava/lang/String;` — no args, returns String
- `()V` — no args, returns void
- `(I)V` — takes int, returns void
- `(Ljava/lang/String;)V` — takes String, returns void

## Event Handler Interfaces

All in `org.jdiscript.handlers`. All are `@FunctionalInterface` — use lambdas.

| Interface | Event | Key accessor |
|-----------|-------|--------------|
| `OnBreakpoint` | Breakpoint hit | `e.location()`, `e.thread()` |
| `OnClassPrepare` | Class loaded | `e.referenceType()` |
| `OnException` | Exception thrown | `e.exception()`, `e.catchLocation()` |
| `OnMethodEntry` | Method entered | `e.method()`, `e.thread()` |
| `OnMethodExit` | Method exited | `e.method()`, `e.returnValue()` |
| `OnStep` | Step completed | `e.location()`, `e.thread()` |
| `OnAccessWatchpoint` | Field read | `e.field()`, `e.valueCurrent()` |
| `OnModificationWatchpoint` | Field written | `e.field()`, `e.valueCurrent()`, `e.valueToBe()` |
| `OnThreadStart` | Thread created | `e.thread()` |
| `OnThreadDeath` | Thread died | `e.thread()` |
| `OnMonitorContendedEnter` | Waiting for lock | `e.monitor()`, `e.thread()` |
| `OnMonitorContendedEntered` | Acquired lock | `e.monitor()`, `e.thread()` |
| `OnMonitorWait` | Entering wait() | `e.monitor()`, `e.timeout()` |
| `OnMonitorWaited` | Exiting wait() | `e.monitor()`, `e.timedout()` |
| `OnVMDeath` | VM shutting down | — |
| `OnVMDisconnect` | Debug connection lost | — |
| `OnVMStart` | VM started | `e.thread()` |

## Utils

In `org.jdiscript.util.Utils`:

| Method | Description |
|--------|-------------|
| `println(String)` | Print to stdout. |
| `unchecked(Block)` | Execute a block, wrapping checked exceptions as RuntimeException. |
