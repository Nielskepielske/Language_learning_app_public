package com.final_app.viewmodels;

import com.final_app.globals.TKey;
import com.final_app.models.*;
import com.final_app.services.AppService;
import com.final_app.services.ConversationService;
import com.final_app.services.UserService;
import com.final_app.tools.PerformanceTimer;
import com.final_app.tools.TranslationManager;
import com.final_app.views.pages.ChatView;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ConversationsViewModel extends BaseViewModel {
    private final AppService appService = AppService.getInstance();
    private final UserService userService = appService.getUserService();
    private final ConversationService conversationService = appService.getConversationService();

    private final ObservableList<UserConversation> userConversations = FXCollections.observableArrayList();
    private final ObservableList<ConversationChain> userConversationChains = FXCollections.observableArrayList();
    private final ObservableMap<String, List<UserConversation>> groupedUserConversations = FXCollections.observableHashMap();
    private final ObservableList<Conversation> availableConversations = FXCollections.observableArrayList();
    private final ObservableList<ConversationChain> availableConversationChains = FXCollections.observableArrayList();

    private final ObservableList<Language> languages = FXCollections.observableArrayList();
    private final ObjectProperty<Settings> currentSettings = new SimpleObjectProperty<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    RootViewModel rootViewModel;

    public void initialize() {
        PerformanceTimer.start("ConversationsViewModel.initialize");
        rootViewModel = RootViewModel.getInstance();


        executor.submit(() -> {
            try {
                if (appService.isAuthenticated()) {
                    String userId = appService.getCurrentUser().getId();

                    // Load all necessary data in bulk
                    List<UserConversation> userConvos = appService.getConversationService()
                            .getUserConversations(userId);

                    List<Conversation> allConversations = conversationService.getAllConversations();
                    List<ConversationChain> allChains = conversationService.getAllConversationChains();

                    Map<String, Conversation> conversationMap = allConversations.stream()
                            .collect(Collectors.toMap(Conversation::getId, c -> c));

                    List<UserLanguage> userLanguages = appService.getLanguageService().getUserLanguages(userId);

                    languages.setAll(userLanguages.stream().map(UserLanguage::getLanguage).toList());

                    // Map conversations to user conversations
                    for (UserConversation uc : userConvos) {
                        uc.setConversation(conversationMap.get(uc.getConversationId()));
                    }

                    // Group user conversations by conversation ID
                    Map<String, List<UserConversation>> groupedConvos = userConvos.stream()
                            .collect(Collectors.groupingBy(UserConversation::getConversationId));

                    // Find available conversations user hasn't started yet
                    List<String> startedConversationIds = userConvos.stream()
                            .map(UserConversation::getConversationId)
                            .toList();

                    List<Conversation> availableConvos = allConversations.stream()
                            .filter(c -> !startedConversationIds.contains(c.getId()) &&
                                    userLanguages.stream()
                                            .map(UserLanguage::getLanguageId)
                                            .anyMatch(langId -> langId.equals(c.getLanguageId())))
                            .toList();

                    // Partition chains into those containing user conversations and those that don't
                    List<ConversationChain> userChains = new ArrayList<>();
                    List<ConversationChain> availableChains = new ArrayList<>();

                    for (ConversationChain chain : allChains) {
                        List<UserConversationChainItem> items = conversationService
                                .getUserConversationFromConversationChain(chain.getId());
                        if (items.size() > 0) {
                            userChains.add(chain);
                        } else {
                            availableChains.add(chain);
                        }
                    }

                    appService.getUserService().getUserSettings(getCurrentUser().getId())
                            .thenAccept(currentSettings::set);

                    // Apply everything to UI thread
                    Platform.runLater(() -> {
                        userConversations.setAll(userConvos);
                        groupedUserConversations.clear();
                        groupedUserConversations.putAll(groupedConvos);
                        availableConversations.setAll(availableConvos);
                        userConversationChains.setAll(userChains);
                        availableConversationChains.setAll(availableChains);

                        // System text bindings
//                        lblTitleProperty.bind(TranslationManager.get().t(TKey.CTITLE));
//                        lblDescriptionProperty.bind(TranslationManager.get().t(TKey.CDESCRIPTION));
//                        lblConversationChains.bind(TranslationManager.get().t(TKey.CCONVERSATIONCHAINS));
//                        txtNewConversation.bind(TranslationManager.get().t(TKey.CNEWCONVERSATION));
//                        lblAConversationChains.bind(TranslationManager.get().t(TKey.CACONVERSATIONCHAINS));
//                        lblAConversations.bind(TranslationManager.get().t(TKey.CACONVERSATIONS));
                    });

                }
                PerformanceTimer.stop("ConversationsViewModel.initialize");

            } catch (SQLException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public ObservableList<UserConversation> getUserConversations() {
        return userConversations;
    }

    public ObservableMap<String, List<UserConversation>> getGroupedUserConversations() {
        return groupedUserConversations;
    }

    public ObservableList<Conversation> getAvailableConversations() {
        return availableConversations;
    }

    public ObservableList<ConversationChain> getUserConversationChains() {
        return userConversationChains;
    }

    public ObservableList<ConversationChain> getAvailableConversationChains() {
        return availableConversationChains;
    }

    public ObservableList<Language> getLanguages() {
        return languages;
    }

    public ObjectProperty<Settings> getCurrentSettings() { return currentSettings; }

    public void navigateToChatView(UserConversation userConversation) {
        rootViewModel.getNavigationService().navigate(ChatView.class,
                vm -> vm.setUserConversation(userConversation), true);
    }

    public void startNewConversation(Conversation conversation) {
        try {
            if (!appService.isAuthenticated()) {
                System.err.println("User not logged in");
                return;
            }
            UserConversation userConversation = conversationService.startConversation(
                    appService.getCurrentUser().getId(), conversation.getId());
            rootViewModel.getNavigationService().navigateTo(ChatView.class,
                    vm -> vm.setUserConversation(userConversation));
        } catch (SQLException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNavigatedTo() {
        this.initialize();
    }

    @Override
    public void onNavigatedFrom() {

    }

    // UI bindings system text
    public StringProperty lblTitleProperty = TranslationManager.get().t(TKey.CTITLE);
    public StringProperty lblDescriptionProperty = TranslationManager.get().t(TKey.CDESCRIPTION);
    public StringProperty lblConversationChains = TranslationManager.get().t(TKey.CCONVERSATIONCHAINS);
    public StringProperty txtNewConversation = TranslationManager.get().t(TKey.CNEWCONVERSATION);
    public StringProperty lblAConversationChains = TranslationManager.get().t(TKey.CACONVERSATIONCHAINS);
    public StringProperty lblAConversations = TranslationManager.get().t(TKey.CACONVERSATIONS);
}
