package cern.accsoft.lhc.inspector.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import cern.accsoft.lhc.inspector.Inspector;
import cern.accsoft.lhc.inspector.controller.ThreadState;
import cern.accsoft.lhc.inspector.inspectable.InspectableCallbackListener;
import cern.accsoft.lhc.inspector.inspectable.InspectableInstanceListener;

import com.sun.jdi.ThreadReference;

/**
 * A user interface that can step over code.
 */
public class Stepper extends Application {

    public static final String STEPPER_CSS = "stepper.css";

    private static final ExecutorService LOG_THREAD_POOL = Executors.newFixedThreadPool(2);

    private static Inspector inspector;
    private static FlowPane rootPane;
    private static TabPane tabs;

    public static void close() {
        Platform.runLater(() -> Platform.exit());
        LOG_THREAD_POOL.shutdown();
        inspector.close();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Inspector");

        rootPane = new FlowPane();
        tabs = new TabPane();

        Scene scene = new Scene(rootPane, 300, 500);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(Stepper.class.getResource(STEPPER_CSS).toExternalForm());
        primaryStage.setOnCloseRequest(event -> Stepper.close());
        primaryStage.show();
        rootPane.getChildren().add(tabs);

        ScrollPane scrollPane = new ScrollPane();
        TextFlow logText = new TextFlow();
        scrollPane.setContent(logText);
        redirectInput(logText);
        rootPane.getChildren().add(scrollPane);
    }

    private void redirectInput(TextFlow textFlow) {
        LOG_THREAD_POOL.execute(() -> {
            redirectSingleInput(inspector.getStandardOut(), textFlow, Color.BLACK);
        });
        LOG_THREAD_POOL.execute(() -> {
            redirectSingleInput(inspector.getErrorOut(), textFlow, Color.RED);
        });
    }

    private void redirectSingleInput(InputStream input, TextFlow textFlow, Color textColor) {
        try (InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader reader = new BufferedReader(inputReader)) {
            reader.lines().forEach(line -> Platform.runLater(() -> {
                Text text = new Text();
                text.setText(line + "\n");
                text.setFill(textColor);
                textFlow.getChildren().add(text);
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void start(Inspector newInspector) {
        Stepper.inspector = newInspector;
        Application.launch();
    }

    public static final InspectableInstanceListener INSTANCE_LISTENER = new InspectableInstanceListener() {

        @Override
        public InspectableCallbackListener onNewInspectableInstance(ThreadReference thread, ThreadState state) {
            try {
                final String sourceName = state.getCurrentLocation().sourceName();
                final String sourcePath = state.getCurrentLocation().sourcePath();
                List<String> code = inspector.getCode(sourcePath);
                StepperTab tab = new StepperTab(sourceName, code);
                tab.highlight(state.getCurrentLocation().lineNumber());
                Platform.runLater(() -> {
                    tab.onForwardButton(event -> inspector.stepOver(thread));
                    tabs.getTabs().add(tab);
                    tab.setOnClosed(event -> {
                        tabs.getTabs().remove(tab);
                        if (tabs.getTabs().isEmpty()) {
                            close();
                        }
                    });
                });
                return new StepperCallbackHandler(tab);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    };
}
