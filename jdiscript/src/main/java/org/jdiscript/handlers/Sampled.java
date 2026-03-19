package org.jdiscript.handlers;

import org.jdiscript.events.DebugEventDispatcher;

import com.sun.jdi.event.Event;

/**
 * A handler wrapper that fires the inner handler, disables the triggering
 * event request, then re-enables it after a fixed interval.
 * <p>
 * This is the right tool for sampling hot-path breakpoints: by disabling the
 * request for {@code intervalMs} milliseconds after each hit, the event rate
 * is capped at roughly {@code 1000 / intervalMs} events per second regardless
 * of how frequently the target method is called.  A 100 ms interval delivers
 * at most ~10 events/sec and retains ~97.5% throughput even in a tight loop
 * (vs. a ~7,600× slowdown with an always-on {@code SUSPEND_ALL} breakpoint).
 * <p>
 * Usage:
 * <pre>
 *   j.onMethodInvocation("com.example.MyClass", "hotMethod",
 *       j.sampled(100, e -&gt; System.out.println("sampled hit")));
 * </pre>
 * or with a plain breakpointRequest:
 * <pre>
 *   j.breakpointRequest(location, j.sampled(100, e -&gt; inspect(e))).enable();
 * </pre>
 *
 * @see Once
 */
public class Sampled extends BaseEventHandler {

    private final DebugEventHandler handler;
    private final long intervalMs;

    /**
     * @param handler    The inner handler to invoke on each sampled hit.
     * @param intervalMs How long to disable the request after each hit (milliseconds).
     */
    public Sampled(DebugEventHandler handler, long intervalMs) {
        this.handler = handler;
        this.intervalMs = intervalMs;
    }

    @Override
    public void unhandledEvent(Event e) {
        DebugEventDispatcher.doFullDispatch(e, handler);
        e.request().disable();
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException ignored) {}
            try {
                e.request().enable();
            } catch (Exception ignored) {
                // VM may have disconnected by the time we re-enable; silently drop.
            }
        });
    }
}
