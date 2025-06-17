package com.final_app.views.pages;

import com.final_app.converters.LanguageConverter;
import com.final_app.globals.ConversationStatus;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.*;
import com.final_app.tools.AnimationUtils;
import com.final_app.tools.ListFilter;
import com.final_app.tools.SVGUtil;
import com.final_app.viewmodels.ConversationsViewModel;
import com.final_app.viewmodels.RootViewModel;
import com.final_app.views.components.ConversationCard;
import com.final_app.views.components.ConversationChainCard;
import com.final_app.views.components.CustomGroupWindow;
import com.final_app.views.components.custom.general.EmptyPlace;
import com.final_app.views.components.custom.general.FilterWindow;
import com.final_app.views.components.custom.general.SearchBar;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ConversationsView implements FxmlView<ConversationsViewModel> {
    @FXML private HBox btnNewConversation;

    @FXML private VBox availableBox;
    @FXML private VBox availableConversationsBox;
    @FXML private FlowPane availableSingleConversationsCards;
    @FXML private VBox availableConversationChainBox;
    @FXML private FlowPane availableConversationChainCards;

    @FXML private VBox yourCardBox;
    @FXML private FlowPane cards;
    @FXML private VBox userConversationChainBox;
    @FXML private FlowPane userConversationChainCards;

    @FXML private CustomGroupWindow<UserConversation> conversationGroupWindow;

    @FXML private ScrollPane scrollPane;
    @FXML private StackPane parent;

    @FXML private SearchBar searchBar;

    @FXML private VBox btnFilter;
    @FXML private ImageView imgFilter;
    @FXML private FilterWindow filterWindow;

    @InjectViewModel
    private ConversationsViewModel viewModel;

    private BooleanProperty showAvailableCards = new SimpleBooleanProperty(false);

    private List<UserConversation> selectedCluster = new ArrayList<>();

    private List<Language> selectedLanguages = new ArrayList<>();

    // System text elements
    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblConversationChains;
    @FXML private Label lblConversations;
    @FXML private Label lblAConversationChains;
    @FXML private Label lblAConversations;
    @FXML private Label lblNewConversation;

    public void initialize() {
        System.out.println("ConversationsView initialized");
        showAvailableCards.addListener((obs, oldVal, newVal)->{
            animateLists();
        });

        setBindings();
        setUpEmptyComponents();
        loadConversationLists();
        loadUserConversationLists();
        bindUIText();


        viewModel.getUserConversations().addListener((ListChangeListener<? super UserConversation>) e -> {
            System.out.println("UserConversations changed");
            loadUserConversationLists();
            setUpEmptyComponents();
        });
        viewModel.getGroupedUserConversations().addListener((MapChangeListener<? super String, ? super List<UserConversation>>) e -> {
            if(e.wasAdded()){
                if(conversationGroupWindow.isVisible() && !viewModel.getGroupedUserConversations().isEmpty() && selectedCluster != null){
                    System.out.println("GroupedUserConversations changed");
                    var firstSelect = selectedCluster.getFirst();
//                    if(viewModel.getGroupedUserConversations().get(firstSelect.getConversationId()) != null && selectedCluster.size() > 1){
//                        System.out.println("Updating grouped window");
//                        selectedCluster = viewModel.getGroupedUserConversations().get(firstSelect.getConversationId());
//                        conversationGroupWindow.initializeRow(selectedCluster);
//                    }
                    if(viewModel.getGroupedUserConversations().containsKey(firstSelect.getConversationId())) selectedCluster = viewModel.getGroupedUserConversations().get(firstSelect.getConversationId());
                    if(selectedCluster != null){
                        conversationGroupWindow.initializeRow(selectedCluster);

                        conversationGroupWindow.setVisible(true);
                    }

                }
                loadUserConversationLists();
                setUpEmptyComponents();
            }

        });
        viewModel.getAvailableConversations().addListener((ListChangeListener<? super Conversation>) e -> {
            loadConversationLists();
            setUpEmptyComponents();
        });
        viewModel.getAvailableConversationChains().addListener((ListChangeListener<? super ConversationChain>) e -> {
            loadConversationLists();
            setUpEmptyComponents();
        });





    }

    private void setBindings() {
        imgFilter.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "filter_light.svg", 20, 20));
        filterWindow.setLanguageKeys(TKey.FWLTITLE, TKey.FWLDESCRIPTION);
        btnFilter.setOnMouseClicked(e -> {
            if(!filterWindow.isVisible()){
                filterWindow.setItems(viewModel.getLanguages());
            }
            filterWindow.setVisible(!filterWindow.isVisible());
        });

        filterWindow.setOnCancel(_ -> {
            filterWindow.setVisible(false);
        });
        filterWindow.setOnApply(lang -> {
            selectedLanguages = lang;
            filterWindow.setVisible(false);
            loadConversationLists();
            loadUserConversationLists();
        });
        // New conversation button
        btnNewConversation.setOnMouseClicked(e -> {
            // Show dialog or navigate to a "new conversation" page
            showAvailableCards.set(!showAvailableCards.get());
        });

        conversationGroupWindow.setOnBackButtonClicked(e -> {
            conversationGroupWindow.setVisible(false);
        });

        searchBar.setOnSearch(text -> {
            loadConversationLists();
            loadUserConversationLists();
        });

        viewModel.getCurrentSettings().addListener((obs, oldVal, newVal)->{
            System.out.println("CurrentSettings changed: " + newVal.getSelectedLanguages());
            loadConversationLists();
            loadUserConversationLists();
        });
    }



    private final EmptyPlace availableEmpty = new EmptyPlace("No available conversations...");
    private final EmptyPlace availableEmptyChain = new EmptyPlace("No available conversation chains...");
    private final EmptyPlace noUserConversations = new EmptyPlace("No user conversations found...");
    private final EmptyPlace noUserConversationsChain = new EmptyPlace("No user conversation chains found...");
    private void setUpEmptyComponents(){
        if(!availableConversationsBox.getChildren().contains(availableEmpty)) availableConversationsBox.getChildren().add(availableEmpty);
        if(!yourCardBox.getChildren().contains(noUserConversations)) yourCardBox.getChildren().add(noUserConversations);
        if(!availableConversationChainBox.getChildren().contains(availableEmptyChain)) availableConversationChainBox.getChildren().add(availableEmptyChain);
        if(!userConversationChainBox.getChildren().contains(noUserConversationsChain)) userConversationChainBox.getChildren().add(noUserConversationsChain);

        noUserConversationsChain.setMaxWidth(userConversationChainCards.getMaxWidth());
        noUserConversationsChain.setPrefWidth(userConversationChainCards.getMaxWidth());

        if(viewModel.getUserConversations().isEmpty()){
            noUserConversations.setVisible(true);
            noUserConversations.setManaged(true);
        }else{
            noUserConversations.setVisible(false);
            noUserConversations.setManaged(false);
        }
        if(viewModel.getAvailableConversations().isEmpty()){
            availableEmpty.setVisible(true);
            availableEmpty.setManaged(true);
        }else{
            availableEmpty.setVisible(false);
            availableEmpty.setManaged(false);
        }
        if(viewModel.getAvailableConversationChains().isEmpty()){
            availableEmptyChain.setVisible(true);
            availableEmptyChain.setManaged(true);
        }else{
            availableEmptyChain.setVisible(false);
            availableEmptyChain.setManaged(false);
        }
        if(viewModel.getUserConversationChains().isEmpty()){
            noUserConversationsChain.setVisible(true);
            noUserConversationsChain.setManaged(true);
        }else{
            noUserConversationsChain.setVisible(false);
            noUserConversationsChain.setManaged(false);
        }
    }

    private void animateLists(){
        if(showAvailableCards.get()){
            AnimationUtils.fadeOutSlideOut(yourCardBox, Duration.millis(300), 200, 0, ()->{
                yourCardBox.setManaged(false);
                yourCardBox.setVisible(false);
                availableBox.setManaged(true);
                availableBox.setVisible(true);
                AnimationUtils.fadeInSlideIn(availableBox, Duration.millis(300), 200, 0, null);
            });
        }else{
            AnimationUtils.fadeOutSlideOut(availableBox, Duration.millis(300), 200, 0, ()->{
                availableBox.setManaged(false);
                availableBox.setVisible(false);
                yourCardBox.setManaged(true);
                yourCardBox.setVisible(true);
                AnimationUtils.fadeInSlideIn(yourCardBox, Duration.millis(300), 200, 0, null);
            });
        }
    }

    public void bindUIText(){
        Platform.runLater(()->{
            lblTitle.textProperty().bind(viewModel.lblTitleProperty);
            lblDescription.textProperty().bind(viewModel.lblDescriptionProperty);
            lblConversations.textProperty().bind(viewModel.lblTitleProperty);
            lblConversationChains.textProperty().bind(viewModel.lblConversationChains);
            lblNewConversation.textProperty().bind(viewModel.txtNewConversation);
            lblAConversations.textProperty().bind(viewModel.lblAConversations);
            lblAConversationChains.textProperty().bind(viewModel.lblAConversationChains);
        });
    }
    public void loadConversationLists(){
        Platform.runLater(()->{
            // Clear the cards
            availableSingleConversationsCards.getChildren().clear();
            availableConversationChainCards.getChildren().clear();
            // Add available conversations
            //List<Conversation> snapshot = (searchBar.textProperty.get().isEmpty() || searchBar.getSearchFieldText().isBlank()) ? new ArrayList<>(viewModel.getAvailableConversations()) : viewModel.getAvailableConversations().stream().filter(conversation -> conversation.getTitle().toLowerCase().contains(searchBar.textProperty.get().toLowerCase())).toList();
            List<Conversation> snapshot = (List<Conversation>) filteredList(viewModel.getAvailableConversations());
            if(snapshot!=null){
                snapshot.forEach(conversation -> {
                    ConversationCard card = new ConversationCard(conversation);
                    availableSingleConversationsCards.getChildren().add(card);

                    card.setOnButtonClick(obj -> {
                        showAvailableCards.set(false);
                        if (obj instanceof Conversation) {
                            viewModel.startNewConversation((Conversation) obj);
                        }
                    });
                });
            }

            AnimationUtils.fadeInSlideIn(availableSingleConversationsCards, Duration.millis(300), 200, 0, null);

            assert snapshot != null;
            if(snapshot.isEmpty()){
                availableEmpty.setVisible(true);
                availableEmpty.setManaged(true);
            }else{
                availableEmpty.setVisible(false);
                availableEmpty.setManaged(false);
            }

            // Add available conversationChains
            filteredList(viewModel.getAvailableConversationChains()).forEach(item -> {
                ConversationChain conversationChain = (ConversationChain)item;
                ConversationChainCard card = new ConversationChainCard(conversationChain);
                availableConversationChainCards.getChildren().add(card);

                card.setOnButtonClick(e -> {
                    //ConversationChainPage chainPage = new ConversationChainPage(conversationChain);
                    //parent.getChildren().add(chainPage);

                    //chainPage.setOnBack(onBack -> {
                    //   parent.getChildren().remove(chainPage);
                    //});
                    RootViewModel.getInstance().getNavigationService().navigateTo(ConversationChainPage.class, vm ->{
                        vm.setConversationChain(conversationChain);
                    });
                });
            });
            if(viewModel.getAvailableConversationChains().isEmpty()){
                availableEmptyChain.setVisible(true);
                availableEmptyChain.setManaged(true);
            }else{
                availableEmptyChain.setVisible(false);
                availableEmptyChain.setManaged(false);
            }
        });

    }

    public void loadUserConversationLists(){
        Platform.runLater(()->{
            // Clear the cards
            cards.getChildren().clear();
            userConversationChainCards.getChildren().clear();
            viewModel.getGroupedUserConversations().forEach((key, list) -> {
                if(filteredList(list) == null || filteredList(list).isEmpty()) return;
                ConversationCard card = new ConversationCard(list.getFirst());
                cards.getChildren().add(card);
                card.setOnButtonClick(obj->{
                    if(obj instanceof UserConversation){
                        if(list.size() > 1 || list.stream().map(us -> us.getStatusEnum()).toList().contains(ConversationStatus.COMPLETED)){
                            conversationGroupWindow.initializeRow(list);
                            selectedCluster = list;
                            conversationGroupWindow.setVisible(true);
                            conversationGroupWindow.setOnCreateButtonClicked(us -> {
                                viewModel.startNewConversation(us.getConversation());
                            });
                        }else{
                            viewModel.navigateToChatView((UserConversation) obj);
                        }
                    }
                });
            });
            // Add all userconversationChains
            filteredList(viewModel.getUserConversationChains()).forEach(item -> {
                ConversationChain userConversationChain = (ConversationChain)item;
                ConversationChainCard card = new ConversationChainCard(userConversationChain);
                userConversationChainCards.getChildren().add(card);

                card.setOnButtonClick(e -> {
                    RootViewModel.getInstance().getNavigationService().navigateTo(ConversationChainPage.class, vm ->{
                        vm.setConversationChain(userConversationChain);
                    });
                });
            });
        });
    }

    private List<?> filteredList(List<?> list){
        if(list.isEmpty()) return list;
        if(list.getFirst() instanceof Conversation){
            Predicate<Conversation> conversationPredicate = item ->
                    searchBar.getSearchFieldText().isEmpty()
                    || item.getTitle().toLowerCase().contains(searchBar.textProperty.get().toLowerCase());
            Predicate<Conversation> conversationPredicate2 = item -> selectedLanguages.isEmpty()
                    || selectedLanguages.contains(item.getLanguage());
            Predicate<Conversation> conversationPredicate3 = item ->
                    viewModel.getCurrentSettings().get() == null
                    || viewModel.getCurrentSettings().get().getSelectedLanguages().isEmpty()
                    || (viewModel.getCurrentSettings().get().getSelectedLanguages().size() == 1 && viewModel.getCurrentSettings().get().getSelectedLanguages().getFirst().isEmpty())
                    || viewModel.getCurrentSettings().get().getSelectedLanguages().contains(item.getLanguageFromId());

            return ListFilter.filterList((List<Conversation>)list, conversationPredicate, conversationPredicate2, conversationPredicate3);
        }else if(list.getFirst() instanceof ConversationChain){
            Predicate<ConversationChain> conversationChainPredicate = item ->
                    searchBar.getSearchFieldText().isEmpty()
                    || item.getTitle().toLowerCase().contains(searchBar.textProperty.get().toLowerCase());

            Predicate<ConversationChain> conversationChainPredicate2 = item ->
                    selectedLanguages.isEmpty()
                    || selectedLanguages.contains(item.getLanguage());

            Predicate<ConversationChain> conversationChainPredicate3 = item ->
                    viewModel.getCurrentSettings().get() == null
                    || viewModel.getCurrentSettings().get().getSelectedLanguages().isEmpty()
                    || (viewModel.getCurrentSettings().get().getSelectedLanguages().size() == 1 && viewModel.getCurrentSettings().get().getSelectedLanguages().getFirst().isEmpty())
                    || viewModel.getCurrentSettings().get().getSelectedLanguages().contains(item.getLanguageFromId());

            return ListFilter.filterList((List<ConversationChain>)list, conversationChainPredicate, conversationChainPredicate2, conversationChainPredicate3);
        }else if(list.getFirst() instanceof UserConversation){
            Predicate<UserConversation> userConversationPredicate = item ->
                    searchBar.textProperty.get().isEmpty()
                    || item.getConversation().getTitle().toLowerCase().contains(searchBar.textProperty.get().toLowerCase());

            Predicate<UserConversation> userConversationPredicate2 = item -> selectedLanguages.isEmpty()
                    || selectedLanguages.contains(item.getConversation().getLanguage());

            Predicate<UserConversation> userConversationPredicate3 = item ->
                    viewModel.getCurrentSettings().get() == null
                    || viewModel.getCurrentSettings().get().getSelectedLanguages().isEmpty()
                    || (viewModel.getCurrentSettings().get().getSelectedLanguages().size() == 1 && viewModel.getCurrentSettings().get().getSelectedLanguages().getFirst().isEmpty())
                    || viewModel.getCurrentSettings().get().getSelectedLanguages().contains(item.getConversation().getLanguageFromId());

            return ListFilter.filterList((List<UserConversation>)list, userConversationPredicate, userConversationPredicate2, userConversationPredicate3);
        }else if(list.getFirst() instanceof UserConversationChainItem){
            Predicate<UserConversationChainItem> userConversationChainItemPredicate = item ->
                    searchBar.getSearchFieldText().isEmpty()
                    || item.getConversationChain().getTitle().toLowerCase().contains(searchBar.textProperty.get().toLowerCase());

            Predicate<UserConversationChainItem> userConversationChainItemPredicate2 = item ->
                    selectedLanguages.isEmpty()
                    || selectedLanguages.contains(item.getConversationChain().getLanguage());

            return ListFilter.filterList((List<UserConversationChainItem>)list, userConversationChainItemPredicate, userConversationChainItemPredicate2);
        }
        return list;
    }
}
