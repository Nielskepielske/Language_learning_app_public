package com.final_app.views.pages;

import com.final_app.events.EventBus;
import com.final_app.events.XpEarnedEvent;
import com.final_app.globals.Color;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.Evaluation;
import com.final_app.models.UserSpeakingTestResponse;
import com.final_app.tools.*;
import com.final_app.viewmodels.SpeakingTestViewModel;
import com.final_app.views.components.CircularProgressBar;
import com.final_app.views.components.QuestionCard;
import com.final_app.views.components.XpNotificationView;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpeakingTestPage implements FxmlView<SpeakingTestViewModel> {
    @FXML private Label lblTitle;
    @FXML private Label lblExtra;

    @FXML private Label lblLesson;

    @FXML private HBox btnBack;
    @FXML private ImageView imgBack;
    @FXML private VBox outerBox;
    @FXML private VBox explanationBox;
    @FXML private Label lblExplanation;
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane gridPane;
    @FXML private Button btnStartTest;

    @FXML private VBox evaluationBox;
    @FXML private VBox progressBox;
    @FXML private FlowPane questionBox;
    //@FXML private VBox outerBox;

    private XpNotificationView xpNotificationView;

    private AudioRecorder audioRecorder = new AudioRecorder();


    @InjectViewModel SpeakingTestViewModel viewModel;

    public void initialize(){

        Platform.runLater(() ->{
            lblTitle.setText(viewModel.getUserSpeakingTest().getTest().getTitle());
            lblExtra.setText(viewModel.getUserSpeakingTest().getTest().getLanguage().getName() + " • " + viewModel.getUserSpeakingTest().getTest().getLanguageLevel().getName());
            configureBackButton();
            outerBox.setManaged(true);
            outerBox.setVisible(true);

            if(viewModel.questionIndex.get() == 0){
                renderExplanation();
            }else if(viewModel.questionIndex.get() == viewModel.getSpeakingTestQuestions().size() -1 && viewModel.getUserSpeakingTestResponses().getLast().getOverallScore() != 0){
                viewModel.questionIndex.set(viewModel.getSpeakingTestQuestions().size());
                showEvaluationBox();
            }
            else{
                renderCards(1);
            }
            //showEvaluationBox();


            viewModel.questionIndex.addListener((obs, oldVal, newVal)->{
                int multiplier = newVal.intValue() - oldVal.intValue();
                renderCards(multiplier);

//                if(newVal.intValue() >= viewModel.getSpeakingTestQuestions().size()){
//                    showEvaluationBox();
//                }else{
//                    evaluationBox.getChildren().clear();
//                }
            });

            EventBus.getInstance().subscribe(XpEarnedEvent.ANY, e ->{
                xpNotificationView = new XpNotificationView();
                xpNotificationView.showNotification(e.getTransaction());
            });

            lblLesson.textProperty().bind(TranslationManager.get().t(TKey.LESSON));
            btnStartTest.textProperty().bind(TranslationManager.get().t(TKey.STARTTEST));


        });
        scrollPane.heightProperty().addListener((obs, oldVal, newVal)->{
//            System.out.println("height: " + newVal);
//            System.out.println("vmax: " + scrollPane.getVmax());
//            System.out.println("vmin: " + scrollPane.getVmin());
//            if(scrollPane.getVmax() > scrollPane.getVmin()){
//                scrollPane.setFitToHeight(false);
//            }else{
//                scrollPane.setFitToHeight(true);
//                scrollPane.setVvalue(1);
//            }
        });
    }


    private void configureBackButton() {
        imgBack.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "back_arrow_light.svg", 30, 30));

        btnBack.setOnMouseClicked(e -> {
            //RootViewModel.getInstance().getNavigationService().navigateTo(ConversationsView.class);
            viewModel.navigateBack();
        });
    }

    private static QuestionCard lastCard = null;
    @SuppressWarnings({})
    private void renderCards(int multiplier){
        if(viewModel.questionIndex.get() < 0 && multiplier > 0) viewModel.questionIndex.set(0);
        else if(viewModel.questionIndex.get() < viewModel.getSpeakingTestQuestions().size()){
            UserSpeakingTestResponse currentQuestion = viewModel.getUserSpeakingTestResponses().get(viewModel.questionIndex.get());
            QuestionCard card = new QuestionCard(currentQuestion);
            if(currentQuestion.getOverallScore() != 0){
                card.setEvaluation(new Evaluation(currentQuestion.getOverallScore(), 5, currentQuestion.getVocabularyScore(), currentQuestion.getGrammarScore(), currentQuestion.getFeedback()));
            }
            if(lastCard != null){
                AnimationUtils.fadeOutSlideOut(lastCard, Duration.millis(400), -1 * multiplier * 100, 0, ()-> {

                    Platform.runLater(() -> {
                        outerBox.getChildren().remove(lastCard);
                        renderCard(card, currentQuestion, multiplier);
                    });
                });
            }else{
                renderCard(card, currentQuestion, multiplier);
            }
        }
    }

    private void renderCard(QuestionCard card, UserSpeakingTestResponse currentQuestion, int multiplier){
        Platform.runLater(()->{
            outerBox.getChildren().add(card);

            configureScrollPane(card.heightProperty());

            AnimationUtils.fadeInSlideIn(card, Duration.millis(400), multiplier * 100, 0, null);

            card.setOnRecordClicked(recording ->{
                PerformanceTimer.stop("eventRecord");
                if(recording){

                }else{
                    //audioRecorder.stopRecording();

                    PerformanceTimer.start("transcription");
                    Optional<String> transcription = OpenAIWhisperSTT.transcribeAudio("question_answer.wav", currentQuestion.getQuestion().getExpectedResponseLanguageIso(), "transcribe exactly what is said. So don't convert to numerals, if for example 'five' is said, it stays 'five' and doesn't become '5'.");
                    PerformanceTimer.stop("transcription");
                    transcription.ifPresent(s -> Platform.runLater(() -> {
                        card.setQuestionAnswer(s);
                        currentQuestion.setTranscribedText(s);
                    }));


                    //viewModel.evaluateResponse(transcription, viewModel.getSpeakingTestQuestions().get(viewModel.questionIndex.get()));
                }
            });
            card.setOnEvaluateClicked(_ -> {
                if(viewModel.getUserSpeakingTestResponses().get(viewModel.questionIndex.get()).getOverallScore() == 0){
                    CompletableFuture<UserSpeakingTestResponse> completableFuture = CompletableFuture.supplyAsync(() -> {
                        UserSpeakingTestResponse evaluatedResponse = viewModel.evaluateResponse(currentQuestion.getTranscribedText(), currentQuestion);
                        return evaluatedResponse;
                    });
                    completableFuture.thenAccept(e -> {
                        card.setEvaluation(new Evaluation(e.getOverallScore(), e.maxScore, e.getVocabularyScore(), e.getGrammarScore(), e.getFeedback()));

//                        if(viewModel.questionIndex.get() + 1 >= viewModel.getSpeakingTestQuestions().size()){
//                            Platform.runLater(()->{
//                                outerBox.getChildren().remove(card);
//                                showEvaluationBox();
//                            });
//
//                        }
                    });
                }
            });
            card.setOnButtonRightClicked(_ -> {
                lastCard = card;
                int newIndex = 0;
                if(viewModel.questionIndex.get() <= 0){
                    explanationBox.setManaged(false);
                    explanationBox.setVisible(false);
                }
                System.out.println("index: " + viewModel.questionIndex.get());
                System.out.println("size: " + viewModel.getSpeakingTestQuestions().size());

                if(viewModel.questionIndex.get() + 1 < viewModel.getSpeakingTestQuestions().size()){
                    System.out.println("next card");
                    newIndex = viewModel.questionIndex.get() + 1;
                    viewModel.questionIndex.set(newIndex);
                }else if(viewModel.questionIndex.get() + 1 >= viewModel.getSpeakingTestQuestions().size()){
                    newIndex = viewModel.questionIndex.get() + 1;
                    System.out.println("last card");
                    outerBox.getChildren().remove(card);
                    showEvaluationBox();
                    viewModel.questionIndex.set(newIndex);
                }




            });
            card.setOnButtonLeftClicked(_ ->{
                lastCard = card;
                int newIndex = 0;
                if(viewModel.questionIndex.get() - 1 < 0){
                    newIndex = viewModel.questionIndex.get() - 1; // Added - 1 here
                    outerBox.getChildren().remove(card);
                    lastCard = null;
                    renderExplanation();
                    viewModel.questionIndex.set(newIndex); // Added this line here
                }else{
                    newIndex = viewModel.questionIndex.get() - 1;
//                    outerBox.setVisible(true);
//                    outerBox.setManaged(true);
                    evaluationBox.setVisible(false);
                    evaluationBox.setManaged(false);
                    viewModel.questionIndex.set(newIndex);
                }
            });
        });
    }
    private void renderExplanation(){
        explanationBox.setManaged(true);
        explanationBox.setVisible(true);
        //outerBox.setManaged(false);
        //outerBox.setVisible(false);
        AnimationUtils.fadeInSlideIn(explanationBox, Duration.millis(800), 0, -200, null);

        lblExplanation.setText(viewModel.getUserSpeakingTest().getTest().getExplanation());
        explanationBox.setVisible(true);
        explanationBox.setOpacity(1);

        btnStartTest.setOnAction(_->{
            explanationBox.setManaged(false);
            explanationBox.setVisible(false);

            renderCards(1);
        });


        lblExplanation.setWrapText(true);

       configureScrollPane(explanationBox.heightProperty());


    }

    /**
     * Takes in a heightProperty to configure if the scrollPane should be scrollable or not
     * Helps with the central positioning of elements
     * @param p
     */
    private void configureScrollPane(ReadOnlyDoubleProperty p) {
        p.addListener((obs, oldVal, newVal)->{
            System.out.println("height: " + newVal);
            System.out.println("scrollPane height: " + scrollPane.getHeight());
            Platform.runLater(()->{
                if(newVal.doubleValue() > scrollPane.getHeight()){
                    System.out.println("scrolling");
                    scrollPane.setFitToHeight(false);
                    System.out.println("fit to height: " + scrollPane.isFitToHeight());
                    gridPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
                }else{
                    System.out.println("not scrolling");
                    scrollPane.setFitToHeight(true);
                    System.out.println("fit to height: " + scrollPane.isFitToHeight());
                    scrollPane.setVvalue(1);
                    gridPane.setPrefHeight(Region.USE_PREF_SIZE);

                    scrollPane.requestLayout();
                }
            });

//            lblExplanation.setWrapText(true);
//            lblExplanation.setPrefHeight(Region.USE_PREF_SIZE);
//            scrollPane.setMaxHeight(Region.USE_PREF_SIZE);
//            scrollPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
//            scrollPane.setMinViewportHeight(Region.USE_PREF_SIZE);
        });
    }

    private void showEvaluationBox(){
        //evaluationBox.getChildren().clear();
        questionBox.getChildren().clear();
        evaluationBox.setVisible(true);
        evaluationBox.setManaged(true);

        configureScrollPane(evaluationBox.heightProperty());

//        outerBox.setVisible(false);
//        outerBox.setManaged(false);

        questionBox.setVgap(10);
        questionBox.setHgap(10);
        List<UserSpeakingTestResponse> allResponses = viewModel.getUserSpeakingTestResponses();
        int score = 0;
        int maxScore = 5 * allResponses.size();
        for(UserSpeakingTestResponse userSpeakingTestResponse : allResponses){
            score += userSpeakingTestResponse.getOverallScore();

            Label questionLabel = new Label(Integer.toString(userSpeakingTestResponse.getQuestion().getOrderIndex()));
            questionLabel.setPrefWidth(100);
            questionLabel.getStyleClass().addAll("border-radius-3","primary","align-center", "b3", "p-label-m", "btn", "btn-modern");
            Color color = Color.BLUE;
            if(userSpeakingTestResponse.getOverallScore() == 0){
                color = Color.RED;
            }else if(userSpeakingTestResponse.getOverallScore() == 5){
                color = Color.LIGHTGREEN;
            }else{
                color = Color.LIGHTORANGE;
            }
            //questionLabel.setBackground(Background.fill(paint));
            questionLabel.setStyle("-fx-background-color:" + ColorTranslator.textToBackground(color.getValue()) +";-fx-text-fill: " + color.getValue());
            questionBox.getChildren().add(questionLabel);

            questionLabel.setOnMouseClicked(e ->{
               viewModel.questionIndex.set(userSpeakingTestResponse.getQuestionIndex());
               evaluationBox.setVisible(false);
               evaluationBox.setManaged(false);
            });
        }
        CircularProgressBar progressBar = new CircularProgressBar();
        progressBar.animateProgress( (double) score /maxScore, 600);
        progressBar.setSize(150);
        progressBar.setStrokeWidth(15);
        progressBar.setProgressColor(Color.BLUE.getValue());


        questionBox.setMaxWidth(Double.MAX_VALUE);
        //questionBox.getStyleClass().addAll("align-center");

        progressBox.getChildren().clear();
        progressBox.getChildren().add(progressBar);
        if(!(xpNotificationView == null)){
            progressBox.getChildren().add(xpNotificationView);
        }
        //evaluationBox.getChildren().add(progressBar);
        //evaluationBox.getChildren().add(questionPane);
    }
}
