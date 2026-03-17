package org.jdiscript.example.testapp;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exercises SLF4J + Logback code paths that LogConfigDebugger intercepts:
 * <ul>
 *   <li>LoggerFactory.bind — SLF4J 2.x provider binding</li>
 *   <li>ContextInitializer.autoConfig — logback auto-configuration</li>
 *   <li>configureByResource — which config file was loaded</li>
 *   <li>Logger.setLevel — programmatic level changes</li>
 *   <li>FileAppender.setFile — output destination (via logback-test.xml)</li>
 * </ul>
 *
 * Uses logback-test.xml from the classpath. Run with JDWP to test:
 * <pre>
 * java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 \
 *      -cp ... org.jdiscript.example.testapp.LogbackApp
 * </pre>
 */
public class LogbackApp {

    private static final Logger logger = LoggerFactory.getLogger(LogbackApp.class);
    private static final Logger serviceLogger = LoggerFactory.getLogger("com.example.service");

    public static void main(String[] args) {
        // 1. First SLF4J call triggers binding + logback autoConfig
        //    -> triggers SLF4J-BIND, LOGBACK-CONFIG, and CLASSPATH events
        logger.info("LogbackApp starting — SLF4J bound to logback");

        // 2. Log at various levels
        logger.debug("Debug from main logger");
        logger.trace("Trace from main logger (may be filtered by config)");
        serviceLogger.info("Service logger initial message");
        serviceLogger.warn("Service logger warning");

        // 3. Programmatic level change via logback API
        //    -> triggers LOGBACK-LEVEL
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logbackLogger =
            ctx.getLogger("com.example.service");
        logbackLogger.setLevel(Level.TRACE);

        serviceLogger.trace("Service logger now at TRACE");
        serviceLogger.debug("Service logger debug after level change");

        // 4. Change root logger level
        ch.qos.logback.classic.Logger rootLogger =
            ctx.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.WARN);

        logger.info("This should be filtered after root set to WARN");
        logger.warn("This warning should still appear");

        // 5. Set a custom logger level
        ch.qos.logback.classic.Logger customLogger =
            ctx.getLogger("com.example.custom");
        customLogger.setLevel(Level.ALL);

        Logger custom = LoggerFactory.getLogger("com.example.custom");
        custom.trace("Custom logger at ALL — trace visible");

        System.out.println("LogbackApp finished.");
    }
}
