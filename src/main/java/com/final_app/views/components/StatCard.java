package com.final_app.views.components;

import com.final_app.globals.GlobalVariables;
import com.final_app.tools.SVGUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class StatCard extends VBox {

    @FXML private Label lblTitle;
    @FXML private Label lblValue;
    @FXML private Label lblExtra;
    @FXML private ImageView icon;

    private StringProperty title = new SimpleStringProperty();
    private StringProperty value = new SimpleStringProperty();
    private StringProperty extra = new SimpleStringProperty();
    private StringProperty iconPath = new SimpleStringProperty();

    private int iconSize = 30;

    public StatCard(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/StatCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            lblTitle.textProperty().bind(title);
            lblValue.textProperty().bind(value);
            lblExtra.textProperty().bind(extra);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void setAll(String title, int value, String extra, String iconPath){
        this.title.set(title);
        this.value.set(""+value);
        this.extra.set(extra);

        if(iconPath != null){
            this.iconPath.set(GlobalVariables.BASE_PATH + "icons/" + iconPath);
            this.icon.setImage(SVGUtil.loadSVG(this.iconPath.get(), iconSize,iconSize));
        }
    }
    public void setAll(StringProperty title, int value, StringProperty extra, String iconPath){
        this.title.bind(title);
        this.value.set(""+value);
        if(extra != null){
            this.extra.bind(extra);
        }

        if(iconPath != null){
            this.iconPath.set(GlobalVariables.BASE_PATH + "icons/" + iconPath);
            this.icon.setImage(SVGUtil.loadSVG(this.iconPath.get(), iconSize,iconSize));
        }
    }
//    @Override
//    public void setWidth(double width){
//        this.setPrefWidth(width);
//    }

    public void setTitle(String title){
        this.title.set(title);
    }
    public void setValue(int value){
        this.value.set(""+value);
    }
    public void setExtra(String extra){
        this.extra.set(extra);
    }
    public void setIconPath(String path){
        if(path != null){
            this.iconPath.set(GlobalVariables.BASE_PATH + "icons/" + path);
            this.icon.setImage(SVGUtil.loadSVG(this.iconPath.get(), iconSize,iconSize));
        }
    }
    public void setIconSize(int size){
        this.iconSize = size;
    }
}
