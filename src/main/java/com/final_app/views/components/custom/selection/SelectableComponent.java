package com.final_app.views.components.custom.selection;

import com.final_app.globals.GlobalVariables;
import com.final_app.models.Conversation;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.tools.ColorTranslator;
import com.final_app.tools.SVGUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;

import java.util.function.Consumer;

public class SelectableComponent<T> extends HBox {
    private Label lblSmallText = new Label();
    private Label lblLargeText = new Label();
    private ImageView imgCheck = new ImageView(SVGUtil.loadSVG(GlobalVariables.ICONS + "check_light.svg", 20,20));
    private VBox imgBox = new VBox(imgCheck);

    private Region spacer = new Region();

    private final StringProperty smallTextProperty = new SimpleStringProperty();
    private final StringProperty largeTextProperty = new SimpleStringProperty();

    private T item;

    public boolean isSelected = false;


    // Actions
    private Consumer<T> onSelectionChanged;
    public void setOnSelectionChanged(Consumer<T> onSelectionChanged){this.onSelectionChanged = onSelectionChanged;}
    public void select(boolean triggerEvent){
        if(onSelectionChanged != null){
            Platform.runLater(() -> {
                System.out.println("Selected");
                imgBox.setVisible(true);
                isSelected = true;
                this.getStyleClass().add("selected");
                if(triggerEvent){
                    onSelectionChanged.accept(item);
                }
            });

        }
    }
    public void deselect(boolean triggerEvent){
        if(onSelectionChanged != null){
            Platform.runLater(() -> {
                System.out.println("Deselected");
                imgBox.setVisible(false);
                isSelected = false;
                this.getStyleClass().remove("selected");
                if(triggerEvent){
                    onSelectionChanged.accept(null);
                }
            });

        }
    }

    public SelectableComponent() {
        super();
        styleComponent();
        bindProperties();

        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(lblSmallText, lblLargeText, spacer, imgBox);
        imgBox.setVisible(false);
    }
    public SelectableComponent(T item){
        this();

        this.item = item;
        bindByItem();

    }


    private void bindProperties(){
        lblSmallText.textProperty().bind(smallTextProperty);
        lblLargeText.textProperty().bind(largeTextProperty);

        this.setOnMouseClicked(e->{
            System.out.println("Clicked");
            if(isSelected){
                deselect(true);
            }else{
                select(true);
            }
        });
    }

    public void setSmallText(String text){
        this.smallTextProperty.set(text);
    }
    public void setLargeText(String text){
        this.largeTextProperty.set(text);
    }

    private void styleComponent(){
        this.getStyleClass().addAll("card", "p-1", "selectable-item");
        this.setSpacing(10);
        this.setMinWidth(200);


        this.getStyleClass().addAll("border-2", "border-light", "align-center-left");

        lblLargeText.getStyleClass().addAll("primary");

        // Make the small text a circle
        lblSmallText.getStyleClass().addAll("circle", "align-center");
        lblSmallText.setMinWidth(0);
        lblSmallText.setMinHeight(0);
        lblSmallText.setMaxHeight(Double.MAX_VALUE);
        lblSmallText.setMaxWidth(Double.MAX_VALUE);
        lblSmallText.setPrefSize(40, 40);

        // Style the check
        imgBox.getStyleClass().addAll("align-center", "circle");
        imgBox.setMinWidth(0);
        imgBox.setMinHeight(0);
        imgBox.setMaxHeight(25);
        imgBox.setMaxWidth(25);
        imgBox.setPrefSize(25, 25);
        imgBox.setStyle("-fx-background-color: -hover-color;");
    }

    private void bindByItem(){
        if(item instanceof String){
            smallTextProperty.set((String) item.toString().substring(0, 1));
        }else if(item instanceof Language){
            smallTextProperty.set(((Language) item).getIso());
            largeTextProperty.set(((Language) item).getName());

            lblSmallText.setStyle("-fx-text-fill:" + ((Language) item).getColor() + ";" +
                    "-fx-background-color:" + ColorTranslator.textToBackground(((Language) item).getColor()));
        }else if(item instanceof LanguageLevel){
            smallTextProperty.set(Integer.toString(((LanguageLevel) item).getValue()));
            largeTextProperty.set(((LanguageLevel) item).getName());

            lblSmallText.setStyle("-fx-text-fill: #ffffff; -fx-background-color: #050505;");
        }else if(item instanceof Conversation){
            smallTextProperty.set(((Conversation) item).getTitle().substring(0, 1));
            largeTextProperty.set(((Conversation) item).getTitle());
        }
    }
}
