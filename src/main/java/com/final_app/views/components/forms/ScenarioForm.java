package com.final_app.views.components.forms;

import com.final_app.factories.RepositoryFactory;
import com.final_app.globals.Roles;
import com.final_app.globals.TKey;
import com.final_app.models.IndexedItem;
import com.final_app.models.Scenario;
import com.final_app.tools.TranslationManager;
import com.final_app.views.components.CustomDialog;
import com.final_app.views.components.custom.list.ItemFormList;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

public class ScenarioForm extends BaseForm {
    @FXML private TextArea txtDescription;
    @FXML private ChoiceBox<Roles> roleChoiceBox;
    @FXML private ItemFormList<IndexedItem> lstKeyPoints;
    //@FXML private VBox lstKeyPoints;

    @FXML private HBox btnAdd;
    @FXML private Label lblAdd;
    @FXML private Label btnCreate;

    // System text
    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblPDescription;
    @FXML private Label lblPRole;
    @FXML private Label lblPKeyPoints;


    // properties
    private StringProperty descriptionProperty = new SimpleStringProperty();

    public ScenarioForm(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/forms/ScenarioForm.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            Platform.runLater(()->{
                setAll();
                setBindings();
                reloadTranslations();
            });


//            TranslationManager.get().addLanguageChangeListener(lang -> {
//                Platform.runLater(this::reloadTranslations);
//            });
            //setAll(conversation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadTranslations(){
        lblTitle.textProperty().bind(TranslationManager.get().t(TKey.FSCENARIO));
        lblDescription.textProperty().bind(TranslationManager.get().t(TKey.SFDESCRIPTION));
        lblPDescription.textProperty().bind(TranslationManager.get().t(TKey.FPDESCRIPTION));
        lblPRole.textProperty().bind(TranslationManager.get().t(TKey.FPROLE));
        lblPKeyPoints.textProperty().bind(TranslationManager.get().t(TKey.FPKEYPOINTS));
        btnCreate.textProperty().bind(TranslationManager.get().t(TKey.FPCREATE));
        lblAdd.textProperty().bind(TranslationManager.get().t(TKey.FPADD));

    }

    private void setAll(){
        this.descriptionProperty.set("");
        this.lstKeyPoints.clear();
        this.roleChoiceBox.getItems().addAll(Roles.values());
        this.roleChoiceBox.setValue(null);
    }

    private void setBindings(){
        txtDescription.textProperty().bindBidirectional(this.descriptionProperty);

        btnAdd.setOnMouseClicked(e->{
            setDialog();
        });
        btnCreate.setOnMouseClicked(e->{
            if(txtDescription.getText() != null && !txtDescription.getText().isEmpty() && !lstKeyPoints.getItems().isEmpty() && roleChoiceBox.getValue() != null){
                Scenario scenario = new Scenario();
                scenario.setDescription(txtDescription.getText());
                scenario.setKeyPoints(lstKeyPoints.getItems().stream().map(IndexedItem::getText).toList());
                scenario.setRoleEnum(roleChoiceBox.getValue());
                RepositoryFactory.getScenarioRepository().addScenario(scenario);
                clearForm();
            }
        });
    }

    private void setDialog(){
        Scene scene = this.getScene();
        if(scene != null){
            CustomDialog dialog = new CustomDialog(scene, TranslationManager.get().t(TKey.FPKEYPOINTHEADER), TranslationManager.get().t(TKey.FPKEYPOINT));

            Optional<String> result = dialog.showAndWaitForInput();
            result.ifPresent(keyPoint -> {
                if(!result.get().isEmpty()){
                    IndexedItem indexItem = new IndexedItem(lstKeyPoints.getItems().size() + 1, keyPoint);
                    lstKeyPoints.addItem(indexItem);
                }
            });

        }
    }

    private void clearForm(){
        this.txtDescription.setText("");
        this.lstKeyPoints.clear();
        this.roleChoiceBox.setValue(null);
        this.requestFocus();
    }

    @Override
    public void initialize() {

    }
}
