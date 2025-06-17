package com.final_app.views.pages;

import com.final_app.events.PromptEvent;
import com.final_app.events.XpEarnedEvent;
import com.final_app.globals.Color;
import com.final_app.globals.ConversationStatus;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.Sender;
import com.final_app.models.Evaluation;
import com.final_app.models.Message;
import com.final_app.models.UserConversation;
import com.final_app.services.UserService;
import com.final_app.tools.AudioRecorder;
import com.final_app.tools.OpenAIWhisperSTT;
import com.final_app.tools.SVGUtil;
import com.final_app.viewmodels.ChatViewModel;
import com.final_app.viewmodels.RootViewModel;
import com.final_app.views.components.*;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class ChatView implements FxmlView<ChatViewModel> {
    @FXML private StackPane root;
    @FXML private VBox contentBox;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox btnScrollDown;
    @FXML private ImageView imgScrollDown;
    @FXML private Label lblTitle;
    @FXML private Label lblInfo;
    @FXML private VBox scenarioBox;
    @FXML private VBox messageBox;

    @FXML private HBox btnBack;
    @FXML private ImageView imgBack;

    @FXML private ScenarioCard scenarioCard;
    @FXML private PromptBarView promptBar;
    @FXML private CompletedPopup completedPopup;

    @FXML private VBox btnShowEvaluation;
    @FXML private ImageView btnShowEvaluationIcon;

    @InjectViewModel
    private ChatViewModel viewModel;

    private UserService.XpTransaction lastXpTransaction;

    private AudioRecorder audioRecorder;

    private int btnSize = 30;

    public void initialize() {
        Platform.runLater(() -> {
            UserConversation userConversation = viewModel.getUserConversation();
            if (userConversation == null) {
                System.err.println("Error: No conversation set");
                return;
            }

            // Initialize UI elements
            scenarioCard = new ScenarioCard(userConversation.getConversation().getScenario());
            scenarioBox.getChildren().add(scenarioCard);


            // Configure evaluation button
            configureEvaluationButton();

            // Set labels
            lblTitle.setText(userConversation.getConversation().getTitle());
            lblInfo.setText(
                    userConversation.getConversation().getLanguage().getName() +
                            " • " +
                            userConversation.getConversation().getLanguageLevel().getName()
            );

            // Configure back button
            configureBackButton();

            // Set up message update listener
            viewModel.setOnMessagesChanged(e -> {
                System.out.println("refreshing....");
                refreshMessages();
            });

            // Set up prompt bar
            promptBar.addEventHandler(PromptEvent.ANY, event -> {
                System.out.println("Event found, message: " + event.getMessage());
                viewModel.sendMessage(event.getMessage());
            });

            // Set up audio recording
            configureAudioRecording();

            // Set response handler
            viewModel.setOnResponse(response -> {
                refreshMessages();
            });

            // Set XP transaction handler
            viewModel.setOnXpEarned(transaction -> {
                lastXpTransaction = transaction;

                // Fire XP earned event for global notification
                root.fireEvent(new XpEarnedEvent(transaction));
            });

            // Set evaluation handler to show XP earned
            viewModel.setOnEvaluationReceived(evaluation -> {
                Platform.runLater(() -> {
                    EvaluationView evaluationCard = new EvaluationView();
                    evaluationCard.setScore(evaluation);
                    evaluationCard.setProgressColor("#3882e4"); // Blue

                    // Set XP transaction if available
                    if (lastXpTransaction != null) {
                        evaluationCard.setXpTransaction(lastXpTransaction);
                    }

                    System.out.println("adding evaluationcard....");
                    root.getChildren().add(evaluationCard);

                    evaluationCard.setOnButtonNextClick(e -> {
                        RootViewModel.getInstance().getNavigationService().navigateTo(ConversationsView.class);
                    });
                    promptBar.setPromptBarEnabled(false);
                });
            });

            // Configure scroll behavior
            configureScrollBehavior();

            // Start scenario if needed
            if (userConversation.getStatusEnum() == ConversationStatus.NOTSTARTED) {
                viewModel.startScenario();
            } else {
                // Just load existing messages
                refreshMessages();

                // Load completedpopup if conversation has already been done
                if(userConversation.getStatusEnum() == ConversationStatus.COMPLETED){
                    //completedPopup.setVisible(true);
                    promptBar.setPromptBarEnabled(false);
                    btnShowEvaluation.setVisible(true);
                }else{
                    viewModel.setScenario();
                }
            }

            // Set events for completedpopup
            completedPopup.setOnSeeEvaluationClicked(e -> {
                try {
                    Evaluation evaluation = viewModel.retrieveEvaluation();
                    EvaluationView evView = new EvaluationView();
                    evView.setScore(evaluation);
                    Platform.runLater(()->{
                        root.getChildren().add(evView);
                        evView.setOnButtonNextClick(v -> {
                            RootViewModel.getInstance().getNavigationService().navigateTo(ConversationsView.class);
                        });
                    });
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } catch (ExecutionException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });

            completedPopup.setOnRetryButtonClicked(e -> {
                try {
                    viewModel.restartScenario();
                } catch (SQLException | ExecutionException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });
            setUpScrollBtn();


        });
    }

    private void setUpScrollBtn(){
        imgScrollDown.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "arrow_big_down_light.svg", btnSize - 10, btnSize - 10));

        btnScrollDown.setMaxSize(btnSize, btnSize);

        StackPane.setAlignment(btnScrollDown, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(btnScrollDown, new javafx.geometry.Insets(0, 20, 20, 0));
    }


    private void configureBackButton() {
        imgBack.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "back_arrow_light.svg", btnSize, btnSize));

        btnBack.setOnMouseClicked(e -> {
            //RootViewModel.getInstance().getNavigationService().navigateTo(ConversationsView.class);
            viewModel.navigateBack();
        });
    }

    private void configureEvaluationButton() {
        btnShowEvaluation.setPrefSize(btnSize + 10, btnSize + 10);
        btnShowEvaluation.setMinSize(btnSize + 10, btnSize + 10);
        btnShowEvaluation.setMaxSize(btnSize + 10, btnSize + 10);

        StackPane.setAlignment(btnShowEvaluation, Pos.CENTER_RIGHT);
        StackPane.setMargin(btnShowEvaluation, new javafx.geometry.Insets(0, 20, 0, 0));
        btnShowEvaluationIcon.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "evaluation_light.svg", btnSize - 5, btnSize - 5, "#000000"));

        if(viewModel.getUserConversation().getEvaluation() != null){
            btnShowEvaluation.setVisible(true);
        }else{
            btnShowEvaluation.setVisible(false);
        }

        btnShowEvaluation.setOnMouseClicked(_ ->{
            System.out.println("Showing evaluation...");
            Evaluation evaluation = viewModel.getUserConversation().getEvaluation();
            EvaluationView evView = new EvaluationView();
            evView.setScore(evaluation);
            Platform.runLater(()->{
                root.getChildren().add(evView);
                evView.setOnButtonNextClick(v -> {
                    RootViewModel.getInstance().getNavigationService().navigateTo(ConversationsView.class);
                });
            });
        });
    }

    private void configureAudioRecording() {
        promptBar.setOnButtonClick(e -> {
            if (promptBar.recording) {
                File recordFile = new File("input.mp3");
                try {
                    audioRecorder = new AudioRecorder();
                    audioRecorder.startRecording(recordFile);
                } catch (IOException | LineUnavailableException ex) {
                    if(audioRecorder != null){
                        audioRecorder.close();
                    }
                    throw new RuntimeException(ex);
                }
            } else {
                if(audioRecorder != null && audioRecorder.isRecording()){
                    audioRecorder.close();
                    Optional<String> transcription = OpenAIWhisperSTT.transcribeAudio("input.mp3", viewModel.getUserConversation().getConversation().getLanguage().getIso());
                    transcription.ifPresent(s -> viewModel.sendMessage(s));
                }

            }
        });

        this.scrollPane.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.F && !promptBar.isPromptBarFocused() && viewModel.getUserConversation().getStatusEnum() != ConversationStatus.COMPLETED) {
                promptBar.recordingAction();
            }
        });
    }

    private void configureScrollBehavior() {
        scrollPane.vvalueProperty().bind(contentBox.heightProperty());

        btnScrollDown.setOnMouseClicked(e -> {
            if (!scrollPane.vvalueProperty().isBound()) {
                scrollPane.vvalueProperty().bind(contentBox.heightProperty());
            }
        });
        this.contentBox.requestFocus();

        this.contentBox.setOnScroll(e -> {
            //System.out.println("scrolling...");
            if (scrollPane.vvalueProperty().isBound()) {
                scrollPane.vvalueProperty().unbind();
            }
        });
    }

    private void refreshMessages() {
        Platform.runLater(() -> {
            messageBox.getChildren().clear();

            UserConversation userConversation = viewModel.getUserConversation();
            if (userConversation == null || userConversation.getMessages() == null) {
                System.out.println("Userconversation or messages is null");
                return;
            }

            List<Message> messages = userConversation.getMessages().stream().sorted((e, b) -> e.getIndex() - b.getIndex()).toList();
            for (Message message : messages) {
                var newBubble = new TextBubbleView();
                newBubble.setText(message.getText());
                newBubble.getStyleClass().addAll("b1");

                HBox messageLine = new HBox();
                messageLine.setSpacing(20);
                if (Sender.USER.name().equals(message.getSender())) {
                    messageLine.setAlignment(Pos.CENTER_LEFT);
                    newBubble.getStyleClass().addAll("left");

                    messageLine.getChildren().add(newBubble);
                } else {
                    messageLine.setAlignment(Pos.CENTER_RIGHT);



                    VBox translateBtn = new VBox();

                    translateBtn.setPrefSize(30, 30);
                    translateBtn.getStyleClass().addAll("border-radius-1", "border-light", "border-1", "align-center");
                    ImageView icon = new ImageView(SVGUtil.loadSVG("/com/final_app/icons/translation_light.svg", 20, 20));
                    translateBtn.getChildren().add(icon);
                    translateBtn.setVisible(false);
                    translateBtn.setManaged(false);

                    translateBtn.setOnMouseEntered(e ->{
                        icon.setImage(SVGUtil.loadSVG("/com/final_app/icons/translation_dark.svg", 20,20));
                        translateBtn.getStyleClass().add("bg-light");
                    });
                    translateBtn.setOnMouseExited(e -> {
                        icon.setImage(SVGUtil.loadSVG("/com/final_app/icons/translation_light.svg", 20, 20));
                        translateBtn.getStyleClass().remove("bg-light");
                    });
                    translateBtn.setOnMouseClicked(e -> {
                        viewModel.translateMessage(message);
                    });

                    messageLine.setOnMouseEntered(e ->{
                        int index = messages.indexOf(message);
                        if (messages.getLast() != message && messages.get(index + 1).getSenderEnum() != Sender.TRANSLATION){
                            translateBtn.setManaged(true);
                            translateBtn.setVisible(true);
                        }else if(messages.getLast() == message){
                            translateBtn.setManaged(true);
                            translateBtn.setVisible(true);
                        }
                    });
                    messageLine.setOnMouseExited(e -> {
                        translateBtn.setVisible(false);
                        translateBtn.setManaged(false);
                    });

                    messageLine.getChildren().add(newBubble);
                    if(message.getSenderEnum() != Sender.TRANSLATION) {
                        messageLine.getChildren().add(translateBtn);
                        newBubble.getStyleClass().addAll("right");
                    }else{
                        newBubble.getStyleClass().addAll("translation");
                    }
                }

                messageBox.getChildren().add(messageLine);
            }

            if (!scrollPane.vvalueProperty().isBound()) {
                scrollPane.vvalueProperty().bind(contentBox.heightProperty());
            }
        });
    }
}