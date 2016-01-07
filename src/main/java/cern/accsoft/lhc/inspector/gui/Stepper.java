package cern.accsoft.lhc.inspector.gui;

import cern.accsoft.lhc.inspector.Inspector;
import cern.accsoft.lhc.inspector.handlers.InspectorEventHandler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jdiscript.JDIScript;

import java.util.concurrent.Executors;

/**
 * A user interface that can step over code.
 */
public class Stepper extends Application {

    private static Inspector inspector;

    public static void setInspector(Inspector inspector) {
        Stepper.inspector = inspector;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Hello World!");
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(event -> inspector.stepOver());

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    public static void start(Inspector inspector) {
        Executors.newFixedThreadPool(1).execute(() -> {
            setInspector(inspector);
            launch();
        });
    }
}
