package com.final_app.views.components;

import com.final_app.globals.GlobalVariables;
import com.final_app.models.Evaluation;
import com.final_app.models.SpeakingTestQuestion;
import com.final_app.models.UserSpeakingTestResponse;
import com.final_app.tools.AudioRecorder;
import com.final_app.tools.PerformanceTimer;
import com.final_app.tools.SVGUtil;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.sound.sampled.LineUnavailableException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class QuestionCard extends HBox {
    @FXML private VBox outerContainer;
    @FXML private Label lblQuestion;
    @FXML private Label lblUserAnswer;
    @FXML private ImageView imageIcon;
    @FXML private ImageView btnLeft;
    @FXML private ImageView btnRight;
    @FXML private VBox btnRecord;
    @FXML private Label lblQuestionNumber;

    @FXML private Button btnEvaluate;

    @FXML private VBox evaluationContainer;

    private EvaluationView evaluationView = new EvaluationView();

    private StringProperty questionTextProperty = new SimpleStringProperty();
    private StringProperty userAnswerProperty = new SimpleStringProperty();
    private StringProperty iconPathProperty = new SimpleStringProperty(GlobalVariables.ICONS + "microphone_light.svg");

    private BooleanProperty isEvaluated = new SimpleBooleanProperty(false);
    private BooleanProperty isCorrect = new SimpleBooleanProperty(false);

    private AudioRecorder audioRecorder;

    private Consumer<Boolean> onRecordClicked;
    public void setOnRecordClicked(Consumer<Boolean> onRecordClicked){this.onRecordClicked = onRecordClicked;}
    private Consumer<Void> onButtonRightClicked;
    public void setOnButtonRightClicked(Consumer<Void> onButtonRightClicked){this.onButtonRightClicked = onButtonRightClicked;}
    private Consumer<Void> onButtonLeftClicked;
    public void setOnButtonLeftClicked(Consumer<Void> onButtonLeftClicked){this.onButtonLeftClicked = onButtonLeftClicked;}
    private Consumer<Void> onEvaluateClicked;
    public void setOnEvaluateClicked(Consumer<Void> onEvaluateClicked){this.onEvaluateClicked = onEvaluateClicked;}

    private BooleanProperty recording = new SimpleBooleanProperty(false);


    public QuestionCard() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/QuestionCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
            setUpAllIconButtons();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public QuestionCard(UserSpeakingTestResponse userSpeakingTestResponse) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/QuestionCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
            setAll(userSpeakingTestResponse);
            setUpAllIconButtons();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void setAll(UserSpeakingTestResponse userSpeakingTestResponse){
        questionTextProperty.set(userSpeakingTestResponse.getQuestion().getQuestionText());
        userAnswerProperty.set(userSpeakingTestResponse.getTranscribedText());
        lblQuestionNumber.setText(Integer.toString(userSpeakingTestResponse.getQuestionIndex()));

        if(userAnswerProperty.get() != null && evaluationView.getEvaluation() == null){
            btnEvaluate.setDisable(false);
        }else{
            btnEvaluate.setDisable(true);
        }

        isEvaluated.addListener((obs, oldVal, newVal)->{
            if(newVal){
                btnEvaluate.setDisable(true);
            }else{
                btnEvaluate.setDisable(false);
            }
        });
    }

    private void setUpAllIconButtons(){
        int size1 = 30;
        btnLeft.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "arrow_left_light.svg", size1, size1));
        btnRight.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "arrow_right_light.svg", size1, size1));

        btnRecord.setOnMouseEntered(_ ->{
            if(!recording.get()){
                Platform.runLater(()->{
                    btnRecord.getStyleClass().remove("bg-dark");
                    btnRecord.getStyleClass().add("bg-light");
                    btnRecord.setOpacity(0.8);

                    imageIcon.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "microphone_dark.svg", 40, 40));
                });

            }

        });
        btnRecord.setOnMouseExited(_ ->{
            if(!recording.get()){
                Platform.runLater(()->{
                    btnRecord.getStyleClass().remove("bg-light");
                    btnRecord.getStyleClass().add("bg-dark");
                    btnRecord.setOpacity(1);

                    imageIcon.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "microphone_light.svg", 40, 40, "#ffffff"));
                });
            }
        });
        btnRecord.setOnMouseClicked(_ ->{
            recording.set(!recording.get());
        });

        recording.addListener((obs, oldVal, newVal)->{
            if(recording.get()){
                btnRecord.getStyleClass().remove("bg-dark");
                btnRecord.getStyleClass().add("bg-light");
                btnRecord.setOpacity(1);
                imageIcon.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "microphone_dark.svg", 40, 40));
                File recordFile = new File("question_answer.wav");
                try {
                    audioRecorder = new AudioRecorder();
                    audioRecorder.startRecording(recordFile);
                } catch (IOException | LineUnavailableException e) {
                    if(audioRecorder != null){
                        audioRecorder.close();
                    }
                    throw new RuntimeException(e);
                }
            }else{
                btnRecord.getStyleClass().remove("bg-light");
                if(audioRecorder != null && audioRecorder.isRecording()){
                    audioRecorder.stopRecording();
                }
//                btnRecord.getStyleClass().add("bg-dark");
//                btnRecord.setOpacity(1);
//                imageIcon.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "microphone_light.svg", 40, 40));
            }

            Thread thread = new Thread(()->{
                PerformanceTimer.start("eventRecord");
                onRecordClicked.accept(recording.get());
            });
            thread.start();
        });

        btnEvaluate.setOnAction(e->{
            btnEvaluate.setDisable(true);
            onEvaluateClicked.accept(null);
        });

        btnLeft.setOnMouseClicked(_ ->{
            onButtonLeftClicked.accept(null);
        });
        btnRight.setOnMouseClicked(_ ->{
            if(isEvaluated.get()){
                onButtonRightClicked.accept(null);
            }
        });
    }

    private void setBindings(){
        Platform.runLater(()->{
            this.requestFocus();
            lblQuestion.textProperty().bind(questionTextProperty);
            lblUserAnswer.textProperty().bind(userAnswerProperty);

            imageIcon.setImage(SVGUtil.loadSVG(iconPathProperty.get(), 40, 40, "#ffffff"));


            // Monitor when the parent property changes
            this.parentProperty().addListener((obs, oldParent, newParent) -> {
                //System.out.println("parent changed");
                if (newParent != null) {
                    // Now we have a parent, we can safely get the scene
                    Scene scene = newParent.getScene();
                    if (scene != null) {
                        scene.setOnKeyReleased(keyEvent -> {
                            //System.out.println("key pressed");
                            if (keyEvent.getCode() == KeyCode.F) {
                                recording.set(!recording.get());
                                if (!recording.get()) {
                                    imageIcon.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "microphone_light.svg", 40, 40, "#ffffff"));
                                }
                            }
                            if(keyEvent.getCode() == KeyCode.LEFT){
                                onButtonLeftClicked.accept(null);
                            }
                            if(keyEvent.getCode() == KeyCode.RIGHT && userAnswerProperty.get() != null && isEvaluated.get()){
                                System.out.println(isEvaluated.get());
                                onButtonRightClicked.accept(null);
                            }
                        });
                    }
                }
            });

        });

    }

    public void setQuestionAnswer(String answer){
        userAnswerProperty.set(answer);
        if(!isEvaluated.get()){
            btnEvaluate.setDisable(false);
        }
    }

    public void setEvaluation(Evaluation evaluation){
        Platform.runLater(()->{
            System.out.println("evaluation set");
            evaluation.setMaxPointsPerCriteria(5);
            evaluationView.setScore(evaluation);
            evaluationView.disableNextButton();
            evaluationContainer.getChildren().add(evaluationView);
            isEvaluated.set(true);
        });
    }
}
