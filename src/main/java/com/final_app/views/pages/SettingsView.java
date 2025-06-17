package com.final_app.views.pages;

import com.final_app.converters.LanguageConverter;
import com.final_app.globals.TKey;
import com.final_app.models.Language;
import com.final_app.models.Settings;
import com.final_app.tools.TranslationManager;
import com.final_app.viewmodels.SettingsViewModel;
import com.final_app.views.components.custom.selection.SelectableComponent;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.internal.viewloader.View;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class SettingsView implements FxmlView<SettingsViewModel> {
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;

    @FXML private Button btnSave;
    @FXML private Button btnSaveExtraSettings;

    @FXML private ChoiceBox<Language> choiceSystemLanguage;
    @FXML private FlowPane lstSelectedLanguages;

    @InjectViewModel private SettingsViewModel viewModel;

    // System text elements
    @FXML public Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblAccountTitle;
    @FXML private Label lblAccountDescription;
    @FXML private Label lblAccountUsername;
    @FXML private Label lblAccountEmail;
    @FXML private Label lblLanguageTitle;
    @FXML private Label lblLanguageDescription;
    @FXML private Label lblLanguageSub;
    @FXML private Label lblSelectedLanguages;


    public void initialize() {
        Platform.runLater(()->{
            txtEmail.textProperty().bindBidirectional(viewModel.email);
            txtUsername.textProperty().bindBidirectional(viewModel.username);

           choiceSystemLanguage.setItems(viewModel.getLanguages());
           choiceSystemLanguage.setConverter(new LanguageConverter());

//           if(viewModel.getSettings() != null && viewModel.getSettings().getLanguage() != null){
//               choiceSystemLanguage.getSelectionModel().select(viewModel.getSettings().getLanguage());
//           }

            viewModel.getLanguages().addListener((ListChangeListener<Language>) c -> {
                choiceSystemLanguage.getSelectionModel().select(viewModel.selectedLanguageProperty.get());
            });
            if(viewModel.selectedLanguageProperty.get() != null) {
                choiceSystemLanguage.getSelectionModel().select(viewModel.selectedLanguageProperty.get());
            } else {
                choiceSystemLanguage.getSelectionModel().selectFirst();
            }
           viewModel.selectedLanguageProperty.addListener((observable, oldValue, newValue) -> {
                if(newValue != null){
                     choiceSystemLanguage.getSelectionModel().select(newValue);
                }
           });
           choiceSystemLanguage.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue != null){
                    viewModel.selectedLanguageProperty.set(newValue);
                }
           });

           btnSave.setOnAction(event -> {
                viewModel.saveUser(txtEmail.getText(), txtUsername.getText());
           });
           btnSaveExtraSettings.setOnAction(event -> {
               Settings tempSettings = viewModel.getSettings();
               tempSettings.setLanguage(choiceSystemLanguage.getValue());
                viewModel.saveSettings(tempSettings);
           });

           viewModel.selectedLanguages.addListener((ListChangeListener<String>) c -> {
               setSelectedLanguages();
           });

           bindUIText();
           setSelectedLanguages();
        });
    }

    private void bindUIText(){
        lblTitle.textProperty().bind(viewModel.lblTitleProperty);
        lblDescription.textProperty().bind(viewModel.lblDescriptionProperty);
        lblAccountTitle.textProperty().bind(viewModel.lblAccountTitleProperty);
        lblAccountDescription.textProperty().bind(viewModel.lblAccountDescriptionProperty);
        lblAccountUsername.textProperty().bind(viewModel.lblAccountUsernameProperty);
        lblAccountEmail.textProperty().bind(viewModel.lblAccountEmailProperty);
        lblLanguageTitle.textProperty().bind(viewModel.lblLanguageTitleProperty);
        lblLanguageDescription.textProperty().bind(viewModel.lblLanguageDescriptionProperty);
        lblLanguageSub.textProperty().bind(viewModel.lblLanguageSubTitleProperty);
        lblSelectedLanguages.textProperty().bind(TranslationManager.get().t(TKey.SELECTEDLANGUAGES));

        btnSave.textProperty().bind(viewModel.btnTextSaveProperty);
        btnSaveExtraSettings.textProperty().bind(viewModel.btnTextSaveProperty);
    }

    private void setSelectedLanguages(){
        lstSelectedLanguages.getChildren().clear();
        for(Language l :viewModel.getLanguages()){
            SelectableComponent<Language> comp = new SelectableComponent<Language>(l);

            comp.setOnSelectionChanged(_ ->{
                if(comp.isSelected){
                    viewModel.selectedLanguages.add(l.getId());
                }else{
                    viewModel.selectedLanguages.remove(l.getId());
                }
            });

            if(viewModel.selectedLanguages.contains(l.getId())){
                comp.select(false);
            }
            lstSelectedLanguages.getChildren().add(comp);
        }
    }
}
