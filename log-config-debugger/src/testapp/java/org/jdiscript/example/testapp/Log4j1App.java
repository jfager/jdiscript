package org.jdiscript.example.testapp;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

/**
 * Exercises Log4j 1.x code paths that LogConfigDebugger intercepts:
 * <ul>
 *   <li>PropertyConfigurator.doConfigure — config file loading</li>
 *   <li>Category.setLevel — programmatic level changes</li>
 *   <li>Category.setPriority — legacy level API</li>
 *   <li>FileAppender.setFile — output destination</li>
 * </ul>
 *
 * Run with JDWP to test:
 * <pre>
 * java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 \
 *      -cp ... org.jdiscript.example.testapp.Log4j1App
 * </pre>
 */
public class Log4j1App {

    public static void main(String[] args) throws Exception {
        // 1. Load config from classpath properties file
        //    -> triggers PropertyConfigurator.doConfigure
        PropertyConfigurator.configure(
            Log4j1App.class.getClassLoader().getResource("log4j-test.properties"));

        Logger root = Logger.getRootLogger();
        Logger appLogger = Logger.getLogger("com.example.myapp");
        Logger dataLogger = Logger.getLogger("com.example.myapp.data");

        // 2. Programmatic level changes -> triggers Category.setLevel
        appLogger.setLevel(Level.DEBUG);
        dataLogger.setLevel(Level.TRACE);

        // 3. Legacy setPriority -> triggers Category.setPriority
        Logger legacyLogger = Logger.getLogger("com.example.legacy");
        legacyLogger.setPriority(Level.WARN);

        // 4. FileAppender -> triggers FileAppender.setFile
        FileAppender fileAppender = new FileAppender(
            new PatternLayout("%d{ISO8601} [%t] %-5p %c - %m%n"),
            "/tmp/log4j1-test.log");
        root.addAppender(fileAppender);

        // 5. Emit some log messages at various levels
        root.info("Root logger initialized");
        appLogger.debug("App logger at DEBUG");
        appLogger.trace("App logger trace (should appear since parent is DEBUG)");
        dataLogger.trace("Data logger at TRACE");
        legacyLogger.warn("Legacy logger at WARN");
        legacyLogger.debug("Legacy logger debug (should be filtered)");

        // 6. Runtime level change -> another Category.setLevel
        appLogger.setLevel(Level.ERROR);
        appLogger.info("This should be filtered after level change");
        appLogger.error("This should still appear");

        System.out.println("Log4j1App finished.");
    }
}
