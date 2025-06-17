package com.final_app.views.pages;

import com.final_app.globals.ConversationStatus;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.*;
import com.final_app.services.AppService;
import com.final_app.services.ConversationService;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import com.final_app.viewmodels.ConversationChainViewModel;
import com.final_app.viewmodels.RootViewModel;
import com.final_app.views.components.ConversationCardLong;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.Initialize;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ConversationChainPage implements FxmlView<ConversationChainViewModel>, Initializable {
    @FXML private HBox btnBack;
    @FXML private ImageView imgBack;
    @FXML private Label lblTitle;
    @FXML private Label lblExtraInfo;

    @FXML private Label lblDescription;
    @FXML private Label lblConversationCompleted;
    @FXML private ProgressBar progressCompleted;

    @FXML private VBox conversationContainer;

    @FXML private Label lblDescriptionTitle;
    @FXML private Label lblProgressTitle;
    @FXML private Label lblSubtitle;


    // Properties
    private StringProperty titleProperty = new SimpleStringProperty();
    private StringProperty extraInfoProperty = new SimpleStringProperty();
    private StringProperty descriptionProperty = new SimpleStringProperty();
    private StringProperty completedProperty = new SimpleStringProperty();
    private DoubleProperty progressProperty = new SimpleDoubleProperty();

    // System text
    private StringProperty descriptionTitleProperty = new SimpleStringProperty();
    private StringProperty subtitleProperty = new SimpleStringProperty();
    private StringProperty progressTitleProperty = new SimpleStringProperty();
    private StringProperty conversationsProperty = new SimpleStringProperty();

    // Actions
    private Consumer<Void> onBack;
    private Consumer<ConversationChainItem> onConversationClicked;

    public void setOnBack(Consumer<Void> onBack){this.onBack = onBack;}
    public void setOnConversationClicked(Consumer<ConversationChainItem> onConversationClicked){this.onConversationClicked = onConversationClicked;}

    // Services
    private final ConversationService conversationService = AppService.getInstance().getConversationService();

    // List
    private List<UserConversationChainItem> userConversationChainItems = new ArrayList<>();


    // Viewmodel
    @InjectViewModel
    private ConversationChainViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            setTranslations();
            setBindings();
            setAll(viewModel.getConversationChain());
            setEvents();
            configureBackButton();
        });

    }
    private void configureBackButton() {
        imgBack.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "back_arrow_light.svg", 30, 30));

        btnBack.setOnMouseClicked(e -> {
            RootViewModel.getInstance().getNavigationService().navigateBack();
            //RootViewModel.getInstance().getNavigationService().navigateTo(ConversationsView.class);
        });
    }


    private void setBindings(){
        lblTitle.textProperty().bind(this.titleProperty);
        lblDescription.textProperty().bind(this.descriptionProperty);
        lblConversationCompleted.textProperty().bind(this.completedProperty);
        lblExtraInfo.textProperty().bind(this.extraInfoProperty);

        lblDescriptionTitle.textProperty().bind(this.descriptionTitleProperty);
        lblProgressTitle.textProperty().bind(this.progressTitleProperty);
        lblSubtitle.textProperty().bind(this.subtitleProperty);
    }

    private void setTranslations(){
        descriptionTitleProperty.bind(TranslationManager.get().t(TKey.CCHDESCRIPTIONTITLE));
        subtitleProperty.bind(TranslationManager.get().t(TKey.CCHSUBTITLE));
        progressTitleProperty.bind(TranslationManager.get().t(TKey.CCHPROGRESS));
    }

    private void setAll(ConversationChain conversationChain){
        this.titleProperty.set(conversationChain.getTitle());
        this.extraInfoProperty.set(conversationChain.getLanguage().getName() + " • " + conversationChain.getLanguageLevel().getName());
        this.descriptionProperty.set(conversationChain.getDescription());

        int endEnableIndex = 0;

        try {
            userConversationChainItems = conversationService.getUserConversationFromConversationChain(conversationChain.getId());

            if(userConversationChainItems.size() > 0){
                long done = userConversationChainItems.stream().filter(e -> e.getUserConversation().getStatusEnum() == ConversationStatus.COMPLETED).count();
                int total = conversationChain.getConversations().size();
                progressCompleted.setProgress((double) done / total);
                this.completedProperty.set(done + "/" + total + " " + TranslationManager.get().t(TKey.CONVERSATIONS).get());

                endEnableIndex = (int)done;
            }else{
                progressCompleted.setProgress(0.0);
                this.completedProperty.set("0/" + conversationChain.getConversations().size() + " " + TranslationManager.get().t(TKey.CONVERSATIONS).get());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        conversationContainer.getChildren().clear();
        int finalEndEnableIndex = endEnableIndex;
        conversationChain.getConversations().forEach(conv -> {
            ConversationCardLong card = new ConversationCardLong(conv);
            UserConversationChainItem userConversationChainItem = userConversationChainItems.stream().filter(e -> e.getUserConversation().getConversationId() == conv.getConversationId()).findFirst().orElse(null);
            if(userConversationChainItem != null) card.setUserConversationChainItem(userConversationChainItem);
            if(conv.getConversationIndex() > finalEndEnableIndex) card.disableCard();
            conversationContainer.getChildren().add(card);


            card.setOnBtnPressed(e -> {
                System.out.println("btn pressed in chain");
                UserConversation userConversation = new UserConversation();
                if(userConversationChainItem == null){
                    try {
                        userConversation = conversationService.startConversationInConversationChain(AppService.getInstance().getCurrentUser().getId(), conversationChain.getId(), conv.getConversationId());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    } catch (ExecutionException ex) {
                        throw new RuntimeException(ex);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }else{
                    userConversation = userConversationChainItem.getUserConversation();
                }

                UserConversation finalUserConversation = userConversation;
                RootViewModel.getInstance().getNavigationService().navigateTo(ChatView.class, vm -> vm.setUserConversation(finalUserConversation));
            });
        });
    }

    private void setEvents(){
        btnBack.setOnMouseClicked(e -> {
            RootViewModel.getInstance().getNavigationService().navigateTo(ConversationsView.class);
        });
    }


}
