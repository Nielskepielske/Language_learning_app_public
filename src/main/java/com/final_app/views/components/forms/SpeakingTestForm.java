package com.final_app.views.components.forms;

import com.final_app.converters.LanguageConverter;
import com.final_app.converters.LanguageLevelConverter;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.*;
import com.final_app.services.AppService;
import com.final_app.services.ConversationService;
import com.final_app.services.LanguageService;
import com.final_app.services.SpeakingTestService;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import com.final_app.views.components.AddItemFromListPopup;
import com.final_app.views.components.custom.list.ItemFormList;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SpeakingTestForm extends BaseForm {
    @Override
    public void initialize() {
        setUpOptions();
    }

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextArea txtExplanation;
    @FXML private ChoiceBox<Language> languageChoiceBox;
    @FXML private ChoiceBox<Language> languageFromChoiceBox;
    @FXML private ChoiceBox<LanguageLevel> languageLevelChoiceBox;
    @FXML private ItemFormList<SpeakingTestQuestion> speakingTestQuestionListView;
//    @FXML private HBox btnAddConversation;

    @FXML private Label btnCreate;
    @FXML private Button btnGenerate;
    @FXML private HBox generateBox;

    // System text
    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblPTitle;
    @FXML private Label lblPDescription;
    @FXML private Label lblPLanguage;
    @FXML private Label lblPLanguageFrom;
    @FXML private Label lblPLanguageLevel;
    @FXML private Label lblPExplanation;
    @FXML private Label lblPQuestions;

    private final LanguageService languageService = AppService.getInstance().getLanguageService();
    private final SpeakingTestService speakingTestService = AppService.getInstance().getSpeakingTestService();

    private ObservableList<Language> languages = FXCollections.observableArrayList();
    private ObservableList<LanguageLevel> languageLevels = FXCollections.observableArrayList();

    private SpeakingTest speakingTest = new SpeakingTest();

    private final ObjectProperty<GenerateStatus> generateStatus = new SimpleObjectProperty<>(GenerateStatus.GENERATED);

    private enum GenerateStatus {
        GENERATING,
        GENERATED,
        ERROR
    }

    public SpeakingTestForm(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/forms/SpeakingTestForm.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            Platform.runLater(()->{
                setBindings();
                //setAll(conversation);
                reloadTranslations();
                setUpOptions();
            });


//            TranslationManager.get().addLanguageChangeListener(lang -> {
//                Platform.runLater(this::reloadTranslations);
//            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void reloadTranslations(){
        lblTitle.textProperty().bind(TranslationManager.get().t(TKey.FSPEAKINGTEST));
        lblDescription.textProperty().bind(TranslationManager.get().t(TKey.SFDESCRIPTIONSPEAKINGTEST));
        lblPTitle.textProperty().bind(TranslationManager.get().t(TKey.FPTITLE));
        lblPDescription.textProperty().bind(TranslationManager.get().t(TKey.FPDESCRIPTION));
        lblPLanguage.textProperty().bind(TranslationManager.get().t(TKey.FPLANGUAGE));
        lblPLanguageFrom.textProperty().bind(TranslationManager.get().t(TKey.FPLANGUAGEFROM));
        lblPLanguageLevel.textProperty().bind(TranslationManager.get().t(TKey.FPLANGUAGELEVEL));
        lblPExplanation.textProperty().bind(TranslationManager.get().t(TKey.FPEXPLANATION));
        btnCreate.textProperty().bind(TranslationManager.get().t(TKey.FPCREATE));
        btnGenerate.textProperty().bind(TranslationManager.get().t(TKey.GENERATE));
    }

    private void setUpOptions(){
        try {
            speakingTest = new SpeakingTest();
            languages.setAll(languageService.getAllLanguages());
            for(Language lng : languages){
                languageLevels.setAll(lng.getLanguageLevelSystem().getLevels());
            }

            // Set txt fields on default
            txtDescription.setText("");
            txtTitle.setText("");
            txtExplanation.setText("");

            // reset conversation items
            speakingTestQuestionListView.clear();

            // Set up language choice box
            languageChoiceBox.setValue(null);
            languageChoiceBox.setConverter(new LanguageConverter(languages));

            // Set up language from choice box
            languageFromChoiceBox.setValue(null);
            languageFromChoiceBox.setConverter(new LanguageConverter(languages));

            // Set up language level choice box
            languageLevelChoiceBox.setValue(null);
            languageLevelChoiceBox.setConverter(new LanguageLevelConverter(languageLevels));

            generatingCheck();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadOptions(){
        if(languageChoiceBox.getValue() != null){
            languageLevels.setAll(languageChoiceBox.getValue().getLanguageLevelSystem().getLevels());
        }
    }
    private void checkGenerate(){
        btnGenerate.setDisable(txtTitle.getText().isEmpty() || txtDescription.getText().isEmpty() || languageChoiceBox.getValue() == null || languageLevelChoiceBox.getValue() == null || languageFromChoiceBox.getValue() == null);
    }

    private void setBindings(){
        languageChoiceBox.setItems(languages);
        languageFromChoiceBox.setItems(languages);
        languageLevelChoiceBox.setItems(languageLevels);
        checkGenerate();

        this.txtTitle.textProperty().addListener((obs, oldVal, newVal)->{
            speakingTest.setTitle(newVal);
            checkGenerate();
        });
        this.txtDescription.textProperty().addListener((obs, oldVal, newVal)->{
            speakingTest.setDescription(newVal);
            checkGenerate();
        });
        this.languageChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            speakingTest.setLanguage(newVal);
            reloadOptions();
            languageLevelChoiceBox.setValue(null);
            checkGenerate();
        });
        this.languageFromChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            speakingTest.setLanguageFrom(newVal);
            checkGenerate();
        });
        this.languageLevelChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            speakingTest.setLanguageLevel(newVal);
            checkGenerate();
        });
        this.txtExplanation.textProperty().addListener((obs, oldVal, newVal)->{
            speakingTest.setExplanation(newVal);
        });


        btnCreate.setOnMouseClicked(e -> {
            if(!(speakingTest.getQuestions() == null) &&
                    !speakingTest.getQuestions().isEmpty() &&
                    speakingTest.getTitle() != null &&
                    speakingTest.getDescription() != null &&
                    speakingTest.getLanguage() != null &&
                    speakingTest.getLanguageLevel() != null &&
                    speakingTest.getExplanation() != null &&
                    speakingTest.getGrammarFocus() != null &&
                    speakingTest.getVocabularyTheme() != null
            ){
                List<SpeakingTestQuestion> speakingTestQuestionList = new ArrayList<>();
                for(SpeakingTestQuestion ci : speakingTestQuestionListView.getItems()){
                    int index = speakingTestQuestionListView.getItems().indexOf(ci);
                    ci.setOrderIndex(index);
                    speakingTestQuestionList.add(ci);
                }
                speakingTest.setQuestions(speakingTestQuestionList);

                try {
                    speakingTestService.createSpeakingTest(speakingTest);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } catch (ExecutionException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                setUpOptions();
            }
        });

        btnGenerate.setOnAction(e -> {
            CompletableFuture.supplyAsync(()->{
                        try {
                            generateStatus.set(GenerateStatus.GENERATING);
                            Optional<SpeakingTest> generatedTest = AppService.getInstance().getSpeakingTestService().generateSpeakingTest(speakingTest.getTitle(), speakingTest.getDescription(), speakingTest.getLanguage(), speakingTest.getLanguageFrom(), speakingTest.getLanguageLevel());
                            generatedTest.ifPresent(test -> speakingTest = test);
                        } catch (IOException ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText(ex.getMessage());
                            alert.showAndWait();
                            throw new RuntimeException(ex);
                        }
                        return speakingTest;
            })
                    .thenAccept(sp ->{
                        Platform.runLater(() -> {
                            setAll(sp);
                            generateStatus.set(GenerateStatus.GENERATED);
                            System.out.println("speakingtest set");
                        });
                    });
        });
    }
    ProgressIndicator progressIndicator = new ProgressIndicator();
    private void generatingCheck(){
        progressIndicator.setPrefSize(20, 20);
        if(!generateBox.getChildren().contains(progressIndicator)){
            generateBox.getChildren().add(progressIndicator);
            progressIndicator.setVisible(false);
        }
        generateStatus.addListener((obs, oldVal, newVal) -> {
            if(newVal == GenerateStatus.GENERATED){
                progressIndicator.setVisible(false);
                btnGenerate.setDisable(false);
            }else if(newVal == GenerateStatus.GENERATING){
                progressIndicator.setVisible(true);
                btnGenerate.setFocusTraversable(Boolean.FALSE);
                btnGenerate.setDisable(true);
            }else if(newVal == GenerateStatus.ERROR){
                progressIndicator.setVisible(false);
                btnGenerate.setDisable(false);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });
    }

    private void setAll(SpeakingTest speakingTest){
        txtTitle.setText(speakingTest.getTitle());
        txtDescription.setText(speakingTest.getDescription());
        txtExplanation.setText(speakingTest.getExplanation());

        this.speakingTestQuestionListView.setItems(speakingTest.getQuestions());
    }
}
