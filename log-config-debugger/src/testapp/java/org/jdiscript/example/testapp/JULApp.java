package org.jdiscript.example.testapp;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Exercises java.util.logging code paths that LogConfigDebugger intercepts:
 * <ul>
 *   <li>LogManager.readConfiguration — config loading</li>
 *   <li>Logger.setLevel — programmatic level changes</li>
 * </ul>
 *
 * Run with JDWP to test:
 * <pre>
 * java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 \
 *      -cp ... org.jdiscript.example.testapp.JULApp
 * </pre>
 */
public class JULApp {

    public static void main(String[] args) throws Exception {
        // 1. Load configuration from an inline properties string
        //    -> triggers JUL-CONFIG LogManager.readConfiguration
        String config = String.join("\n",
            "handlers=java.util.logging.ConsoleHandler",
            "java.util.logging.ConsoleHandler.level=ALL",
            "java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter",
            ".level=INFO",
            "com.example.jul.level=FINE"
        );
        LogManager.getLogManager().readConfiguration(
            new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));

        Logger rootLogger = Logger.getLogger("");
        Logger appLogger = Logger.getLogger("com.example.jul");
        Logger dbLogger = Logger.getLogger("com.example.jul.db");

        // 2. Log at various levels
        rootLogger.info("Root logger initialized via readConfiguration");
        appLogger.fine("App logger at FINE (should appear)");
        appLogger.finer("App logger at FINER (should be filtered)");

        // 3. Programmatic level changes -> triggers JUL-LEVEL
        dbLogger.setLevel(Level.ALL);
        dbLogger.finest("DB logger at ALL — finest visible");
        dbLogger.fine("DB logger fine message");

        // 4. Another readConfiguration to simulate reconfiguration
        String reconfig = String.join("\n",
            "handlers=java.util.logging.ConsoleHandler",
            "java.util.logging.ConsoleHandler.level=ALL",
            "java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter",
            ".level=WARNING"
        );
        LogManager.getLogManager().readConfiguration(
            new ByteArrayInputStream(reconfig.getBytes(StandardCharsets.UTF_8)));

        rootLogger.info("This should be filtered after reconfig to WARNING");
        rootLogger.warning("This warning should appear after reconfig");

        // 5. System property for JUL config
        //    -> triggers SYS-PROPERTY
        System.setProperty("java.util.logging.config.class", "com.example.FakeConfigClass");

        // 6. More level changes
        Logger.getLogger("com.example.jul.web").setLevel(Level.SEVERE);
        Logger.getLogger("com.example.jul.web").severe("Web logger at SEVERE");

        System.out.println("JULApp finished.");
    }
}
