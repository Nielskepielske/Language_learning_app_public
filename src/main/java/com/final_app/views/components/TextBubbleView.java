package com.final_app.views.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;


public class TextBubbleView extends Label {
    @FXML
    private Label lblText;


    public TextBubbleView(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/TextBubbleView.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void setBackgroundColor(String color){
        this.lblText.setStyle("-fx-background-color: " + color);
    }
    public void setTextColor(String color){
        this.lblText.setStyle("-fx-text-fill: " + color);
    }


    public void initialize() {
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                lblText.maxWidthProperty().bind(newScene.widthProperty().divide(2));
            }
        });
    }
}
