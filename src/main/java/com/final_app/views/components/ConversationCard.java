package com.final_app.views.components;

import com.final_app.globals.ConversationStatus;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.Conversation;
import com.final_app.models.UserConversation;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import com.google.cloud.translate.Translation;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;

public class ConversationCard extends VBox {
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

    private StringProperty titleProperty = new SimpleStringProperty();
    private StringProperty languageProperty = new SimpleStringProperty();
    private StringProperty descriptionProperty = new SimpleStringProperty();
    private StringProperty statusProperty = new SimpleStringProperty();
    private StringProperty difficultyProperty = new SimpleStringProperty();

    private Consumer<Object> onButtonClick;
    private UserConversation userConversation;
    private Conversation conversation;

    private int iconSize = 60;

    public ConversationCard() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/ConversationCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConversationCard(UserConversation userConversation) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/ConversationCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
            setUserConversation(userConversation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConversationCard(Conversation conversation) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/ConversationCard.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            setBindings();
            setAll(conversation);
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

        actionButton.setOnAction(e -> {
            if (onButtonClick != null) {
                if (userConversation != null) {
                    onButtonClick.accept(userConversation);
                } else if (conversation != null) {
                    onButtonClick.accept(conversation);
                } else {
                    onButtonClick.accept(null);
                }
            }
        });
    }

    public void setUserConversation(UserConversation userConversation) {
        this.userConversation = userConversation;
        this.conversation = null;

        Conversation conv = userConversation.getConversation();

        // Set conversation info
        this.titleProperty.set(conv.getTitle());
        this.descriptionProperty.set(conv.getDescription());
        //this.statusProperty.set(userConversation.getStatusEnum().getText());
        this.languageProperty.set(conv.getLanguage().getName());
        this.circle.setStyle("-fx-background-color: " + conv.getLanguage().getColor());

        // Set status icon and button text
        actionButton.textProperty().unbind();
        this.statusProperty.unbind();
        if (ConversationStatus.COMPLETED == userConversation.getStatusEnum()) {
            statusIcon.setText("✓");
            actionButton.textProperty().bind(TranslationManager.get().t(TKey.REVIEW));
            statusProperty.bind(TranslationManager.get().t(TKey.COMPLETED));
            //actionButton.setText("Review");
        } else if (ConversationStatus.IN_PROGRESS == userConversation.getStatusEnum()) {
            statusIcon.setText("⟳");
            actionButton.textProperty().bind(TranslationManager.get().t(TKey.CONTINUE));
            statusProperty.bind(TranslationManager.get().t(TKey.INPROGRESS));
            //actionButton.setText("Continue");
        } else {
            statusIcon.setText("");
            actionButton.textProperty().bind(TranslationManager.get().t(TKey.START));
            statusProperty.bind(TranslationManager.get().t(TKey.NOTSTARTED));
            //actionButton.setText("Start");
        }

        // Set icon
        icon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/conversation.svg", iconSize, iconSize));

        // Set difficulty stars
        String tempDifString = "";
        for (int i = 0; i < conv.getLanguageLevel().getValue(); i++) {
            tempDifString += "★";
        }
        this.difficultyProperty.set(tempDifString);
    }

    public void setAll(Conversation conversation) {
        this.conversation = conversation;
        this.userConversation = null;

        this.titleProperty.set(conversation.getTitle());
        this.descriptionProperty.set(conversation.getDescription());
        //this.statusProperty.set(ConversationStatus.NOTSTARTED.getText());
        this.statusProperty.unbind();
        this.statusProperty.bind(TranslationManager.get().t(TKey.NOTSTARTED));

        this.languageProperty.set(conversation.getLanguage().getName());
        this.circle.setStyle("-fx-background-color: " + conversation.getLanguage().getColor());

        statusIcon.setText("");
        actionButton.textProperty().unbind();
        actionButton.textProperty().bind(TranslationManager.get().t(TKey.START));
        //actionButton.setText("Start");


        icon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/conversation.svg", iconSize, iconSize));

        String tempDifString = "";
        for (int i = 0; i < conversation.getLanguageLevel().getValue(); i++) {
            tempDifString += "★";
        }
        this.difficultyProperty.set(tempDifString);
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
    public UserConversation getUserConversation() {
        return userConversation;
    }

    /**
     * Get the Conversation associated with this card, if any
     */
    public Conversation getConversation() {
        return conversation;
    }
}