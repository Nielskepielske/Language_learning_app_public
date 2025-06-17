package com.final_app.views.components;

import com.final_app.globals.TKey;
import com.final_app.models.ConversationChain;
import com.final_app.models.ConversationChainItem;
import com.final_app.models.UserConversationChainItem;
import com.final_app.tools.TranslationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.function.Consumer;

public class ConversationCardLong extends HBox {
    @FXML private Label lblOrder;
    @FXML private Label lblStatus;
    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblRating;
    @FXML private Button btnAction;

    private ConversationChainItem conversationChainItem;
    private UserConversationChainItem userConversationChainItem;

    private StringProperty orderProperty = new SimpleStringProperty();
    private StringProperty statusProperty = new SimpleStringProperty();
    private StringProperty titleProperty = new SimpleStringProperty();
    private StringProperty descriptionProperty = new SimpleStringProperty();
    private StringProperty ratingProperty = new SimpleStringProperty();
    private StringProperty btnTextProperty = new SimpleStringProperty();

    private Consumer<Void> onBtnPressed;
    public void setOnBtnPressed(Consumer<Void> onBtnPressed){this.onBtnPressed = onBtnPressed;}


    public ConversationCardLong(ConversationChainItem conversationChainItem) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/ConversationCardLong.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            this.conversationChainItem = conversationChainItem;
            setBindings();
            setEvents();
            reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setBindings(){
        lblTitle.textProperty().bind(titleProperty);
        lblDescription.textProperty().bind(descriptionProperty);
        lblOrder.textProperty().bind(orderProperty);
        lblRating.textProperty().bind(ratingProperty);
        lblStatus.textProperty().bind(statusProperty);
        btnAction.textProperty().bind(btnTextProperty);
    }
    public void reload(){
        titleProperty.set(conversationChainItem.getConversation().getTitle());
        descriptionProperty.set(conversationChainItem.getConversation().getDescription());
        orderProperty.set(Integer.toString(conversationChainItem.getConversationIndex()));

        String temp = "";
        for(int i = 0; i < conversationChainItem.getConversation().getLanguageLevel().getValue(); i++){
            temp += "★";
        }
        ratingProperty.set(temp);

        btnTextProperty.unbind();
        if(userConversationChainItem != null){
            statusProperty.set(userConversationChainItem.getUserConversation().getStatusEnum().getText());
            switch (userConversationChainItem.getUserConversation().getStatusEnum()){
                case NOTSTARTED -> {
                    btnTextProperty.bind(TranslationManager.get().t(TKey.START));
                    //btnTextProperty.set("Start");
                    break;
                }
                case IN_PROGRESS -> {
                    btnTextProperty.bind(TranslationManager.get().t(TKey.CONTINUE));
                    //btnTextProperty.set("Continue");
                    break;
                }
                case COMPLETED -> {
                    //btnAction.setDisable(true);
                    btnTextProperty.bind(TranslationManager.get().t(TKey.REVIEW));
                    //btnTextProperty.set("Review");
                    break;
                }
                default -> {
                    btnTextProperty.bind(TranslationManager.get().t(TKey.START));
                    //btnTextProperty.set("Start");
                }
            }
        }else{
            btnTextProperty.bind(TranslationManager.get().t(TKey.START));
            //btnTextProperty.set("Start");
        }
    }
    public void setEvents(){
        btnAction.setOnAction(e -> {
            onBtnPressed.accept(null);
        });
    }
    public void disableCard(){
        btnAction.setDisable(true);
    }

    public void setUserConversationChainItem(UserConversationChainItem userConversationChainItem){
        this.userConversationChainItem = userConversationChainItem;
        reload();
    }
    public UserConversationChainItem getUserConversationChainItem() {
        return userConversationChainItem;
    }
}
