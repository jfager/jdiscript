package org.jdiscript.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdiscript.events.DebugEventHandler;

import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadDeathRequest;

public class TraceExampleHandler extends DebugEventHandler {

	private final String[] excludes; // Packages to exclude

	private Map<ThreadReference, ThreadTrace> traceMap
		= new HashMap<ThreadReference, ThreadTrace>();

	static String nextBaseIndent = ""; // Starting indent for next thread

	private boolean vmDied = false;

	public TraceExampleHandler()
	{
		super();
		this.excludes = new String[0];
	}

	/**
	 * Create the desired event requests, and enable them so that we will get
	 * events.
	 *
	 * @param excludes
	 *            Class patterns for which we don't want events
	 * @param watchFields
	 *            Do we want to watch assignments to fields
	 */
	void setEventRequests(boolean watchFields) {

		// want all exceptions
		ExceptionRequest excReq = exceptionRequest(null, true, true);
		// suspend so we can step
		excReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		excReq.enable();

		MethodEntryRequest menr = methodEntryRequest();
		for (int i = 0; i < excludes.length; ++i) {
			menr.addClassExclusionFilter(excludes[i]);
		}
		menr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		menr.enable();

		MethodExitRequest mexr = methodExitRequest();
		for (int i = 0; i < excludes.length; ++i) {
			mexr.addClassExclusionFilter(excludes[i]);
		}
		mexr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		mexr.enable();

		ThreadDeathRequest tdr = threadDeathRequest();
		// Make sure we sync on thread death
		tdr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		tdr.enable();

		if (watchFields) {
			ClassPrepareRequest cpr = classPrepareRequest();
			for (int i = 0; i < excludes.length; ++i) {
				cpr.addClassExclusionFilter(excludes[i]);
			}
			cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
			cpr.enable();
		}
	}

	/**
	 * Returns the ThreadTrace instance for the specified thread, creating one
	 * if needed.
	 */
	ThreadTrace threadTrace(ThreadReference thread) {
		ThreadTrace trace = traceMap.get(thread);
		if (trace == null) {
			trace = new ThreadTrace(thread);
			traceMap.put(thread, trace);
		}
		return trace;
	}

	@Override
	public void vmStart(VMStartEvent event) {
		System.out.println("-- VM Started --");
		setEventRequests(false);
		try {
			Thread.sleep(1 * 1000);
		} catch(InterruptedException e) {}
	}

	// Forward event for thread specific processing
	@Override
	public void methodEntry(MethodEntryEvent event) {
		threadTrace(event.thread()).methodEntry(event);
	}

	// Forward event for thread specific processing
	@Override
	public void methodExit(MethodExitEvent event) {
		threadTrace(event.thread()).methodExit(event);
	}

	// Forward event for thread specific processing
	@Override
	public void step(StepEvent event) {
		threadTrace(event.thread()).step(event);
	}

	// Forward event for thread specific processing
	public void fieldWatch(ModificationWatchpointEvent event) {
		threadTrace(event.thread()).fieldWatch(event);
	}

	@Override
	public void threadDeath(ThreadDeathEvent event) {
		ThreadTrace trace = traceMap.get(event.thread());
		if (trace != null) { // only want threads we care about
			trace.threadDeath(event); // Forward event
		}
	}

	/**
	 * A new class has been loaded. Set watchpoints on each of its fields
	 */
	@Override
	public void classPrepare(ClassPrepareEvent event) {
		List<Field> fields = event.referenceType().visibleFields();
		for (Field field : fields) {
			ModificationWatchpointRequest req
				= modificationWatchpointRequest(field);
			for (int i = 0; i < excludes.length; ++i) {
				req.addClassExclusionFilter(excludes[i]);
			}
			req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
			req.enable();
		}
	}

	@Override
	public void exception(ExceptionEvent event) {
		ThreadTrace trace = traceMap.get(event.thread());
		if (trace != null) { // only want threads we care about
			trace.exception(event); // Forward event
		}
	}

	@Override
	public void vmDeath(VMDeathEvent event) {
		vmDied = true;
		System.out.println("-- The application exited --");
	}

	@Override
	public void vmDisconnect(VMDisconnectEvent event) {
		if (!vmDied) {
			System.out.println("-- The application has been disconnected --");
		}
	}

	/**
	 * This class keeps context on events in one thread. In this implementation,
	 * context is the indentation prefix.
	 */
	class ThreadTrace {
		final ThreadReference thread;
		final String baseIndent;
		static final String threadDelta = "                     ";
		StringBuffer indent;

		ThreadTrace(ThreadReference thread) {
			this.thread = thread;
			this.baseIndent = nextBaseIndent;
			indent = new StringBuffer(baseIndent);
			nextBaseIndent += threadDelta;
			println("====== " + thread.name() + " ======");
		}

		private void println(String str) {
			System.out.println(indent + str);
		}

		void methodEntry(MethodEntryEvent event) {
			println(event.method().name() + "  --  "
					+ event.method().declaringType().name());
			indent.append("| ");
		}

		void methodExit(MethodExitEvent event) {
			indent.setLength(indent.length() - 2);
		}

		void fieldWatch(ModificationWatchpointEvent event) {
			Field field = event.field();
			Value value = event.valueToBe();
			println("    " + field.name() + " = " + value);
		}

		void exception(ExceptionEvent event) {
			println("Exception: " + event.exception() + " catch: "
					+ event.catchLocation());

			// Step to the catch
			StepRequest req = stepRequest(thread,
					StepRequest.STEP_MIN, StepRequest.STEP_INTO);
			req.addCountFilter(1); // next step only
			req.setSuspendPolicy(EventRequest.SUSPEND_ALL);
			req.enable();
		}

		// Step to exception catch
		void step(StepEvent event) {
			// Adjust call depth
			int cnt = 0;
			indent = new StringBuffer(baseIndent);
			try {
				cnt = thread.frameCount();
			} catch (IncompatibleThreadStateException exc) {
			}
			while (cnt-- > 0) {
				indent.append("| ");
			}

			deleteEventRequest(event.request());
		}

		void threadDeath(ThreadDeathEvent event) {
			indent = new StringBuffer(baseIndent);
			println("====== " + thread.name() + " end ======");
		}
	}
}
