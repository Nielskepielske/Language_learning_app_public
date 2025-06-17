package com.final_app.views.components;

import com.final_app.globals.ConversationStatus;
import com.final_app.globals.Difficulty;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.*;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class SpeakingTestCard<T>  extends VBox {

    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblStatus;
    @FXML private Button actionButton;
    @FXML private ImageView icon;
    @FXML private HBox header;

    @FXML private Label circle;
    @FXML private Label lblLanguage;
    @FXML private Label lblDifficulty;
    @FXML private Label statusIcon;
    @FXML private Label lblLanguageLevel;
    @FXML private Label lblScore;
    @FXML private ProgressBar progress;


    private StringProperty titleProperty = new SimpleStringProperty();
    private StringProperty languageProperty = new SimpleStringProperty();
    private StringProperty descriptionProperty = new SimpleStringProperty();
    private StringProperty statusProperty = new SimpleStringProperty();
    private StringProperty languageLevelProperty = new SimpleStringProperty("Beginner");
    private DoubleProperty progressProperty = new SimpleDoubleProperty(0);
    private StringProperty scoreProperty = new SimpleStringProperty("0/100");
    private StringProperty difficultyProperty = new SimpleStringProperty();

    private int iconSize = 60;
    private String iconPath = GlobalVariables.ICONS + "microphone_light.svg";

    private Consumer<Void> onButtonClick;
    public void setOnButtonClick(Consumer<Void> onButtonClick){this.onButtonClick = onButtonClick;}

    private T object = null;

    public SpeakingTestCard(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/SpeakingTestCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public SpeakingTestCard(String title,
                            String description,
                            Language language,
                            ConversationStatus status,
                            String iconPath,
                            LanguageLevel languageLevel){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/SpeakingTestCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();

            setAll(title, description, language, status, iconPath, languageLevel, null);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public SpeakingTestCard(
            T test
    ){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/SpeakingTestCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
            object = test;

            if(test instanceof SpeakingTest){
                setAll(((SpeakingTest) test).getTitle(),
                        ((SpeakingTest) test).getDescription(),
                        ((SpeakingTest) test).getLanguage(),
                        ConversationStatus.NOTSTARTED,
                        iconPath,
                        ((SpeakingTest) test).getLanguageLevel(),
                        null);
            }
            if(test instanceof UserSpeakingTest){
                setAll(((UserSpeakingTest) test).getTest().getTitle(),
                        ((UserSpeakingTest) test).getTest().getDescription(),
                        ((UserSpeakingTest) test).getTest().getLanguage(),
                        ((UserSpeakingTest) test).getStatusEnum(),
                        iconPath,
                        ((UserSpeakingTest) test).getTest().getLanguageLevel(),
                        ((UserSpeakingTest) test).getResponses());
            }


        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void setBindings(){
        lblTitle.textProperty().bind(this.titleProperty);
        lblDescription.textProperty().bind(this.descriptionProperty);
        lblStatus.textProperty().bind(this.statusProperty);
        lblLanguage.textProperty().bind(this.languageProperty);
        lblLanguageLevel.textProperty().bind(this.languageLevelProperty);
        progress.progressProperty().bind(this.progressProperty);
        lblScore.textProperty().bind(this.scoreProperty);

        actionButton.setOnAction(_ ->{
            onButtonClick.accept(null);
        });
//            lblDifficulty.textProperty().bind(this.difficultyProperty);
    }

    public void setAll(String title,
                       String description,
                       Language language,
                       ConversationStatus status,
                       String iconPath,
                       LanguageLevel languageLevel,
                       List<UserSpeakingTestResponse> responses
    ){
        this.titleProperty.set(title);
        this.descriptionProperty.set(description);
        //this.statusProperty.set(status.getText());
        this.languageProperty.set(language.getName());
        this.languageLevelProperty.set(languageLevel.getName());
        this.circle.setStyle("-fx-background-color: " + language.getColor());
        actionButton.textProperty().unbind();
        this.statusProperty.unbind();
        if(status == ConversationStatus.COMPLETED){
            statusIcon.setText("✓");
            actionButton.textProperty().bind(TranslationManager.get().t(TKey.REVIEW));
            statusProperty.bind(TranslationManager.get().t(TKey.COMPLETED));
            //actionButton.setText("Review");
        }else if(status == ConversationStatus.NOTSTARTED){
            actionButton.textProperty().bind(TranslationManager.get().t(TKey.START));
            this.statusProperty.bind(TranslationManager.get().t(TKey.NOTSTARTED));
            //actionButton.setText("Start");
        }else if(status == ConversationStatus.IN_PROGRESS){
            statusIcon.setText("⟳");
            actionButton.textProperty().bind(TranslationManager.get().t(TKey.CONTINUE));
            this.statusProperty.bind(TranslationManager.get().t(TKey.INPROGRESS));
            //actionButton.setText("Continue");
        }
        if(iconPath != null){
            icon.setImage(SVGUtil.loadSVG(iconPath, iconSize, iconSize));
        }else{
            icon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/microphone.svg", iconSize, iconSize));
        }

//        String tempDifString = "";
//        for (int i = 0; i < difficulty.getValue(); i++){
//            tempDifString += "★";
//        }
//        this.difficultyProperty.set(tempDifString);
        if(responses != null && !responses.isEmpty()){
            List<Integer> scores = responses.stream().map(UserSpeakingTestResponse::getOverallScore).toList();
            System.out.println(scores);
            int totalScore = scores.stream().reduce(Integer::sum).orElse(0);
            int maxScore = 5;
            if(object instanceof SpeakingTest){
              maxScore = ((SpeakingTest) object).getQuestions().size() * 5;
            }else if(object instanceof UserSpeakingTest){
                maxScore = ((UserSpeakingTest) object).getTest().getQuestions().size() * 5;
            }
            this.progressProperty.set((double)totalScore/maxScore);
            this.scoreProperty.set(" "+totalScore+"/"+maxScore);
        }
    }
    public void setTitle(String title){
        this.titleProperty.set(title);
    }
    public void setDescription(String description){
        this.descriptionProperty.set(description);
    }
}
