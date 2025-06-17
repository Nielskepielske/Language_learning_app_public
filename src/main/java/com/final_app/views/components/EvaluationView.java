package com.final_app.views.components;

import com.final_app.events.XpEarnedEvent;
import com.final_app.globals.TKey;
import com.final_app.models.Evaluation;
import com.final_app.services.UserService;
import com.final_app.tools.TranslationManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.function.Consumer;

public class EvaluationView extends VBox {
    // Upper section
    @FXML private Label totalScore;
    @FXML private CircularProgressBar progressCircle;
    @FXML private Label evaluationTitle;

    // XP Section
    @FXML private VBox xpSection;
    @FXML private Label lblXpEarned;
    @FXML private Label lblLevelUp;

    // Lower section - Feedback view
    @FXML private VBox feedbackSection;
    @FXML private Label lblDescription;

    // Lower section - Stats view
    @FXML private VBox statsSection;
    @FXML private Label lblVocab;
    @FXML private Label lblGrammar;
    @FXML private ProgressBar vocabProgressBar;
    @FXML private ProgressBar grammarProgressBar;

    // Toggle button
    @FXML private Button btnToggleView;

    // Close button
    @FXML private Button btnNext;

    @FXML private Pane pane;
    @FXML private VBox root;

    // System text
    @FXML private Label lblFeedbackTitle;
    @FXML private Label lblSkillTitle;

    private Evaluation currentEvaluation;
    private Consumer<Void> onButtonNextClick;
    private boolean showingFeedback = true; // Track which view is showing
    private UserService.XpTransaction xpTransaction;

    public EvaluationView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/EvaluationView.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            //pane.prefHeightProperty().bind(root.heightProperty());

            // Set up the circular progress bar
            progressCircle.setSize(120);
            progressCircle.setStrokeWidth(12);
            progressCircle.setTrackColor("#26272B");
            progressCircle.setProgressColor("#3882e4");

            // Initially hide XP section
            xpSection.setVisible(false);
            xpSection.setManaged(false);
            lblLevelUp.setVisible(false);

            // Initial state - show feedback, hide stats
            feedbackSection.setVisible(true);
            statsSection.setVisible(false);
            btnToggleView.setText("Show Stats");

            // Set up button actions
            btnNext.setOnAction(event -> {
                if (onButtonNextClick != null) {
                    onButtonNextClick.accept(null);
                }
            });

            btnToggleView.setOnAction(event -> toggleView());

            reloadTranslations();

            TranslationManager.get().addLanguageChangeListener(lang -> {
                Platform.runLater(this::reloadTranslations);
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadTranslations(){
        evaluationTitle.textProperty().bind(TranslationManager.get().t(TKey.EVTITLEN));
        lblFeedbackTitle.textProperty().bind(TranslationManager.get().t(TKey.EVFEEDBACK).concat(":"));
        lblSkillTitle.textProperty().bind(TranslationManager.get().t(TKey.EVSTATSTITLE).concat(":"));

        btnNext.textProperty().bind(TranslationManager.get().t(TKey.EVBTNNEXT));
        btnToggleView.textProperty().bind((showingFeedback) ? TranslationManager.get().t(TKey.EVBTNSHOWSTATS) : TranslationManager.get().t(TKey.EVBTNSHOWFEEDBACK) );

        if(currentEvaluation != null){
            setScore(currentEvaluation);
        }
    }

    /**
     * Toggles between feedback view and stats view
     */
    private void toggleView() {
        showingFeedback = !showingFeedback;

        if (showingFeedback) {
            // Switch to feedback view
            feedbackSection.setVisible(true);
            feedbackSection.setManaged(true);
            statsSection.setVisible(false);
            statsSection.setManaged(false);
            btnToggleView.textProperty().bind(TranslationManager.get().t(TKey.EVBTNSHOWSTATS));
        } else {
            // Switch to stats view
            feedbackSection.setVisible(false);
            feedbackSection.setManaged(false);
            statsSection.setVisible(true);
            statsSection.setManaged(true);
            btnToggleView.textProperty().bind(TranslationManager.get().t(TKey.EVBTNSHOWFEEDBACK));
        }
    }

    public void setOnButtonNextClick(Consumer<Void> listener) {
        this.onButtonNextClick = listener;
    }

    /**
     * Sets the title of the evaluation view
     */
    public void setTitle(String title) {
        evaluationTitle.setText(title);
    }

    /**
     * Sets all the evaluation data and updates the UI
     */
    public void setScore(Evaluation ev) {
        currentEvaluation = ev;

        // Update the score display
        totalScore.setText(ev.getScore() + "/" + ev.getMaxScore());

        // Calculate and animate the progress
        double progressValue = (double) ev.getScore() / ev.getMaxScore();
        progressCircle.animateProgress(progressValue, 1000);

        // Update feedback section
        lblDescription.setText(ev.getFeedback());

        // Update stats section
        lblVocab.setText(TranslationManager.get().t(TKey.EVVOCAB).get() + ": " + ev.getVocab() + "/" + ev.maxPointsPerCriteria());
        lblGrammar.setText(TranslationManager.get().t(TKey.EVGRAMMAR).get() + ": " + ev.getGrammar() + "/" + ev.maxPointsPerCriteria() );

        // Update progress bars
        vocabProgressBar.setProgress((double) ev.getVocab() / ev.maxPointsPerCriteria() );
        grammarProgressBar.setProgress((double) ev.getGrammar() / ev.maxPointsPerCriteria() );
    }

    /**
     * Sets the XP transaction data and shows the XP section
     */
    public void setXpTransaction(UserService.XpTransaction transaction) {
        this.xpTransaction = transaction;

        if (transaction != null) {
            // Update XP earned label
            lblXpEarned.setText("+" + transaction.getAmount() + " XP!");

            // Show level up message if applicable
            if (transaction.isLeveledUp()) {
                lblLevelUp.setText("Level Up! You're now level " + transaction.getNewLevel() + "!");
                lblLevelUp.setVisible(true);
                lblLevelUp.setManaged(true);
                lblLevelUp.getStyleClass().add("level-up-text");
            } else {
                lblLevelUp.setVisible(false);
                lblLevelUp.setManaged(false);
            }

            // Show XP section with animation
            xpSection.setVisible(true);
            xpSection.setManaged(true);
            xpSection.setOpacity(0);

            // Create fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), xpSection);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            // Pause before starting animation
            PauseTransition pause = new PauseTransition(Duration.millis(500));

            // Create sequential animation
            SequentialTransition sequence = new SequentialTransition(pause, fadeIn);
            sequence.play();

            // Apply pulse animation to XP amount
            lblXpEarned.getStyleClass().add("xp-pulse");
        }
    }

    /**
     * Sets the color of the progress circle
     */
    public void setProgressColor(String colorHex) {
        progressCircle.setProgressColor(colorHex);
    }

    public Evaluation getEvaluation() { return this.currentEvaluation;}

    public void disableNextButton(){
        btnNext.setDisable(true);
    }
}