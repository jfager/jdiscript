package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jdiscript.handlers.OnVMDeath;
import org.jdiscript.util.JDISession;
import org.jdiscript.util.RemoteObject;
import org.jdiscript.util.VMSocketAttacher;

import com.sun.jdi.event.BreakpointEvent;

/**
 * Demonstrates {@link JDISession} — the entry point for interactive and
 * agentic debugging workflows.
 *
 * <p>Shows three patterns that become possible once the event loop is
 * non-blocking:
 *
 * <ol>
 *   <li><b>Point-in-time inspection with {@code withSuspend}</b> — pause the
 *       VM, read state, resume, without needing a breakpoint event.
 *       Useful for "what is the count/state of X right now?" queries.</li>
 *   <li><b>Sequential event awaiting with {@code awaitMethodInvocation}</b> —
 *       register interest in one event, wait for it, inspect the result, decide
 *       what to do next.  Enables a query-driven loop instead of declaring all
 *       handlers upfront.</li>
 *   <li><b>Hybrid: standing handlers + selective awaiting</b> — combine
 *       always-on instrumentation with one-shot awaiting in the same session.
 *       </li>
 * </ol>
 *
 * <p>Run against a target JVM started with:
 * <pre>
 *   java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:5005 \
 *        -cp ... com.example.MyServer
 * </pre>
 *
 * <p>For demonstration this example attaches via socket and then exercises
 * the HelloWorld class; in production replace the class/method names and
 * remove the VMLauncher shim.
 */
public class InteractiveExample {

    // In a real scenario this would be:
    //   JDISession session = JDISession.start(new VMSocketAttacher(5005).attach());
    // Here we simulate a running server by using the HelloWorld target.
    static final String TARGET_CLASS  = "org.jdiscript.example.HelloWorld";
    static final String TARGET_METHOD = "main";

    public static void main(String[] args) throws Exception {
        // ---------------------------------------------------------------
        // Start the event loop without blocking — the current thread is
        // free to do other work (read commands, serve HTTP, etc.)
        // ---------------------------------------------------------------
        JDISession session = JDISession.start(
            new org.jdiscript.util.VMLauncher(
                "-cp ./build/classes/java/example", TARGET_CLASS).start(),
            List.of(
                (OnVMDeath) e -> println("[VM exiting]")
            )
        );

        // ---------------------------------------------------------------
        // Pattern 1: Point-in-time inspection — no breakpoint needed.
        //
        // In a real server scenario you'd call this periodically from a
        // monitoring thread, an HTTP health endpoint, or an agent tool call.
        // ---------------------------------------------------------------
        long count = session.withSuspend(
            () -> session.j.instanceCount(TARGET_CLASS));
        println("[withSuspend] live " + TARGET_CLASS + " instances: " + count);

        // ---------------------------------------------------------------
        // Pattern 2: Sequential awaiting — ask one question at a time.
        //
        // The CompletableFuture returns as soon as the method is hit.
        // The agent can inspect the result and decide what to inspect next,
        // without pre-declaring all its questions.
        // ---------------------------------------------------------------
        session.then(
            session.awaitMethodInvocation(TARGET_CLASS, TARGET_METHOD),
            (BreakpointEvent e) -> {
                println("[awaitMethodInvocation] hit: " + e.location());
                Map<String, String> locals = RemoteObject.locals(e.thread());
                if (!locals.isEmpty()) {
                    println("  locals:");
                    locals.forEach((name, val) -> println("    " + name + " = " + val));
                }
            });

        // ---------------------------------------------------------------
        // Pattern 3: Standing handlers + selective awaiting in one session.
        //
        // Register always-on exception tracking, then await a specific event
        // for deeper one-time inspection, all in the same event loop.
        // ---------------------------------------------------------------
        session.j.onException(false, true, e ->
            println("[onException] uncaught: " + e.exception().referenceType().name()));

        // (In a real scenario, an agent might loop here awaiting further events
        // based on what it observed above, e.g.:)
        //   while (!done) {
        //       String next = agent.decideNextTarget();
        //       session.then(
        //           session.awaitMethodInvocation(className, next),
        //           e -> agent.process(e));
        //   }

        // Wait for the target to finish (or call session.close() to disconnect
        // without killing the target).
        session.join();
        println("[done]");
    }
}
