package org.jdiscript.example;

import static org.jdiscript.util.Utils.println;
import static org.jdiscript.util.Utils.unchecked;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnVMDeath;
import org.jdiscript.handlers.OnVMDisconnect;
import org.jdiscript.util.RemoteObject;
import org.jdiscript.util.VMSocketAttacher;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

/**
 * Attaches to a running JVM and traces how the logging framework gets
 * configured. Answers the question "where are my log settings actually
 * coming from?" by intercepting Log4j 1.x, Log4j 2.x, SLF4J binding,
 * Logback, and JUL initialization as they happen.
 *
 * <p>Produces a timestamped timeline of every configuration event:
 * which config files were loaded, which logging backend was selected,
 * every programmatic level change, and where log output is being sent.
 *
 * <h2>Usage</h2>
 * <p>Start your target JVM with JDWP enabled:
 * <pre>
 *   -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
 * </pre>
 *
 * <p>For Dataproc Spark jobs, add to your submit command:
 * <pre>
 *   --driver-java-options "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
 * </pre>
 * and SSH-tunnel to the driver node:
 * <pre>
 *   gcloud compute ssh CLUSTER-m -- -L 5005:localhost:5005
 * </pre>
 *
 * <p>Then run this tool:
 * <pre>
 *   java -cp ... org.jdiscript.example.LogConfigDebugger [host] port
 * </pre>
 *
 * <p>If you want to catch the very first config events, use {@code suspend=y}
 * on the target JVM so it waits for this debugger to attach before proceeding.
 *
 * @see <a href="https://github.com/jfager/jdiscript">jdiscript</a>
 */
public class LogConfigDebugger {

    private final JDIScript j;

    // Timeline of observed logging config events
    private final List<LogEvent> timeline = new ArrayList<>();

    // Track all logger configurations we discover
    private final Map<String, String> effectiveLoggers = new LinkedHashMap<>();

    // Deduplication: track last event to collapse repeated identical events
    private LogEvent lastEvent = null;
    private int repeatCount = 0;

    LogConfigDebugger(JDIScript j) {
        this.j = j;
    }

    // ------------------------------------------------------------------
    // Event recording
    // ------------------------------------------------------------------

    static class LogEvent {
        final Instant timestamp;
        final String category;
        final String description;
        final String caller;

        LogEvent(String category, String description, String caller) {
            this.timestamp = Instant.now();
            this.category = category;
            this.description = description;
            this.caller = caller;
        }

        boolean sameAs(LogEvent other) {
            if (other == null) return false;
            return category.equals(other.category)
                && description.equals(other.description);
        }

        @Override
        public String toString() {
            return String.format("[%s] %-18s %s", timestamp, category, description)
                + (caller != null ? "\n      at " + caller : "");
        }
    }

    private void record(String category, String description, ThreadReference thread) {
        String caller = callerLocation(thread);
        LogEvent event = new LogEvent(category, description, caller);

        // Collapse repeated identical events (same category + description)
        if (event.sameAs(lastEvent)) {
            repeatCount++;
            timeline.add(event);
            return; // suppress printing
        }

        // Flush any pending repeat count
        if (repeatCount > 0) {
            println("      ... repeated " + repeatCount + " more time(s)");
            repeatCount = 0;
        }

        lastEvent = event;
        timeline.add(event);
        println(event.toString());
    }

    private void flushRepeats() {
        if (repeatCount > 0) {
            println("      ... repeated " + repeatCount + " more time(s)");
            repeatCount = 0;
        }
    }

    // ------------------------------------------------------------------
    // Caller location — walks up the stack past framework/JDK internals
    // ------------------------------------------------------------------

    private String callerLocation(ThreadReference thread) {
        if (thread == null) return null;
        Location loc = j.nearestCaller(
            l -> !isFrameworkOrJdkInternal(l.declaringType().name()), thread);
        return loc != null ? loc.toString() : null;
    }

    private static boolean isFrameworkOrJdkInternal(String className) {
        return className.startsWith("org.apache.log4j.")
            || className.startsWith("org.apache.logging.log4j.")
            || className.startsWith("org.slf4j.")
            || className.startsWith("ch.qos.logback.")
            || className.startsWith("java.util.logging.")
            || className.startsWith("java.lang.")
            || className.startsWith("java.security.")
            || className.startsWith("java.io.")
            || className.startsWith("java.net.")
            || className.startsWith("java.util.")
            || className.startsWith("jdk.")
            || className.startsWith("sun.")
            || className.startsWith("com.sun.");
    }

    // ------------------------------------------------------------------
    // Value extraction helpers — delegates to upstream RemoteObject
    // ------------------------------------------------------------------

    /** Read 'this' object's name via a getName() call. */
    private static String thisName(ThreadReference thread) {
        ObjectReference thisObj = RemoteObject.thisObject(thread);
        if (thisObj == null) return "<root>";
        return RemoteObject.invokeRemote(thisObj, "getName",
                "()Ljava/lang/String;", thread)
            .filter(v -> v instanceof StringReference)
            .map(v -> {
                String name = ((StringReference) v).value();
                return (name == null || name.isEmpty()) ? "<root>" : name;
            })
            .orElseGet(() -> RemoteObject.remoteToString(thisObj, thread));
    }

    /** Read first argument as a raw String (no remote invoke, for hot paths). */
    private static String firstArgAsString(ThreadReference thread) {
        try {
            StackFrame frame = thread.frame(0);
            List<Value> args = frame.getArgumentValues();
            if (!args.isEmpty()) {
                Value v = args.get(0);
                if (v instanceof StringReference sr) return sr.value();
                if (v == null) return "null";
                return v.type().name() + "@" + ((ObjectReference) v).uniqueID();
            }
        } catch (IncompatibleThreadStateException e) {
            // fall through
        }
        return "<unknown>";
    }

    // ------------------------------------------------------------------
    // Intercept setup — each method registers breakpoints for a
    // particular logging framework. They are all guarded by onClassPrep
    // so they only activate if the framework is actually on the classpath.
    // ------------------------------------------------------------------

    void setupInterceptors() {
        interceptLog4j1();
        interceptLog4j2();
        interceptSlf4j();
        interceptLogback();
        interceptJUL();
        interceptSystemProperties();
        interceptClassLoaderGetResource();
        interceptSparkLogging();
    }

    // --- Log4j 1.x ---

    private void interceptLog4j1() {
        // PropertyConfigurator.doConfigure
        j.onClassPrep("org.apache.log4j.PropertyConfigurator", cp -> {
            cp.referenceType().methodsByName("doConfigure").forEach(m -> {
                if (m.argumentTypeNames().size() >= 1) {
                    j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                        String configSource = RemoteObject.argToString(e.thread(), 0);
                        record("LOG4J1-CONFIG", "PropertyConfigurator.doConfigure(" + configSource + ")", e.thread());
                    }).enable();
                }
            });
        });

        // DOMConfigurator.doConfigure — XML config
        j.onClassPrep("org.apache.log4j.xml.DOMConfigurator", cp -> {
            cp.referenceType().methodsByName("doConfigure").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String configSource = RemoteObject.argToString(e.thread(), 0);
                    record("LOG4J1-CONFIG", "DOMConfigurator.doConfigure(" + configSource + ")", e.thread());
                }).enable();
            });
        });

        // Category.setLevel — runtime level changes (uses getName() for logger name)
        j.onClassPrep("org.apache.log4j.Category", cp -> {
            cp.referenceType().methodsByName("setLevel").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String loggerName = thisName(e.thread());
                    String level = RemoteObject.argToString(e.thread(), 0);
                    record("LOG4J1-LEVEL", loggerName + " -> " + level, e.thread());
                    effectiveLoggers.put(loggerName, level);
                }).enable();
            });

            // Also catch the older setPriority
            cp.referenceType().methodsByName("setPriority").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String loggerName = thisName(e.thread());
                    String level = RemoteObject.argToString(e.thread(), 0);
                    record("LOG4J1-LEVEL", loggerName + " -> " + level + " (setPriority)", e.thread());
                }).enable();
            });
        });

        // FileAppender.setFile — where logs end up
        j.onClassPrep("org.apache.log4j.FileAppender", cp -> {
            cp.referenceType().methodsByName("setFile").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String file = firstArgAsString(e.thread());
                    record("LOG4J1-OUTPUT", "FileAppender -> " + file, e.thread());
                }).enable();
            });
        });
    }

    // --- Log4j 2.x ---

    private void interceptLog4j2() {
        // ConfigurationFactory.getConfiguration — which config source won
        j.onClassPrep("org.apache.logging.log4j.core.config.ConfigurationFactory", cp -> {
            cp.referenceType().methodsByName("getConfiguration").forEach(m -> {
                if (m.location() != null) {
                    j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                        record("LOG4J2-CONFIG", "ConfigurationFactory.getConfiguration()", e.thread());
                    }).enable();
                }
            });
        });

        // AbstractConfiguration.start — config is built
        j.onClassPrep("org.apache.logging.log4j.core.config.AbstractConfiguration", cp -> {
            cp.referenceType().methodsByName("start").forEach(m -> {
                if (m.argumentTypeNames().isEmpty() && m.location() != null) {
                    j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                        unchecked(() -> {
                            ObjectReference configObj = e.thread().frame(0).thisObject();
                            String configName = configObj != null
                                ? RemoteObject.remoteToString(configObj, e.thread()) : "<unknown>";
                            record("LOG4J2-CONFIG", "Configuration started: " + configName, e.thread());
                        });
                    }).enable();
                }
            });
        });

        // LoggerConfig.setLevel — runtime reconfiguration (show logger name + level)
        j.onClassPrep("org.apache.logging.log4j.core.config.LoggerConfig", cp -> {
            cp.referenceType().methodsByName("setLevel").forEach(m -> {
                if (m.location() != null) {
                    j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                        String loggerName = thisName(e.thread());
                        String level = RemoteObject.argToString(e.thread(), 0);
                        record("LOG4J2-LEVEL", loggerName + " -> " + level, e.thread());
                        effectiveLoggers.put(loggerName, level);
                    }).enable();
                }
            });
        });

        // Configurator.setLevel — programmatic level changes
        j.onClassPrep("org.apache.logging.log4j.core.config.Configurator", cp -> {
            cp.referenceType().methodsByName("setLevel").forEach(m -> {
                if (m.location() != null) {
                    j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                        String loggerName = firstArgAsString(e.thread());
                        String level = RemoteObject.argToString(e.thread(), 1);
                        record("LOG4J2-LEVEL", "Configurator.setLevel(" + loggerName + ", " + level + ")", e.thread());
                    }).enable();
                }
            });
        });
    }

    // --- SLF4J ---

    private void interceptSlf4j() {
        // SLF4J 1.x: StaticLoggerBinder — which backend won
        j.onClassPrep("org.slf4j.impl.StaticLoggerBinder", cp -> {
            cp.referenceType().methodsByName("getSingleton").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    record("SLF4J-BIND", "StaticLoggerBinder.getSingleton() — SLF4J 1.x binding resolved", e.thread());
                }).enable();
            });
        });

        // SLF4J 2.x: uses ServiceLoader, intercept LoggerFactory.bind()
        j.onClassPrep("org.slf4j.LoggerFactory", cp -> {
            cp.referenceType().methodsByName("bind").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    record("SLF4J-BIND", "LoggerFactory.bind() — SLF4J 2.x provider binding", e.thread());
                }).enable();
            });

            // Also catch the "multiple bindings" warning path
            cp.referenceType().methodsByName("reportMultipleBindingAmbiguity").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    record("SLF4J-WARN", "*** Multiple SLF4J bindings detected! ***", e.thread());
                }).enable();
            });

            // reportActualBinding shows which one was selected
            cp.referenceType().methodsByName("reportActualBinding").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    record("SLF4J-BIND", "SLF4J actual binding selected", e.thread());
                }).enable();
            });
        });
    }

    // --- Logback ---

    private void interceptLogback() {
        // ContextInitializer.autoConfig — logback's main config entry point
        j.onClassPrep("ch.qos.logback.classic.util.ContextInitializer", cp -> {
            cp.referenceType().methodsByName("autoConfig").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    record("LOGBACK-CONFIG", "ContextInitializer.autoConfig()", e.thread());
                }).enable();
            });

            // configureByResource — which file was chosen
            cp.referenceType().methodsByName("configureByResource").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String resource = RemoteObject.argToString(e.thread(), 0);
                    record("LOGBACK-CONFIG", "configureByResource(" + resource + ")", e.thread());
                }).enable();
            });
        });

        // Logger.setLevel (uses getName() for logger name)
        j.onClassPrep("ch.qos.logback.classic.Logger", cp -> {
            cp.referenceType().methodsByName("setLevel").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String loggerName = thisName(e.thread());
                    String level = RemoteObject.argToString(e.thread(), 0);
                    record("LOGBACK-LEVEL", loggerName + " -> " + level, e.thread());
                    effectiveLoggers.put(loggerName, level);
                }).enable();
            });
        });

        // FileAppender.setFile — where logback is writing
        j.onClassPrep("ch.qos.logback.core.FileAppender", cp -> {
            cp.referenceType().methodsByName("setFile").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String file = firstArgAsString(e.thread());
                    record("LOGBACK-OUTPUT", "FileAppender -> " + file, e.thread());
                }).enable();
            });
        });
    }

    // --- java.util.logging (JUL) ---

    private void interceptJUL() {
        // LogManager.readConfiguration — JUL config loading
        j.onClassPrep("java.util.logging.LogManager", cp -> {
            cp.referenceType().methodsByName("readConfiguration").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    record("JUL-CONFIG", "LogManager.readConfiguration()", e.thread());
                }).enable();
            });
        });

        // Logger.setLevel (uses getName() for logger name)
        j.onClassPrep("java.util.logging.Logger", cp -> {
            cp.referenceType().methodsByName("setLevel").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String loggerName = thisName(e.thread());
                    String level = RemoteObject.argToString(e.thread(), 0);
                    record("JUL-LEVEL", loggerName + " -> " + level, e.thread());
                    effectiveLoggers.put(loggerName, level);
                }).enable();
            });
        });
    }

    // --- System.setProperty for log4j keys ---

    private void interceptSystemProperties() {
        j.onClassPrep("java.lang.System", cp -> {
            cp.referenceType().methodsByName("setProperty").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String key = firstArgAsString(e.thread());
                    if (key != null && isLoggingProperty(key)) {
                        String value = RemoteObject.argToString(e.thread(), 1);
                        record("SYS-PROPERTY", "System.setProperty(\"" + key + "\", \"" + value + "\")", e.thread());
                    }
                }).enable();
            });
        });
    }

    private static boolean isLoggingProperty(String key) {
        return key.startsWith("log4j")
            || key.startsWith("LOG4J")
            || key.startsWith("logback")
            || key.startsWith("java.util.logging")
            || key.startsWith("org.slf4j")
            || key.equals("spark.log.level")
            || key.equals("SPARK_LOG_LEVEL");
    }

    // --- ClassLoader.getResource for logging config files ---

    private void interceptClassLoaderGetResource() {
        j.onClassPrep("java.lang.ClassLoader", cp -> {
            cp.referenceType().methodsByName("getResource").forEach(m -> {
                // Only intercept the public getResource(String), not internal overloads
                if (m.argumentTypeNames().size() == 1) {
                    j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                        String resourceName = firstArgAsString(e.thread());
                        if (resourceName != null && isLoggingResource(resourceName)) {
                            unchecked(() -> {
                                ObjectReference loader = e.thread().frame(0).thisObject();
                                String loaderName = loader != null
                                    ? loader.referenceType().name()
                                    : "bootstrap";
                                record("CLASSPATH", loaderName + ".getResource(\"" + resourceName + "\")", e.thread());
                            });
                        }
                    }).enable();
                }
            });
        });
    }

    private static boolean isLoggingResource(String name) {
        return name.contains("log4j")
            || name.contains("logback")
            || name.contains("logging.properties")
            || name.contains("commons-logging")
            || name.contains("simplelogger");
    }

    // --- Spark-specific logging init ---

    private void interceptSparkLogging() {
        // Spark's internal Logging trait initialization
        j.onClassPrep("org.apache.spark.internal.Logging", cp -> {
            cp.referenceType().methodsByName("initializeLogging").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    record("SPARK-LOG", "Spark Logging.initializeLogging()", e.thread());
                }).enable();
            });
        });

        // Spark's Utils.setLogLevel
        j.onClassPrep("org.apache.spark.util.Utils", cp -> {
            cp.referenceType().methodsByName("setLogLevel").forEach(m -> {
                j.breakpointRequest(m.location(), (OnBreakpoint) e -> {
                    String level = firstArgAsString(e.thread());
                    record("SPARK-LOG", "Utils.setLogLevel(" + level + ")", e.thread());
                }).enable();
            });
        });

        // Spark 3.x uses Log4j2 with a custom configurator
        j.onClassPrep("org.apache.spark.internal.config.package$", cp -> {
            record("SPARK-LOG", "Spark config package loaded — Spark is initializing", null);
        });
    }

    // ------------------------------------------------------------------
    // Summary report
    // ------------------------------------------------------------------

    void printSummary() {
        flushRepeats();
        println("\n" + "=".repeat(72));
        println("LOG CONFIGURATION TIMELINE (" + timeline.size() + " events)");
        println("=".repeat(72));

        if (timeline.isEmpty()) {
            println("  No logging configuration events captured.");
            println("  Possible reasons:");
            println("  - Logging was already initialized before debugger attached.");
            println("    Try using suspend=y on the target JVM.");
            println("  - The target uses a logging framework not covered by this tool.");
            return;
        }

        // Group by category for the summary
        Map<String, List<LogEvent>> byCategory = new LinkedHashMap<>();
        for (LogEvent e : timeline) {
            byCategory.computeIfAbsent(e.category, k -> new ArrayList<>()).add(e);
        }

        println("\n--- Chronological Timeline ---\n");
        for (int i = 0; i < timeline.size(); i++) {
            println(String.format("  %3d. %s", i + 1, timeline.get(i)));
        }

        println("\n--- Summary by Category ---\n");
        byCategory.forEach((cat, events) -> {
            println(String.format("  %-18s %d event(s)", cat, events.size()));
            events.forEach(e -> println("    " + e.description));
        });

        if (!effectiveLoggers.isEmpty()) {
            println("\n--- Final Logger Levels ---\n");
            effectiveLoggers.forEach((logger, level) ->
                println(String.format("  %-50s %s", logger, level)));
        }

        println("\n" + "=".repeat(72));
        println("TIP: If the timeline is empty or starts late, re-run with suspend=y:");
        println("  -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005");
        println("=".repeat(72));
    }

    // ------------------------------------------------------------------
    // Main
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: LogConfigDebugger [host] <port>");
            System.err.println();
            System.err.println("Attaches to a running JVM and traces logging configuration.");
            System.err.println();
            System.err.println("Target JVM must be started with:");
            System.err.println("  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:<port>");
            System.err.println();
            System.err.println("For Dataproc, add to your spark-submit:");
            System.err.println("  --driver-java-options \"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005\"");
            System.err.println("Then tunnel: gcloud compute ssh CLUSTER-m -- -L 5005:localhost:5005");
            System.exit(1);
        }

        String host;
        int port;
        if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } else {
            host = null;
            port = Integer.parseInt(args[0]);
        }

        println("Attaching to " + (host != null ? host + ":" : "localhost:") + port + " ...");
        VirtualMachine vm;
        if (host != null) {
            vm = new VMSocketAttacher(host, port).attach();
        } else {
            vm = new VMSocketAttacher(port).attach();
        }
        println("Attached to " + vm.name() + " (" + vm.version() + ")");

        JDIScript j = new JDIScript(vm);
        LogConfigDebugger debugger = new LogConfigDebugger(j);

        println("Setting up logging framework interceptors...");
        println("Watching: Log4j 1.x, Log4j 2.x, SLF4J, Logback, JUL, Spark, System properties");
        println("Events will print as they occur. Press Ctrl-C or wait for VM exit.\n");

        // Print summary on VM death or disconnect
        OnVMDeath death = e -> debugger.printSummary();
        OnVMDisconnect disconnect = e -> debugger.printSummary();

        debugger.setupInterceptors();
        j.run(List.of(death, disconnect));
    }
}
