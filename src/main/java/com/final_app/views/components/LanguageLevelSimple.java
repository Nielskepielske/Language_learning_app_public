package com.final_app.views.components;

import com.final_app.globals.TKey;
import com.final_app.models.UserLanguage;
import com.final_app.services.AppService;
import com.final_app.services.XpService;
import com.final_app.tools.TranslationManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LanguageLevelSimple extends VBox {
    @FXML private Label lblLanguage;
    @FXML private Label lblLanguageLevel;
    @FXML private ProgressBar progress;
    @FXML private Label circle;
    @FXML private Label lblXpToNextLevel;
    @FXML private Label lblLanguageLevelReal;

    private StringProperty languageNameProperty = new SimpleStringProperty();
    private StringProperty languageLevelNumericProperty = new SimpleStringProperty();
    private StringProperty languageLevelSystemProperty = new SimpleStringProperty();
    private DoubleProperty progressProperty = new SimpleDoubleProperty();
    private StringProperty xpToNextLevelProperty = new SimpleStringProperty();

    private final XpService xpService = AppService.getInstance().getXpService();


    public LanguageLevelSimple(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/LanguageLevelSimple.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            lblLanguage.textProperty().bind(languageNameProperty);
            lblLanguageLevel.textProperty().bind(languageLevelNumericProperty);
            lblLanguageLevelReal.textProperty().bind(languageLevelSystemProperty);
            lblXpToNextLevel.textProperty().bind(xpToNextLevelProperty);
            //progress.progressProperty().bind(progressProperty);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void startAnimation(){
        int steps = 500;
        int duration = 1000;
        double stepDuration = duration / steps;
        double progressStep = progressProperty.get() / steps;

        Timeline timeline = new Timeline();
        for (int i = 0; i < steps; i++){
            int finalI = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * stepDuration), e -> progress.setProgress(finalI * progressStep)));
        }

        timeline.setCycleCount(1);
        timeline.play();
    }


    public void setAll(UserLanguage userLanguage){
        this.languageNameProperty.set(userLanguage.getLanguage().getName());
        //this.languageLevel.set(userLanguage.getLevel().getName());

        long currentXp = userLanguage.getXp();
        int currentLanguageLevel = xpService.calculateLevelFromXp(currentXp);
        this.languageLevelSystemProperty.set(xpService.mapXpToOfficialLevel(currentXp, userLanguage.getLanguage().getLanguageLevelSystem()).getName());
        this.languageLevelNumericProperty.bind(Bindings.concat(TranslationManager.get().t(TKey.DLEVELPROGRESSH), " ", currentLanguageLevel));
        long xpToNextLevel = xpService.calculateXpForNextLevel(currentLanguageLevel) - currentXp;
        this.xpToNextLevelProperty.bind(Bindings.format("+%s %s %s %d", xpToNextLevel, TranslationManager.get().t(TKey.XPTO), TranslationManager.get().t(TKey.DLEVELPROGRESSH), (currentLanguageLevel + 1)));
        this.progressProperty.set(xpService.calculateProgressToNextLevel(currentXp, currentLanguageLevel));
        this.circle.setStyle("-fx-background-color: "+ userLanguage.getLanguage().getColor());
        startAnimation();
    }

    public void setLanguage(String language){
        this.languageNameProperty.set(language);
    }
    public void setLanguageLevel(String languageLevel){
        this.languageLevelNumericProperty.set(languageLevel);
    }
    public void setProgress(double progress){
        this.progressProperty.set(progress);
    }
}
