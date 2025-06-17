package com.final_app.views.components;

import com.final_app.globals.TKey;
import com.final_app.tools.StyleLoader;
import com.final_app.tools.TranslationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A borderless, draggable custom dialog with a header, content area, and footer button bar.
 * Supports an optional built-in text input mode for simple prompts.
 * <p>To style, add a CSS stylesheet with selectors:
 * .custom-dialog, .dialog-header, .dialog-title, .dialog-close-button,
 * .dialog-content, .dialog-button-bar, .dialog-button
 * </p>
 */
public class CustomDialog extends Stage {
    private final HBox headerBar;
    private final Label titleLabel;
    private final Button closeButton;
    private final Pane contentPane;
    private final HBox buttonBar;
    private TextField inputField;

    private static class Delta { double x, y; }

    /**
     * Primary constructor for a generic dialog.
     * @param owner the owner Stage
     * @param titleProperty the window title and header text
     */
    public CustomDialog(Scene owner, StringProperty titleProperty) {
        initOwner(owner.getWindow());
        initStyle(StageStyle.TRANSPARENT);
        initModality(Modality.APPLICATION_MODAL);

        // Header bar
        headerBar = new HBox();
        headerBar.getStyleClass().add("dialog-header");
        titleLabel = new Label();
        titleLabel.textProperty().bind(titleProperty);
        titleLabel.getStyleClass().add("dialog-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        closeButton = new Button("✕");
        closeButton.getStyleClass().add("dialog-close-button");
        closeButton.setOnAction(e -> close());
        headerBar.getChildren().addAll(titleLabel, spacer, closeButton);

        // Draggable support
        final Delta dragDelta = new Delta();
        headerBar.setOnMousePressed(e -> {
            dragDelta.x = e.getSceneX();
            dragDelta.y = e.getSceneY();
        });
        headerBar.setOnMouseDragged(e -> {
            setX(e.getScreenX() - dragDelta.x);
            setY(e.getScreenY() - dragDelta.y);
        });

        // Content and button bar
        contentPane = new VBox();
        contentPane.getStyleClass().add("dialog-content");
        buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.getStyleClass().add("dialog-button-bar");

        VBox root = new VBox(headerBar, contentPane, buttonBar);
        root.getStyleClass().addAll("custom-dialog");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        StyleLoader.loadStyles(scene);
        setScene(scene);
    }

    /**
     * Convenience constructor for a simple text-input dialog.
     * @param owner the owner Stage
     * @param titleProperty the window title
     * @param promptTextProperty label and placeholder for the input field
     */
    public CustomDialog(Scene owner, StringProperty titleProperty, StringProperty promptTextProperty) {
        this(owner, titleProperty);
        // Inline content: prompt label + text field
        Label promptLabel = new Label();
        promptLabel.textProperty().bind(promptTextProperty);
        promptLabel.getStyleClass().addAll("b2", "primary");
        inputField = new TextField();
        inputField.promptTextProperty().bind(promptTextProperty);
        inputField.getStyleClass().add("textfield-modern");
        VBox body = new VBox(10, promptLabel, inputField);
        body.setPadding(new Insets(15));
        setContent(body);

        // Default buttons
        Button cancelBtn = addButton(TranslationManager.get().t(TKey.CANCEL), dlg -> dlg.close());
        Button okBtn = addButton(new SimpleStringProperty("OK"), dlg -> dlg.close());
        okBtn.setDisable(true);
        // Enable OK only when text entered
        inputField.textProperty().addListener((obs, oldV, newV) ->
                okBtn.setDisable(newV.trim().isEmpty())
        );
    }

    /**
     * Sets the custom content Node in the dialog's body.
     */
    public void setContent(Node node) {
        contentPane.getChildren().setAll(node);
    }

    /**
     * Adds a button to the footer bar.
     * @param text label of the button
     * @param action callback receiving this dialog when clicked
     * @return the created Button
     */
    public Button addButton(StringProperty text, Consumer<CustomDialog> action) {
        Button button = new Button();
        button.textProperty().bind(text);
        button.getStyleClass().add("dialog-button");
        button.setOnAction(e -> action.accept(this));
        buttonBar.getChildren().add(button);
        return button;
    }

    /**
     * Returns the text typed into the built-in input field, or null if none.
     */
    public String getInputText() {
        return inputField != null ? inputField.getText() : null;
    }

    /**
     * Shows the dialog and returns the input text (if a text-field dialog),
     * otherwise returns Optional.empty().
     */
    public Optional<String> showAndWaitForInput() {
        showAndWait();
        return Optional.ofNullable(getInputText());
    }
}


