package com.final_app.views.components.custom.general;

import com.final_app.tools.ColorTranslator;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SummaryComponent extends HBox {
    private Label lblSmallText = new Label();
    private Label lblHeadTitle = new Label();
    private Region spacer = new Region();
    private Label lblSubTitle = new Label();

    public SummaryComponent() {
        super();

        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.setSpacing(20);
        this.getChildren().addAll(lblSmallText, lblHeadTitle, spacer, lblSubTitle);
    }
    public SummaryComponent(String smallText, String headTitle, String subTitle){
        this();

        lblSmallText.setText(smallText);
        lblHeadTitle.setText(headTitle);
        lblSubTitle.setText(subTitle);

        styleElements();
    }

    private void styleElements(){
        lblSmallText.getStyleClass().addAll("circle", "align-center");
        lblSmallText.setMinSize(0,0);
        lblSmallText.setMaxSize(40, 40);
        lblSmallText.setPrefSize(40, 40);

        lblHeadTitle.getStyleClass().addAll("primary", "h5");

        //lblSubTitle.getStyleClass().addAll("border-radius-2", "border-light", "border-2", "primary","b4", "p-label-sm");
        lblSubTitle.getStyleClass().addAll("pill");


        this.getStyleClass().addAll("border-light","border-3", "border-radius-1", "align-center-left", "p-2");
    }
    public void setCircleTextColor(String color){
        lblSmallText.setStyle("-fx-text-fill: " + color + "; -fx-background-color: " + ColorTranslator.textToBackground(color) +";");
    }
}
