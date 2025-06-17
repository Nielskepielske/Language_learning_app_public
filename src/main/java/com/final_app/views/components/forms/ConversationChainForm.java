package com.final_app.views.components.forms;

import com.final_app.converters.LanguageConverter;
import com.final_app.converters.LanguageLevelConverter;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.*;
import com.final_app.services.AppService;
import com.final_app.services.ConversationService;
import com.final_app.services.LanguageService;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import com.final_app.views.components.AddItemFromListPopup;
import com.final_app.views.components.custom.list.ItemFormList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ConversationChainForm extends BaseForm {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ChoiceBox<Language> languageChoiceBox;
    @FXML private ChoiceBox<Language> languageFromChoiceBox;
    @FXML private ChoiceBox<LanguageLevel> languageLevelChoiceBox;
    //@FXML private ListView<ConversationChainItem> conversationListView;
    @FXML private ItemFormList<ConversationChainItem> conversationListView;
    @FXML private HBox btnAddConversation;

    @FXML private Label btnCreate;

    @FXML private VBox mainBox;

    // System text
    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblPTitle;
    @FXML private Label lblPDescription;
    @FXML private Label lblPLanguage;
    @FXML private Label lblPLanguageFrom;
    @FXML private Label lblPLanguageLevel;
    @FXML private Label lblPConversations;
    @FXML private Label lblAdd;

    private final LanguageService languageService = AppService.getInstance().getLanguageService();
    private final ConversationService conversationService = AppService.getInstance().getConversationService();

    private ObservableList<Language> languages = FXCollections.observableArrayList();
    private ObservableList<LanguageLevel> languageLevels = FXCollections.observableArrayList();

    private ConversationChain conversationChain = new ConversationChain();

    public ConversationChainForm(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/forms/ConversationChainForm.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            Platform.runLater(()->{
                setBindings();
                //setAll(conversation);
                setUpOptions();

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
        lblTitle.textProperty().bind(TranslationManager.get().t(TKey.FCONVERSATIONCHAIN));
        lblDescription.textProperty().bind(TranslationManager.get().t(TKey.CCFDESCRIPTION));
        lblPTitle.textProperty().bind(TranslationManager.get().t(TKey.FPTITLE));
        lblPDescription.textProperty().bind(TranslationManager.get().t(TKey.FPDESCRIPTION));
        lblPLanguage.textProperty().bind(TranslationManager.get().t(TKey.FPLANGUAGE));
        lblPLanguageFrom.textProperty().bind(TranslationManager.get().t(TKey.FPLANGUAGEFROM));
        lblPLanguageLevel.textProperty().bind(TranslationManager.get().t(TKey.FPLANGUAGELEVEL));
        lblPConversations.textProperty().bind(TranslationManager.get().t(TKey.CONVERSATIONS));
        lblAdd.textProperty().bind(TranslationManager.get().t(TKey.FPADD));
        btnCreate.textProperty().bind(TranslationManager.get().t(TKey.FPCREATE));
    }

    private void setUpOptions(){
        try {
            conversationChain = new ConversationChain();
            languages.setAll(languageService.getAllLanguages());
            for(Language lng : languages){
                languageLevels.setAll(lng.getLanguageLevelSystem().getLevels());
            }

            // Set txt fields on default
            txtDescription.setText("");
            txtTitle.setText("");

            // reset conversation items
            conversationListView.clear();

            // Set up language choice box
            languageChoiceBox.setValue(null);
            languageChoiceBox.setConverter(new LanguageConverter(languages));

            // Set up language from choice box
            languageFromChoiceBox.setValue(null);
            languageFromChoiceBox.setConverter(new LanguageConverter(languages));

            // Set up language level choice box
            languageLevelChoiceBox.setValue(null);
            languageLevelChoiceBox.setConverter(new LanguageLevelConverter(languageLevels));


            // Set events
            btnAddConversation.setOnMouseClicked(e -> {
                try {
                    AddItemFromListPopup<Conversation> addItemFromListPopup = new AddItemFromListPopup<>(conversationService.getAllConversations().stream().filter(c -> {
                        if(conversationListView.getItems().stream().anyMatch(ci -> Objects.equals(ci.getConversation().getId(), c.getId()))) {
                            return false;
                        }
                        if(!Objects.equals(c.getLanguageId(), languageChoiceBox.getValue().getId()) || !Objects.equals(c.getLanguageFromId(), languageFromChoiceBox.getValue().getId())) {
                            return false;
                        }
                        if(languageChoiceBox.getValue() != null){
                            return c.getLanguage().equals(languageChoiceBox.getValue());
                        }else{
                            return false;
                        }
                    }).toList(), TKey.FPADDCONVERSATIONSTITLE, TKey.FPADDCONVERSATIONSDESCRIPTION);
                    this.getChildren().add(addItemFromListPopup);
                    mainBox.setVisible(false);
                    mainBox.setManaged(false);

//                    addItemFromListPopup.setOnAdd(conversation -> {
//                        ConversationChainItem conversationChainItem = new ConversationChainItem();
//                        conversationChainItem.setConversation(conversation);
//                        //conversationListView.getItems().add(conversationChainItem);
//                        int index = conversationListView.getItems().size() + 1;
//
//                        //int index = conversationListView.getItems().indexOf(conversation);
//                        conversationChainItem.setConversationIndex(index);
//
//                        System.out.println("index: " + index);
//                        conversationListView.addItem(conversationChainItem);
//                        this.getChildren().remove(addItemFromListPopup);
//                    });
                    addItemFromListPopup.setOnAddList(conversations -> {
                        for(Conversation conversation : conversations){
                            ConversationChainItem conversationChainItem = new ConversationChainItem();
                            conversationChainItem.setConversation(conversation);

                            int index = conversationListView.getItems().size() + 1;
                            conversationChainItem.setConversationIndex(index);
                            conversationListView.addItem(conversationChainItem);
                        }
                        this.getChildren().remove(addItemFromListPopup);
                        mainBox.setVisible(true);
                        mainBox.setManaged(true);
                    });
                    addItemFromListPopup.setOnCancel(cancel -> {
                        this.getChildren().remove(addItemFromListPopup);
                        mainBox.setVisible(true);
                        mainBox.setManaged(true);
                    });
                } catch (SQLException | ExecutionException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });

        } catch (SQLException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadOptions(){
        if(languageChoiceBox.getValue() != null){
            languageLevels.setAll(languageChoiceBox.getValue().getLanguageLevelSystem().getLevels());
        }
    }

    private void setBindings(){
        languageChoiceBox.setItems(languages);
        languageFromChoiceBox.setItems(languages);
        languageLevelChoiceBox.setItems(languageLevels);

        this.txtTitle.textProperty().addListener((obs, oldVal, newVal)->{
            conversationChain.setTitle(newVal);
        });
        this.txtDescription.textProperty().addListener((obs, oldVal, newVal)->{
            conversationChain.setDescription(newVal);
        });
        this.languageChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            conversationChain.setLanguage(newVal);
            reloadOptions();
        });
        this.languageFromChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            conversationChain.setLanguageFrom(newVal);
            reloadOptions();
        });
        this.languageLevelChoiceBox.valueProperty().addListener((obs, oldVal, newVal)->{
            conversationChain.setLanguageLevel(newVal);
        });
        this.conversationListView.setOnListChanged( change -> {
                    conversationChain.setConversations(this.conversationListView.getItems());
                    System.out.println("conversationchaintitems: " + conversationChain.getConversations());
        });

        btnCreate.setOnMouseClicked(e -> {
            if(!(conversationChain.getConversations() == null) &&
                    !conversationChain.getConversations().isEmpty() &&
                            conversationChain.getTitle() != null &&
                            conversationChain.getDescription() != null &&
                            conversationChain.getLanguage() != null &&
                            conversationChain.getLanguageFrom() != null &&
                            conversationChain.getLanguageLevel() != null){
                List<ConversationChainItem> conversationChainItems = new ArrayList<>();
                for(ConversationChainItem ci : conversationListView.getItems()){
                    int index = conversationListView.getItems().indexOf(ci);
                    ci.setConversationIndex(index);
                    conversationChainItems.add(ci);
                }
                conversationChain.setConversations(conversationChainItems);

                try {
                    conversationService.createConversationChain(conversationChain);
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
    }

    @Override
    public void initialize() {
        setUpOptions();
    }
}
