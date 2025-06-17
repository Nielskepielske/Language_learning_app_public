package com.final_app.views.components;

import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.UserLanguage;
import com.final_app.services.AppService;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import com.final_app.views.components.custom.general.SummaryComponent;
import com.final_app.views.components.custom.selection.SelectableComponent;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class LanguageSelectionView extends VBox {
    // Language list
    @FXML private Label lblLanguageListTitle;
    @FXML private Label lblLanguageListDescription;
    @FXML private FlowPane lstLanguages;

    // Language level selection
    @FXML private Label lblProficiencyTitle;
    @FXML private Label lblProficiencyDescription;
    @FXML private FlowPane lstLanguageLevels;

    // Summary
    @FXML private Label lblSummaryTitle;
    @FXML private Label lblSummaryDescription;
    @FXML private VBox summaryBox;
    @FXML private Button btnAddLanguage;

    @FXML private HBox btnBack;
    @FXML private ImageView imgBack;

    // Titles
    @FXML private Label lblTitle;
    @FXML private Label lblDescription;

    private Consumer<Void> onBackButtonClicked;
    private Consumer<UserLanguage> onAddClicked;
    public void setOnBackButtonClicked(Consumer<Void> onBackButtonClicked){this.onBackButtonClicked = onBackButtonClicked;}
    public void setOnAddClicked(Consumer<UserLanguage> onAddClicked){this.onAddClicked = onAddClicked;}

    private ObjectProperty<Language> selectedLanguage = new SimpleObjectProperty<>();
    public Language getSelectedLanguage(){return selectedLanguage.get();}
    public void setSelectedLanguage(Language language){selectedLanguage.set(language);}

    private ObjectProperty<LanguageLevel> selectedLanguageLevel = new SimpleObjectProperty<>();

    // System text properties
    private StringProperty titleProperty = new SimpleStringProperty();
    private StringProperty descriptionProperty = new SimpleStringProperty();
    private StringProperty selectLanguageTitleProperty = new SimpleStringProperty();
    private StringProperty selectLanguageDescriptionProperty = new SimpleStringProperty();
    private StringProperty proficiencyTitleProperty = new SimpleStringProperty();
    private StringProperty proficiencyDescriptionProperty = new SimpleStringProperty();
    private StringProperty summaryTitleProperty = new SimpleStringProperty();
    private StringProperty summaryDescriptionProperty = new SimpleStringProperty();
    private StringProperty addLanguageButtonProperty = new SimpleStringProperty();

    public LanguageSelectionView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/LanguageSelectionView.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            configureBackButton();
            configureLists();
            Platform.runLater(()->{
                bindText();
                bindActions();
                reloadSystemText();
            });

//            TranslationManager.get().addLanguageChangeListener(lng -> {
//                Platform.runLater(this::reloadSystemText);
//            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void bindText(){
        titleProperty.bind(TranslationManager.get().t(TKey.NLTITLE));
        descriptionProperty.bind(TranslationManager.get().t(TKey.NLDESCRIPTION));
        selectLanguageTitleProperty.bind(TranslationManager.get().t(TKey.NLSELECTLANGUAGE));
        selectLanguageDescriptionProperty.bind(TranslationManager.get().t(TKey.NLSELECTLANGUAGEDES));
        proficiencyTitleProperty.bind(TranslationManager.get().t(TKey.NLPROFICIENCY));
        proficiencyDescriptionProperty.bind(TranslationManager.get().t(TKey.NLPROFICIENCYDES));
        summaryTitleProperty.bind(TranslationManager.get().t(TKey.NLSUMMARY));
        summaryDescriptionProperty.bind(TranslationManager.get().t(TKey.NLSUMMARYDES));
        addLanguageButtonProperty.bind(TranslationManager.get().t(TKey.NLCOMFIRMBTN));
    }
    private void configureBackButton() {
        imgBack.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "back_arrow_light.svg", 30,30));

        btnBack.setOnMouseClicked(e -> {
            onBackButtonClicked.accept(null);
        });
    }
    private void configureLists(){
        lstLanguages.setVgap(10);
        lstLanguages.setHgap(10);

        lstLanguageLevels.setVgap(10);
        lstLanguageLevels.setHgap(10);
        lstLanguageLevels.setPrefWrapLength(lstLanguages.getWidth());
    }
    public void initializeRows(List<Language> languages){
        lstLanguages.getChildren().clear();

        for(Language language : languages){
            SelectableComponent<Language> languageComponent = new SelectableComponent<>(language);
            lstLanguages.getChildren().add(languageComponent);

            languageComponent.setOnSelectionChanged(selected -> {
                lstLanguageLevels.getChildren().clear();
                if(selected != null && languageComponent.isSelected){
                    selectedLanguage.set(language);
                    selectedLanguageLevel.set(null);
                    lstLanguages.getChildren().forEach(node -> {
                        if(node instanceof SelectableComponent<?> && !((SelectableComponent<?>) node == languageComponent)){
                            SelectableComponent<?> selectableComponent = (SelectableComponent<?>) node;
                            selectableComponent.deselect(false);
                        }
                    });
                    language.getLanguageLevelSystem().getLevels()
                            .forEach(level -> {
                                SelectableComponent<LanguageLevel> languageLevelSelectableComponent = new SelectableComponent<>(level);
                                languageLevelSelectableComponent.setPrefWidth(lstLanguageLevels.getWidth() - 10);
                                lstLanguageLevels.getChildren().add(languageLevelSelectableComponent);
                                lstLanguageLevels.setOrientation(Orientation.VERTICAL);
                                lstLanguageLevels.setPrefWrapLength(lstLanguageLevels.getWidth());

                                languageLevelSelectableComponent.setOnSelectionChanged(selectedLevel -> {
                                    if(selectedLevel != null && languageLevelSelectableComponent.isSelected){
                                        selectedLanguageLevel.set(level);
                                        lstLanguageLevels.getChildren().forEach(node -> {
                                            if(node instanceof SelectableComponent<?> && !((SelectableComponent<?>) node == languageLevelSelectableComponent)){
                                                SelectableComponent<?> selectableComponent = (SelectableComponent<?>) node;
                                                selectableComponent.deselect(false);
                                            }
                                        });
                                    }else{
                                        selectedLanguageLevel.set(null);
                                    }
                                });
                            });
                }else {
                    selectedLanguageLevel.set(null);
                }
            });
        }
    }

    private void bindActions(){
        btnAddLanguage.setDisable(true);
        selectedLanguage.addListener((obs, oldLanguage, newLanguage) -> {
            setSummary();
            if(newLanguage == null){
                lstLanguageLevels.getChildren().clear();
            }
        });
        selectedLanguageLevel.addListener(((observableValue, oldLanguageLevel, newLanguageLevel) -> {
            setSummary();
        }));

        btnAddLanguage.setOnAction(e -> {
            UserLanguage userLanguage = new UserLanguage();
            userLanguage.setUserId(AppService.getInstance().getCurrentUser().getId());
            userLanguage.setLanguage(selectedLanguage.get());
            userLanguage.setLevel(selectedLanguageLevel.get());
            onAddClicked.accept(userLanguage);
            selectedLanguage.set(null);
            selectedLanguageLevel.set(null);
        });

        // Bind system text
        lblTitle.textProperty().bind(titleProperty);
        lblDescription.textProperty().bind(descriptionProperty);
        lblLanguageListTitle.textProperty().bind(selectLanguageTitleProperty);
        lblLanguageListDescription.textProperty().bind(selectLanguageDescriptionProperty);
        lblProficiencyTitle.textProperty().bind(proficiencyTitleProperty);
        lblProficiencyDescription.textProperty().bind(proficiencyDescriptionProperty);
        lblSummaryTitle.textProperty().bind(summaryTitleProperty);
        lblSummaryDescription.textProperty().bind(summaryDescriptionProperty);
        btnAddLanguage.textProperty().bind(addLanguageButtonProperty);
    }
    private void setSummary(){
        summaryBox.getChildren().clear();
        if(selectedLanguage.get() != null && selectedLanguageLevel.get() != null){
            SummaryComponent summaryComponent = new SummaryComponent(selectedLanguage.get().getIso(), selectedLanguage.get().getName(), selectedLanguageLevel.get().getName());
            summaryComponent.setCircleTextColor(selectedLanguage.get().getColor());
            summaryBox.getChildren().add(summaryComponent);
            btnAddLanguage.setDisable(false);
        }else{
            btnAddLanguage.setDisable(true);
        }
    }

    private void reloadSystemText(){
//        titleProperty.set(TranslationManager.get().t(TKey.NLTITLE));
//        descriptionProperty.set(TranslationManager.get().t(TKey.NLDESCRIPTION));
//        selectLanguageTitleProperty.set(TranslationManager.get().t(TKey.NLSELECTLANGUAGE));
//        selectLanguageDescriptionProperty.set(TranslationManager.get().t(TKey.NLSELECTLANGUAGEDES));
//        proficiencyTitleProperty.set(TranslationManager.get().t(TKey.NLPROFICIENCY));
//        proficiencyDescriptionProperty.set(TranslationManager.get().t(TKey.NLPROFICIENCYDES));
//        summaryTitleProperty.set(TranslationManager.get().t(TKey.NLSUMMARY));
//        summaryDescriptionProperty.set(TranslationManager.get().t(TKey.NLSUMMARYDES));
//        addLanguageButtonProperty.set(TranslationManager.get().t(TKey.NLCOMFIRMBTN));
    }
}
