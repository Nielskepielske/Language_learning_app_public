package com.final_app.views.components;

import com.final_app.globals.ConversationStatus;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.Conversation;
import com.final_app.models.ConversationChain;
import com.final_app.models.UserConversation;
import com.final_app.models.UserConversationChainItem;
import com.final_app.services.AppService;
import com.final_app.services.ConversationService;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ConversationChainCard extends VBox {
    @FXML private Label lblTitle;
    @FXML private Label lblAmountCompleted;
    @FXML private Label lblDescription;
    @FXML private Label lblStatus;
    @FXML private Button actionButton;
    @FXML private ImageView icon;
    @FXML private HBox header;

    @FXML private Label circle;
    @FXML private Label lblLanguage;
    @FXML private Label lblDifficulty;
    @FXML private Label statusIcon;

    private StringProperty titleProperty = new SimpleStringProperty();
    private StringProperty languageProperty = new SimpleStringProperty();
    private StringProperty amountCompletedProperty = new SimpleStringProperty();
    private StringProperty descriptionProperty = new SimpleStringProperty();
    private StringProperty statusProperty = new SimpleStringProperty();
    private StringProperty difficultyProperty = new SimpleStringProperty();

    private Consumer<Object> onButtonClick;
    private ConversationChain conversationChain;
    private List<UserConversation> userConversations;

    private int iconSize = 60;

    private final ConversationService conversationService = AppService.getInstance().getConversationService();

    public ConversationChainCard() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/ConversationChainCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
            setEvents();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConversationChainCard(ConversationChain conversationChain) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/ConversationChainCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
            setAll(conversationChain);
            setEvents();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setBindings() {
        lblTitle.textProperty().bind(this.titleProperty);
        lblDescription.textProperty().bind(this.descriptionProperty);
        lblStatus.textProperty().bind(this.statusProperty);
        lblLanguage.textProperty().bind(this.languageProperty);
        lblDifficulty.textProperty().bind(this.difficultyProperty);
        lblAmountCompleted.textProperty().bind(this.amountCompletedProperty);

//        actionButton.setOnAction(e -> {
//            if (onButtonClick != null) {
//                if (userConversation != null) {
//                    onButtonClick.accept(userConversation);
//                } else if (conversation != null) {
//                    onButtonClick.accept(conversation);
//                } else {
//                    onButtonClick.accept(null);
//                }
//            }
//        });
    }

    public void setUserConversationChain() {
        // Get all the user conversation of a user
        // Connect them to this specific conversationChain
        // Count all unique conversations
        try {
            List<UserConversationChainItem> userConversationChainItems = conversationService.getUserConversationFromConversationChain(this.conversationChain.getId());

            Map<String, List<UserConversationChainItem>> uniqueUserConversationChainItems = FXCollections.observableHashMap();
            for(UserConversationChainItem userConversationChainItem : userConversationChainItems){
                String id = userConversationChainItem.getUserConversation().getConversationId();
                if(!uniqueUserConversationChainItems.containsKey(id)){
                    uniqueUserConversationChainItems.put(id, new ArrayList<UserConversationChainItem>());
                }
                uniqueUserConversationChainItems.get(id).add(userConversationChainItem);
            }
            int completed = 0;
            for(List<UserConversationChainItem> userConversationChainItems1 : uniqueUserConversationChainItems.values()){
                for(UserConversationChainItem userConversationChainItem : userConversationChainItems1){
                    if(userConversationChainItem.getUserConversation().getStatusEnum() == ConversationStatus.COMPLETED) completed++;
                }
            }
            if(userConversationChainItems.size() != 0) {
                this.amountCompletedProperty.set(completed + "/" + this.conversationChain.getConversations().size());
                actionButton.textProperty().unbind();
                this.statusProperty.unbind();
                if(completed != this.conversationChain.getConversations().size()){
                    actionButton.textProperty().bind(TranslationManager.get().t(TKey.CONTINUE));
                    statusProperty.bind(TranslationManager.get().t(TKey.INPROGRESS));
                    //actionButton.setText("Continue");
                }else{
                    actionButton.textProperty().bind(TranslationManager.get().t(TKey.REVIEW));
                    statusProperty.bind(TranslationManager.get().t(TKey.COMPLETED));
                    //actionButton.setText("Review");
                }
            }else{
                lblAmountCompleted.setVisible(false);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAll(ConversationChain conversationChain) {
        this.conversationChain = conversationChain;

        this.titleProperty.set(conversationChain.getTitle());
        this.descriptionProperty.set(conversationChain.getDescription());
        this.statusProperty.unbind();
        this.statusProperty.bind(TranslationManager.get().t(TKey.NOTSTARTED));
        //this.statusProperty.set(ConversationStatus.NOTSTARTED.getText());
        this.languageProperty.set(conversationChain.getLanguage().getName());
        this.circle.setStyle("-fx-background-color: " + conversationChain.getLanguage().getColor());

        statusIcon.setText("");
        actionButton.textProperty().unbind();
        actionButton.textProperty().bind(TranslationManager.get().t(TKey.START));
        //actionButton.setText("Start");

        icon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/conversation_chain_light.svg", iconSize * 2, iconSize));

        String tempDifString = "";
//        for (int i = 0; i < conversation.getLanguageLevel().getValue(); i++) {
//            tempDifString += "★";
//        }
        this.difficultyProperty.set(conversationChain.getLanguageLevel().getName());
        setUserConversationChain();
    }
    public void setEvents(){
        actionButton.setOnMouseClicked(e -> {
            onButtonClick.accept(null);
        });
    }

    public void setTitle(String title) {
        this.titleProperty.set(title);
    }

    public void setDescription(String description) {
        this.descriptionProperty.set(description);
    }

    /**
     * Set a button click handler that can handle any type of object.
     * The handler will receive either a UserConversation, Conversation, or null
     * depending on what was set on this card.
     *
     * @param listener The consumer to handle the button click
     */
    public void setOnButtonClick(Consumer<Object> listener) {
        this.onButtonClick = listener;
    }

    /**
     * Get the UserConversation associated with this card, if any
     */
    public List<UserConversation> getUserConversations() {
        return userConversations;
    }

}