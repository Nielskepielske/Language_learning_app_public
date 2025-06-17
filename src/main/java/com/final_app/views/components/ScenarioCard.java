package com.final_app.views.components;

import com.final_app.globals.TKey;
import com.final_app.models.Scenario;
import com.final_app.tools.TranslationManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScenarioCard extends VBox {
    @FXML private Label lblDescription;
    @FXML private VBox lstKeyPoints;

    @FXML private Label lblTitle;
    @FXML private Label lblKeyPoints;

    private StringProperty descriptionProperty = new SimpleStringProperty();
    private List<String> keyPoints = new ArrayList<>();

    public ScenarioCard(
            Scenario scenario
    ){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/ScenarioCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
            setAll(scenario);

            reloadTranslations();
//            TranslationManager.get().addLanguageChangeListener(lang -> {
//                Platform.runLater(this::reloadTranslations);
//            });
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void reloadTranslations(){
        lblTitle.textProperty().bind(TranslationManager.get().t(TKey.FSCENARIO));
        lblKeyPoints.textProperty().bind(TranslationManager.get().t(TKey.CHKEYPOINTSTOPRACTICE));
    }

    public void setBindings(){
        lblDescription.textProperty().bind(this.descriptionProperty);
    }
    public void setAll(Scenario scenario){
        this.descriptionProperty.set(scenario.getDescription());
        this.keyPoints = scenario.getKeyPoints();
        //System.out.println(this.keyPoints);
        this.keyPoints.forEach(point -> {
            Label listItem = new Label("• " +  point);
            listItem.getStyleClass().addAll("primary", "b3");
            lstKeyPoints.getChildren().add(listItem);
        });
    }
}
