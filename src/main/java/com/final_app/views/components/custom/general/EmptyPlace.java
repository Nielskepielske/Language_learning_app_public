package com.final_app.views.components.custom.general;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class EmptyPlace extends HBox {
    private Label message = new Label("Nothing to see here");

    public EmptyPlace(){
        super();
        getChildren().add(message);
        styleComponent();
    }
    public EmptyPlace(String message){
        this();
        this.message.setText(message);
    }

    private void styleComponent(){
        this.setMinHeight(150);
        this.getStyleClass().addAll("align-center-left", "border-3", "border-radius-1", "p-2", "border-light");

        message.getStyleClass().addAll("secondary", "b2");
    }
    public void setText(String text){
        this.message.setText(text);
    }
}
