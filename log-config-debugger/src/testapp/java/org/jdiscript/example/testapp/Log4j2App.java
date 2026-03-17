package org.jdiscript.example.testapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Exercises Log4j 2.x code paths that LogConfigDebugger intercepts:
 * <ul>
 *   <li>ConfigurationFactory.getConfiguration — config source resolution</li>
 *   <li>AbstractConfiguration.start — config activation</li>
 *   <li>ConfigurationSource construction — config file being read</li>
 *   <li>LoggerConfig.setLevel — runtime level changes</li>
 * </ul>
 *
 * Uses log4j2-test.xml from the classpath. Run with JDWP to test:
 * <pre>
 * java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 \
 *      -cp ... org.jdiscript.example.testapp.Log4j2App
 * </pre>
 */
public class Log4j2App {

    private static final Logger logger = LogManager.getLogger(Log4j2App.class);
    private static final Logger dataLogger = LogManager.getLogger("com.example.data");

    public static void main(String[] args) {
        // 1. Initial logging — config is loaded automatically from log4j2-test.xml
        //    -> triggers ConfigurationFactory.getConfiguration,
        //       AbstractConfiguration.start, ConfigurationSource.<init>
        logger.info("Log4j2App starting up");
        logger.debug("Debug message from main logger");
        dataLogger.info("Data logger initial message");

        // 2. System property for logging config
        //    -> triggers SYS-PROPERTY intercept
        System.setProperty("log4j2.debug", "true");

        // 3. Programmatic level change via Configurator
        //    -> triggers LoggerConfig.setLevel
        Configurator.setLevel("com.example.data", org.apache.logging.log4j.Level.TRACE);
        dataLogger.trace("Data logger now at TRACE");
        dataLogger.debug("Data logger debug after level change");

        // 4. Change root level
        Configurator.setRootLevel(org.apache.logging.log4j.Level.WARN);
        logger.info("This should be filtered after root set to WARN");
        logger.warn("This warning should still appear");
        logger.error("This error should still appear");

        // 5. Change it back
        Configurator.setLevel(Log4j2App.class.getName(), org.apache.logging.log4j.Level.DEBUG);
        logger.debug("Back to DEBUG for main logger");

        System.out.println("Log4j2App finished.");
    }
}
