package org.jdiscript.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jdiscript.JDIScript;
import org.jdiscript.events.DebugEventDispatcher;
import org.jdiscript.events.EventThread;
import org.jdiscript.handlers.DebugEventHandler;
import org.jdiscript.handlers.OnBreakpoint;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

/**
 * An interactive debugging session backed by a non-blocking event loop.
 * <p>
 * {@link JDISession} is the entry point for <em>interactive and agentic</em>
 * debugging workflows — situations where a script does not know all the
 * questions it wants to ask upfront.  Contrast with {@link JDIScript#run()},
 * which blocks until the target VM exits:
 *
 * <ul>
 *   <li><b>Scripting mode</b> ({@code run()}) — register all handlers, then
 *       block.  Good for automated, fire-and-forget analysis.</li>
 *   <li><b>Interactive mode</b> ({@link #start}) — start the loop, then drive
 *       it from external input: a JShell REPL, stdin commands, HTTP endpoints,
 *       or AI agent tool calls.</li>
 * </ul>
 *
 * <h2>Usage patterns</h2>
 *
 * <h3>REPL / JShell</h3>
 * <pre>
 *   var session = JDISession.start(new VMSocketAttacher(5005).attach());
 *   session.j.instanceCount("com.example.UserSession")  // → 42
 *   session.withSuspend(() -&gt; session.j.instanceCount("com.example.Connection"))
 *   session.close()
 * </pre>
 *
 * <h3>Agent query loop</h3>
 * <pre>
 *   var session = JDISession.start(vm);
 *   // Agent decides to watch the next call to UserService.findUser:
 *   var future = session.awaitMethodInvocation("com.example.UserService", "findUser");
 *   // Agent does something to trigger that call, then:
 *   var event = future.get(10, SECONDS);
 *   var userId = RemoteObject.argToString(event.thread(), 0);
 *   // Agent processes the result and decides what to inspect next...
 *   session.close();
 * </pre>
 *
 * <h3>Hybrid (script + interactive tail)</h3>
 * <pre>
 *   var session = JDISession.start(vm);
 *   // Set up standing handlers as usual:
 *   session.j.onException(true, true, e -&gt; log(e));
 *   // Then hand off to an interactive loop:
 *   readCommands(session); // reads from stdin or network
 *   session.join(); // block until target exits
 * </pre>
 *
 * <p>The event loop runs as a <em>daemon thread</em>, so the JVM can exit
 * normally even if the target VM is still connected.  Call {@link #join()} if
 * you want the current thread to block until the target exits.
 */
public class JDISession implements AutoCloseable {

    /**
     * The underlying {@link JDIScript} instance.  Public for ergonomic access
     * in REPL and scripting contexts ({@code session.j.instanceCount(...)}).
     */
    public final JDIScript j;

    private final EventThread eventThread;

    private JDISession(JDIScript j, EventThread eventThread) {
        this.j = j;
        this.eventThread = eventThread;
    }

    // ---------------------------------------------------------------
    // Factory methods
    // ---------------------------------------------------------------

    /**
     * Attach to an already-connected VM and start the event loop.
     *
     * @param vm The target VM (typically from {@link VMSocketAttacher}).
     * @return A running session.
     */
    public static JDISession start(VirtualMachine vm) {
        return start(vm, Collections.emptyList());
    }

    /**
     * Attach to an already-connected VM, register VM-level event handlers
     * (VMDeath, VMDisconnect, etc.), and start the event loop.
     *
     * @param vm       The target VM.
     * @param handlers VM-level event handlers.
     * @return A running session.
     */
    public static JDISession start(VirtualMachine vm, List<DebugEventHandler> handlers) {
        JDIScript j = new JDIScript(vm);
        EventThread et = j.start(handlers);
        return new JDISession(j, et);
    }

    // ---------------------------------------------------------------
    // Synchronous inspection
    // ---------------------------------------------------------------

    /**
     * Suspend the target VM, run {@code work}, then resume.
     * <p>
     * Use this to read state at a specific moment — instance counts, field
     * values, thread stacks — without needing a breakpoint event.  The VM
     * suspension is reference-counted, so calling this inside a handler is
     * safe.
     *
     * @param work Code to run while the VM is suspended.
     */
    public void withSuspend(Runnable work) {
        j.withSuspend(work);
    }

    /**
     * Like {@link #withSuspend(Runnable)} but returns a value.
     *
     * @param <T>  The return type.
     * @param work Code to run while the VM is suspended.
     * @return Whatever {@code work} returns.
     */
    public <T> T withSuspend(Supplier<T> work) {
        return j.withSuspend(work);
    }

    // ---------------------------------------------------------------
    // One-shot event awaiting
    // ---------------------------------------------------------------

    /**
     * Await the next invocation of a method and return a future that completes
     * when it fires.
     * <p>
     * This is the building block for sequential, query-driven debugging: the
     * caller registers interest in one event, waits for it, inspects the result,
     * then decides what to do next — without pre-declaring all questions upfront.
     * <p>
     * If the class is <em>already loaded</em> in the target VM, breakpoints are
     * set directly on all matching methods and removed as soon as one fires.  If
     * the class is <em>not yet loaded</em>, a ClassPrepare watch is used instead
     * (matching the {@link JDIScript#onMethodInvocation} contract).
     * <p>
     * Call {@link CompletableFuture#orTimeout(long, java.util.concurrent.TimeUnit)}
     * (Java 9+) to add a timeout:
     * <pre>
     *   BreakpointEvent e = session
     *       .awaitMethodInvocation("com.example.Foo", "bar")
     *       .orTimeout(10, SECONDS)
     *       .join();
     * </pre>
     *
     * @param className  Fully-qualified class name.
     * @param methodName Method name (all overloads are covered).
     * @return A future that completes with the {@link BreakpointEvent} of the
     *         first hit.
     */
    public CompletableFuture<BreakpointEvent> awaitMethodInvocation(
            String className, String methodName) {
        CompletableFuture<BreakpointEvent> future = new CompletableFuture<>();
        List<EventRequest> requests = Collections.synchronizedList(new ArrayList<>());

        OnBreakpoint handler = e -> {
            if (future.complete(e)) {
                // Clean up all overload breakpoints created for this await
                j.deleteEventRequests(new ArrayList<>(requests));
            }
        };

        List<ReferenceType> loaded = j.vm().classesByName(className);
        if (!loaded.isEmpty()) {
            // Class is already in the VM: set breakpoints directly.
            // Use the low-level EventRequestManager so we get BreakpointRequest
            // (an EventRequest subtype) rather than ChainingBreakpointRequest.
            for (ReferenceType type : loaded) {
                type.methodsByName(methodName).forEach(m -> {
                    if (m.location() != null) {
                        BreakpointRequest br = j.vm().eventRequestManager()
                            .createBreakpointRequest(m.location());
                        DebugEventDispatcher.addHandler(br, handler);
                        br.enable();
                        requests.add(br);
                    }
                });
            }
        } else {
            // Class not yet loaded: defer via ClassPrepare (like onMethodInvocation)
            j.onMethodInvocation(className, methodName, j.once(handler));
        }

        return future;
    }

    /**
     * Run {@code action} once the given future completes, on the calling thread.
     * <p>
     * Convenience for the common pattern of chaining an inspection step after
     * {@link #awaitMethodInvocation}:
     * <pre>
     *   session.then(
     *       session.awaitMethodInvocation("com.example.Foo", "bar"),
     *       e -&gt; System.out.println("arg0=" + RemoteObject.argToString(e.thread(), 0)));
     * </pre>
     *
     * @param <T>    The future's value type.
     * @param future The future to wait on.
     * @param action The action to run with the result.
     */
    public <T> void then(CompletableFuture<T> future, Consumer<T> action) {
        action.accept(future.join());
    }

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    /**
     * Block the current thread until the target VM exits or the session is
     * closed.  Equivalent to calling {@link Thread#join()} on the event thread.
     */
    public void join() {
        try {
            eventThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Disconnect from the target VM.
     * <p>
     * The target process is <em>not killed</em> — it continues running.
     * Stops the background event thread by interrupting it, which triggers
     * {@link VirtualMachine#dispose()}.
     */
    @Override
    public void close() {
        eventThread.interrupt();
    }
}
