package com.final_app.views.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;

public class CompletedPopup extends VBox {
    @FXML
    private Button btnRetry;
    @FXML
    private Button btnSeeEvaluation;

    private Consumer<Void> onRetryButtonClicked;
    private Consumer<Void> onSeeEvaluationClicked;

    public CompletedPopup() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/CompletedPopup.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            btnRetry.setOnAction((e)->{
                onRetryButtonClicked.accept(null);
            });
            btnSeeEvaluation.setOnAction(e->{
                onSeeEvaluationClicked.accept(null);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOnRetryButtonClicked(Consumer<Void> onRetryButtonClicked){this.onRetryButtonClicked = onRetryButtonClicked;}
    public void setOnSeeEvaluationClicked(Consumer<Void> onSeeEvaluationClicked){this.onSeeEvaluationClicked = onSeeEvaluationClicked;}
}
