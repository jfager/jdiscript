package org.jdiscript.example.testapp;

import java.util.logging.Level;

/**
 * The kitchen sink: initializes multiple logging frameworks in a single
 * JVM, the way a real Spark/Dataproc application often does. This is the
 * most realistic test for LogConfigDebugger — it exercises nearly every
 * interceptor at once.
 *
 * <p>Startup sequence (mirrors a typical Spark driver):
 * <ol>
 *   <li>System properties set before any framework loads</li>
 *   <li>JUL initializes (JVM default)</li>
 *   <li>SLF4J binds to Logback</li>
 *   <li>Log4j 1.x loaded by a "legacy" library</li>
 *   <li>Log4j 2.x loaded by another dependency</li>
 *   <li>Runtime level changes from all frameworks</li>
 * </ol>
 *
 * Run with JDWP to test:
 * <pre>
 * java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 \
 *      -cp ... org.jdiscript.example.testapp.MultiFrameworkApp
 * </pre>
 */
public class MultiFrameworkApp {

    public static void main(String[] args) throws Exception {
        System.out.println("=== MultiFrameworkApp: exercising all logging frameworks ===\n");

        // ---------------------------------------------------------------
        // Phase 1: System properties (before any framework loads)
        // -> triggers SYS-PROPERTY
        // ---------------------------------------------------------------
        System.out.println("--- Phase 1: System properties ---");
        System.setProperty("log4j.configuration", "log4j-testapp.properties");
        System.setProperty("log4j2.configurationFile", "log4j2-test.xml");
        System.setProperty("logback.configurationFile", "logback-test.xml");

        // ---------------------------------------------------------------
        // Phase 2: JUL
        // -> triggers JUL-CONFIG, JUL-LEVEL
        // ---------------------------------------------------------------
        System.out.println("\n--- Phase 2: java.util.logging ---");
        java.util.logging.Logger julRoot = java.util.logging.Logger.getLogger("");
        java.util.logging.Logger julApp = java.util.logging.Logger.getLogger("com.example.app");
        julApp.setLevel(Level.FINE);
        julApp.info("JUL app logger at FINE");

        // ---------------------------------------------------------------
        // Phase 3: SLF4J + Logback
        // -> triggers SLF4J-BIND, LOGBACK-CONFIG, LOGBACK-LEVEL, CLASSPATH
        // ---------------------------------------------------------------
        System.out.println("\n--- Phase 3: SLF4J + Logback ---");
        org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger("com.example.service");
        slf4jLogger.info("SLF4J via Logback — first message triggers binding");

        ch.qos.logback.classic.LoggerContext logbackCtx =
            (ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        logbackCtx.getLogger("com.example.service")
            .setLevel(ch.qos.logback.classic.Level.TRACE);
        slf4jLogger.trace("SLF4J service logger now at TRACE");

        // ---------------------------------------------------------------
        // Phase 4: Log4j 1.x
        // -> triggers LOG4J1-CONFIG, LOG4J1-LEVEL, LOG4J1-OUTPUT
        // ---------------------------------------------------------------
        System.out.println("\n--- Phase 4: Log4j 1.x ---");
        org.apache.log4j.PropertyConfigurator.configure(
            MultiFrameworkApp.class.getClassLoader().getResource("log4j-testapp.properties"));

        org.apache.log4j.Logger log4j1Logger =
            org.apache.log4j.Logger.getLogger("com.example.legacy");
        log4j1Logger.setLevel(org.apache.log4j.Level.DEBUG);
        log4j1Logger.debug("Log4j 1.x legacy logger at DEBUG");

        org.apache.log4j.FileAppender fa = new org.apache.log4j.FileAppender(
            new org.apache.log4j.PatternLayout("%d [%t] %-5p %c - %m%n"),
            "/tmp/multi-framework-log4j1.log");
        org.apache.log4j.Logger.getRootLogger().addAppender(fa);

        // ---------------------------------------------------------------
        // Phase 5: Log4j 2.x
        // -> triggers LOG4J2-CONFIG, LOG4J2-LEVEL, LOG4J2-SOURCE
        // ---------------------------------------------------------------
        System.out.println("\n--- Phase 5: Log4j 2.x ---");
        org.apache.logging.log4j.Logger log4j2Logger =
            org.apache.logging.log4j.LogManager.getLogger("com.example.modern");
        log4j2Logger.info("Log4j 2.x modern logger — first message triggers config");

        org.apache.logging.log4j.core.config.Configurator.setLevel(
            "com.example.modern", org.apache.logging.log4j.Level.TRACE);
        log4j2Logger.trace("Log4j 2.x modern logger now at TRACE");

        // ---------------------------------------------------------------
        // Phase 6: More runtime changes across frameworks
        // ---------------------------------------------------------------
        System.out.println("\n--- Phase 6: Runtime level changes ---");

        // JUL
        julApp.setLevel(Level.SEVERE);
        julApp.warning("JUL: this should be filtered (SEVERE only)");
        julApp.severe("JUL: severe message");

        // Logback
        logbackCtx.getLogger("com.example.service")
            .setLevel(ch.qos.logback.classic.Level.ERROR);
        slf4jLogger.warn("Logback: this should be filtered (ERROR only)");
        slf4jLogger.error("Logback: error message");

        // Log4j 1.x
        log4j1Logger.setLevel(org.apache.log4j.Level.FATAL);
        log4j1Logger.error("Log4j1: this should be filtered (FATAL only)");
        log4j1Logger.fatal("Log4j1: fatal message");

        // Log4j 2.x
        org.apache.logging.log4j.core.config.Configurator.setLevel(
            "com.example.modern", org.apache.logging.log4j.Level.OFF);
        log4j2Logger.fatal("Log4j2: this should be filtered (OFF)");

        System.out.println("\n=== MultiFrameworkApp finished. ===");
    }
}
