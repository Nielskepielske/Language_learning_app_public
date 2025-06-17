package com.final_app.viewmodels;

import com.final_app.globals.TKey;
import com.final_app.models.NavigationItem;
import com.final_app.services.AppService;
import com.final_app.tools.TranslationManager;
import com.final_app.views.pages.*;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.util.Objects;


public class NavigationPaneViewModel implements ViewModel {

    private final ObservableList<NavigationItem> navigationItems = FXCollections.observableArrayList();

    public NavigationPaneViewModel() {
        loadNavigationItems();
        TranslationManager.get().addLanguageChangeListener(newLanguage -> {
            loadNavigationItems();
        });
    }

    private void loadNavigationItems(){
        Platform.runLater(() -> {
            navigationItems.clear();
            // Voeg hier de navigatie-items toe.
            navigationItems.add(new NavigationItem(TranslationManager.get().t(TKey.DASHBOARD), DashBoardView.class, "dashboard.svg"));
            navigationItems.add(new NavigationItem(TranslationManager.get().t(TKey.CONVERSATIONS), ConversationsView.class, "conversation.svg"));
            navigationItems.add(new NavigationItem(TranslationManager.get().t(TKey.SPEAKINGTESTS), SpeakingTestsView.class, "microphone.svg"));
            navigationItems.add(new NavigationItem(TranslationManager.get().t(TKey.SETTINGS), SettingsView.class, "settings_light.svg"));
            navigationItems.add(new NavigationItem(TranslationManager.get().t(TKey.LOGOUT), "logout_light.svg", ()->{
                AppService.getInstance().logout();
            }));
            //navigationItems.add(new NavigationItem("ChatView", ChatView.class));
        });

    }

    private Image loadIcon(String fileName){
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/final_app/icons/" + fileName)));
    }

    public ObservableList<NavigationItem> getNavigationItems() {
        return navigationItems;
    }

    public void navigate(NavigationItem item) {
        // Haal de NavigationService op via de RootViewModel (of via dependency injection)
        if(item.getViewClass() != null){
            RootViewModel.getInstance().getNavigationService().navigateTo(item.getViewClass());
        }else if(item.getAction() != null){
            item.getAction().run();
        }
    }
}
