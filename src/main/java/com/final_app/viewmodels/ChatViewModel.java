package com.final_app.viewmodels;

import com.final_app.globals.AIModels;
import com.final_app.globals.ConversationStatus;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.Sender;
import com.final_app.models.Evaluation;
import com.final_app.models.Message;
import com.final_app.models.Settings;
import com.final_app.models.UserConversation;
import com.final_app.services.ChatGPTService;
import com.final_app.services.*;
import com.final_app.tools.AudioPlayer;
import com.final_app.views.pages.ChatView;
import de.saxsys.mvvmfx.InjectScope;
import com.final_app.scopes.ChatScope;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ChatViewModel extends BaseViewModel {

    private final AppService appService = AppService.getInstance();
    private final ConversationService conversationService = appService.getConversationService();
    private final EvaluationService evaluationService = appService.getEvaluationService();
    private final XpService xpService = new XpService();

    private ObjectProperty<UserConversation> userConversation = new SimpleObjectProperty<>();
    private StringProperty xpEarnedMessage = new SimpleStringProperty("");
    private BooleanProperty hasEarnedXp = new SimpleBooleanProperty(false);


    @InjectScope
    private ChatScope scope;

    private Consumer<Void> onMessagesChanged;
    private Consumer<String> onResponse;
    private Consumer<Evaluation> onEvaluationReceived;
    private Consumer<UserService.XpTransaction> onXpEarned;

    private Settings settings;

    public void setOnMessagesChanged(Consumer<Void> listener) {
        this.onMessagesChanged = listener;
    }

    public void setOnResponse(Consumer<String> onResponse) {
        this.onResponse = onResponse;
    }

    public void setOnEvaluationReceived(Consumer<Evaluation> onEvaluationReceived) {
        this.onEvaluationReceived = onEvaluationReceived;
    }

    public void setOnXpEarned(Consumer<UserService.XpTransaction> onXpEarned) {
        this.onXpEarned = onXpEarned;
    }

    public void setUserConversation(UserConversation userConversation) {
        this.userConversation.set(userConversation);
    }

    public UserConversation getUserConversation() {
        return userConversation.get();
    }

    public ObjectProperty<UserConversation> userConversationProperty() {
        return userConversation;
    }

    public StringProperty xpEarnedMessageProperty() {
        return xpEarnedMessage;
    }

    public BooleanProperty hasEarnedXpProperty() {
        return hasEarnedXp;
    }

    private List<Message> sortMessages() {
        return userConversation.get().getMessages().stream().sorted((a, b) -> a.getIndex() - b.getIndex()).toList();
    }

    public void initialize(){
        try {
            appService.getUserService().getUserSettings(appService.getCurrentUser().getId())
                    .thenAccept(settings -> {
                        this.settings = settings;
                    });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String text) {
        try {
            var sortedMessages = sortMessages();
            int indexToAdd = (!sortMessages().isEmpty()) ? sortedMessages.getLast().getIndex() + 1 : 0;
            // Create and add message to database
            Message message = conversationService.addMessage(
                    indexToAdd,
                    userConversation.get().getId(),
                    text,
                    Sender.USER
            );
            this.userConversation.get().addMessage(message);

            // Notify UI of changes
            if (onMessagesChanged != null) {
                onMessagesChanged.accept(null);
            }

            // Process with ChatGPT and get response
            Thread t = new Thread(() -> {
                try {
                    // Use existing ChatGPTRepository for now
//                    String response = ChatGPTRepository.sendMessage(text,
//                            userConversation.get().getConversation().getAIModel());

                    Optional<String> response = ChatGPTService.sendMessage(text,
                            AIModels.CONVERSATION);

                    if(response.isPresent()){
                        if(response.get().toLowerCase().contains(GlobalVariables.endConversationKey)){
                            System.out.println("End key recognized!");
                            int startIndex = response.get().indexOf(GlobalVariables.endConversationKey);
                            response = Optional.of(response.get().substring(0, startIndex));

                            String finalResponse = response.get();
                            CompletableFuture<Optional<Evaluation>> ev = CompletableFuture.supplyAsync(() -> {
                                UserConversation temp = userConversation.get();
                                temp.addMessage(new Message(temp.getId(), finalResponse, Sender.AI));
                                try {
                                    //JsonObject evObj = ChatGPTRepository.parseEvaluationResponse(ChatGPTRepository.startEvaluation(temp));
                                    Optional<Evaluation> evaluation = evaluationService.evaluateConversation(temp);
                                    System.out.println("Retrieved evaluation");
                                    return evaluation;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            ev.thenAccept(e -> {
                                System.out.println("Sending evaluation to view...");
                                e.ifPresent(evaluation -> {
                                    try {
                                        // Evaluate conversation and get XP transaction
                                        Evaluation savedEvaluation = conversationService.evaluateConversation(
                                                userConversation.get().getId(),
                                                evaluation.getScore(),
                                                evaluation.getMaxScore(),
                                                evaluation.getVocab(),
                                                evaluation.getGrammar(),
                                                evaluation.getFeedback()
                                        );

                                        // Get most recent XP transaction
                                        UserService.XpTransaction transaction =
                                                appService.getUserService().getRecentXpTransaction(
                                                        userConversation.get().getUserId()
                                                );

                                        if (transaction != null) {
                                            // Set XP earned message
                                            Platform.runLater(() -> {
                                                xpEarnedMessage.set("You earned " + transaction.getAmount() + " XP!");
                                                hasEarnedXp.set(true);

                                                // Notify UI of XP earned
                                                if (onXpEarned != null) {
                                                    onXpEarned.accept(transaction);
                                                }
                                            });
                                        }

                                        onEvaluationReceived.accept(savedEvaluation);
                                    } catch (SQLException ex) {
                                        throw new RuntimeException(ex);
                                    } catch (ExecutionException ex) {
                                        throw new RuntimeException(ex);
                                    } catch (InterruptedException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                });


                            });
                        }
                        // Save AI response to database
                        conversationService.addMessage(
                                sortMessages().getLast().getIndex() +1,
                                userConversation.get().getId(),
                                response.get(),
                                Sender.AI
                        );

                        // Convert to speech and play
                        ChatGPTService.convertTextToSpeech(response.get());

                        loadMessages();
                        //this.userConversation.get().addMessage(new Message(this.userConversation.get().getId(), response, Sender.AI));
                        AudioPlayer.playAudio("output.mp3");

                        // Notify UI of changes
                        if (onMessagesChanged != null) {
                            onMessagesChanged.accept(null);
                        }

                        if (onResponse != null) {
                            onResponse.accept(response.get());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.start();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error appropriately
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void startScenario() {
        Thread t = new Thread(() -> {
            try {
                // Set up scenario in ChatGPT
                ChatGPTService.setScenario(userConversation.get());

                // Get initial message from AI
//                String response = ChatGPTRepository.sendMessage("start conversation",
//                        userConversation.get().getConversation().getAIModel());

                Optional<String> response = ChatGPTService.sendMessage("start conversation",
                        AIModels.CONVERSATION);

                if(response.isPresent()){
                    if(response.get().contains(GlobalVariables.endConversationKey)){
                        int startIndex = response.get().indexOf(GlobalVariables.endConversationKey);
                        response = Optional.of(response.get().substring(0, startIndex));
                    }
                    // Save to database
                    conversationService.addMessage(
                            userConversation.get().getMessages().size() + 1,
                            userConversation.get().getId(),
                            response.get(),
                            Sender.AI
                    );


                    // Convert to speech and play
                    ChatGPTService.convertTextToSpeech(response.get());
                    loadMessages();
                    AudioPlayer.playAudio("output.mp3");

                    // Notify UI of changes
                    if (onMessagesChanged != null) {
                        onMessagesChanged.accept(null);
                    }

                    if (onResponse != null) {
                        onResponse.accept(response.get());
                    }

                    // Update conversation status to IN_PROGRESS
                    conversationService.updateConversationStatus(
                            userConversation.get().getId(),
                            ConversationStatus.IN_PROGRESS
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    public void setScenario(){
        ChatGPTService.setScenario(userConversation.get());
    }

    public void loadMessages() {
        try {
            // Refresh messages from database
            UserConversation refreshed = conversationService.getUserConversationById(
                    userConversation.get().getId()
            );
            userConversation.set(refreshed);

            // Notify UI of changes
            if (onMessagesChanged != null) {
                onMessagesChanged.accept(null);
            }
        } catch (SQLException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            // Handle error appropriately
        }
    }

    public Evaluation retrieveEvaluation() throws SQLException, ExecutionException, InterruptedException {
        return conversationService.getEvaluationByUserConversationId(userConversation.get().getId());
    }

    public void restartScenario() throws SQLException, ExecutionException, InterruptedException {
        UserConversation newUserConversation = conversationService.startConversation(appService.getCurrentUser().getId(), userConversation.get().getConversationId());
        RootViewModel.getInstance().getNavigationService().navigateTo(ChatView.class, vm -> {
            vm.setUserConversation(newUserConversation);
        });
    }
    public void translateMessage(Message message){
        try {
            String languageToTranslateTo = settings.getLanguage().getName();
            Optional<String> translation = ChatGPTService.translateMessage(message.getText(), userConversation.get().getConversation().getLanguage().getName(), languageToTranslateTo);
            if(translation.isPresent()){
                Message msgTranslation = new Message(translation.get(), Sender.TRANSLATION);
                int index = message.getIndex();
                msgTranslation.setIndex(index + 1);

                this.userConversation.get().getMessages().forEach(msg -> {
                    if(msg.getIndex() > index) msg.setIndex(msg.getIndex() + 1);
                });
                this.userConversation.get().getMessages().add(Math.min(userConversation.get().getMessages().size(), index + 1), msgTranslation);
                onMessagesChanged.accept(null);
                conversationService.addMessage(msgTranslation.getIndex(), userConversation.get().getId(), translation.get(), Sender.TRANSLATION);
                conversationService.updateConversationMessages(userConversation.get());
                loadMessages();
            }

        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    public void navigateBack(){
        RootViewModel.getInstance().getNavigationService().navigateBack();
    }

    @Override
    public void onNavigatedTo() {
        initialize();
    }

    @Override
    public void onNavigatedFrom() {

    }
}