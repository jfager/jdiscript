/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.accsoft.lhc.inspector.gui;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class StepperTab extends Tab {

    private static final String LINE_PREFIX = "line_";
    public static final String ACTIVE_LINE_CLASS = "active_line";

    private FlowPane root;
    private int lastLine = 0;
    private Button stepButton;

    public StepperTab(String className, List<String> codeLines) {
        super(className);

        AtomicInteger lineNumbers = new AtomicInteger(1);
        Stream<Text> textPanelStream = codeLines.stream().map(line -> {
            final int lineNumber = lineNumbers.getAndIncrement();
            String content = lineNumber + ": " + line;
            Text text = new Text(content);
            text.setWrappingWidth(20000);
            text.setId(LINE_PREFIX + lineNumber);
            return text;
        });
        root = new FlowPane();
        textPanelStream.forEach(text -> root.getChildren().add(text));

        stepButton = new Button();
        stepButton.setText("Execute next instruction");
        root.getChildren().add(stepButton);
        setContent(root);
    }

    public void close() {
        // http://stackoverflow.com/questions/17047000/javafx-closing-a-tab-in-tabpane-dynamically
        Platform.runLater(() -> Event.fireEvent(this, new Event(Tab.CLOSED_EVENT)));
    }

    public void highlight(int line) {
        Platform.runLater(() -> {
            Text lastText = (Text) root.lookup("#" + LINE_PREFIX + lastLine);
            if (lastText != null) {
                lastText.getStyleClass().remove(ACTIVE_LINE_CLASS);
            }

            Text text = (Text) root.lookup("#" + LINE_PREFIX + line);
            if (text != null) {
                text.getStyleClass().add(ACTIVE_LINE_CLASS);
            }
            lastLine = line;
        });
    }

    public void onForwardButton(EventHandler<ActionEvent> callback) {
        stepButton.setOnAction(callback);
    }

}
