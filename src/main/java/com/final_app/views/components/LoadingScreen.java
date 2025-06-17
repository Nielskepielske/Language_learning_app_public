package com.final_app.views.components;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Simple reusable loading screen overlay for JavaFX
 */
public class LoadingScreen {

    private final Stage dialogStage;
    private final Label messageLabel;

    public LoadingScreen(Stage owner, String message) {
        dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);

        messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        VBox vbox = new VBox(20, progressIndicator, messageLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 30px; -fx-background-radius: 10;");

        StackPane root = new StackPane(vbox);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.3);");

        Scene scene = new Scene(root, 300, 300);
        scene.setFill(Color.TRANSPARENT);

        dialogStage.setScene(scene);
    }

    public void show() {
        dialogStage.show();
    }

    public void close() {
        dialogStage.close();
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}

