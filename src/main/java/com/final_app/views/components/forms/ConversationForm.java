package com.final_app.views.components.forms;

import com.final_app.converters.LanguageConverter;
import com.final_app.converters.LanguageLevelConverter;
import com.final_app.converters.ScenarioConverter;
import com.final_app.globals.TKey;
import com.final_app.models.Conversation;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.Scenario;
import com.final_app.services.AppService;
import com.final_app.services.ConversationService;
import com.final_app.services.LanguageService;
import com.final_app.services.ScenarioService;
import com.final_app.tools.TranslationManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ConversationForm extends BaseForm {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ChoiceBox<Language> languageChoiceBox;
    @FXML private ChoiceBox<Language> languageFromChoiceBox;
    @FXML private ChoiceBox<LanguageLevel> languageLevelChoiceBox;
    @FXML private ChoiceBox<Scenario> scenarioChoiceBox;
    @FXML private TextField txtStartPrompt;

    @FXML private Label btnCreate;

    // System Text
    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblPTitle;
    @FXML private Label lblPDescription;
    @FXML private Label lblPLanguage;
    @FXML private Label lblPLanguageFrom;
    @FXML private Label lblPLanguageLevel;
    @FXML private Label lblPScenario;
    @FXML private Label lblPStartPrompt;

    private final LanguageService languageService = AppService.getInstance().getLanguageService();
    private final ConversationService conversationService = AppService.getInstance().getConversationService();
    private final ScenarioService scenarioService = AppService.getInstance().getScenarioService();

    private Conversation conversation = new Conversation();

    private ObservableList<Language> languages = FXCollections.observableArrayList();
    private ObservableList<LanguageLevel> languageLevels = FXCollections.observableArrayList();
    private ObservableList<Scenario> scenarios = FXCollections.observableArrayList();

    public ConversationForm(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/forms/ConversationForm.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            Platform.runLater(()->{
                setBindings();
                setUp();
                setOnChanges();
                reloadTranslations();
            });


//            TranslationManager.get().addLanguageChangeListener(lang -> {
//                Platform.runLater(this::reloadTranslations);
//            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void reloadTranslations(){
        lblTitle.textProperty().bind(TranslationManager.get().t(TKey.FCONVERSATION));
        lblDescription.textProperty().bind(TranslationManager.get().t(TKey.CFDESCRIPTION));
        lblPTitle.textProperty().bind(TranslationManager.get().t(TKey.FPTITLE));
        lblPDescription.textProperty().bind(TranslationManager.get().t(TKey.FPDESCRIPTION));
        lblPLanguage.textProperty().bind(TranslationManager.get().t(TKey.FPLANGUAGE));
        lblPLanguageFrom.textProperty().bind(TranslationManager.get().t(TKey.FPLANGUAGEFROM));
        lblPLanguageLevel.textProperty().bind(TranslationManager.get().t(TKey.FPLANGUAGELEVEL));
        lblPScenario.textProperty().bind(TranslationManager.get().t(TKey.FSCENARIO));
        lblPStartPrompt.textProperty().bind(TranslationManager.get().t(TKey.FPSTARTPROMPT));
        btnCreate.textProperty().bind(TranslationManager.get().t(TKey.FPCREATE));
    }
    private void setBindings(){
        languageChoiceBox.setItems(languages);
        languageFromChoiceBox.setItems(languages);
        languageLevelChoiceBox.setItems(languageLevels);
        scenarioChoiceBox.setItems(scenarios);

        languageChoiceBox.setConverter(new LanguageConverter(languages));
        languageFromChoiceBox.setConverter(new LanguageConverter(languages));
        languageLevelChoiceBox.setConverter(new LanguageLevelConverter(languageLevels));
        scenarioChoiceBox.setConverter(new ScenarioConverter(scenarios));
    }

    private void setUp(){
        try {
            conversation = new Conversation();
            txtTitle.setText("");
            txtDescription.setText("");
            txtStartPrompt.setText("");

            scenarioChoiceBox.setValue(null);
            languageChoiceBox.setValue(null);
            languageFromChoiceBox.setValue(null);
            languageLevelChoiceBox.setValue(null);

            languages.setAll(languageService.getAllLanguages());
            scenarios.setAll(scenarioService.getAllScenarios());

            languageChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
                reloadItems();
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void reloadItems(){
        if(languageChoiceBox.getValue() != null){
            languageLevels.setAll(languageChoiceBox.getValue().getLanguageLevelSystem().getLevels());
        }else{
            languageLevels.setAll(new ArrayList<>());
        }
    }

    private void setOnChanges(){
        txtTitle.textProperty().addListener((obs, oldVal, newVal)->{
            conversation.setTitle(newVal);
        });
        txtDescription.textProperty().addListener((obs, oldVal, newVal)->{
            conversation.setDescription(newVal);
        });
        languageChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            conversation.setLanguage(newVal);
        });
        languageFromChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            conversation.setLanguageFrom(newVal);
        });
        languageLevelChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            conversation.setLanguageLevel(newVal);
        });
        scenarioChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            conversation.setScenario(newVal);
        });
        txtStartPrompt.textProperty().addListener((obs, oldVal, newVal)->{
            conversation.setStartPrompt(newVal);
        });

        btnCreate.setOnMouseClicked(e -> {
            try {
                if(conversation.getTitle() != null && conversation.getDescription() != null && conversation.getLanguage() != null && conversation.getLanguageLevel() != null && conversation.getScenario() != null && conversation.getStartPrompt() != null && conversation.getLanguageFrom() != null){
                    conversationService.createConversation(conversation);
                    //EventBus.getInstance().post(new CreateEvent(CreateEvent.ANY));
                    setUp();
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void initialize() {
        setUp();
    }
}
