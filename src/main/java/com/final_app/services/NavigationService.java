package com.final_app.services;

import com.final_app.scopes.ChatScope;
import com.final_app.tools.PerformanceTimer;
import com.final_app.views.pages.ChatView;
import com.final_app.views.pages.SpeakingTestPage;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * NavigationService with view caching, back-navigation,
 * and support for Navigable view-models to reload data on display.
 */
public class NavigationService {

    /**
     * Interface for view-models that should reload or refresh data
     * each time their view is displayed via this service.
     */
    public interface Navigable {
        /**
         * Called each time the view is navigated to.
         */
        void onNavigatedTo();

        /**
         * Called each time the view is navigated from.
         */
        void onNavigatedFrom();
    }

    private final Pane container;
    private final ChatScope sharedChatScope = new ChatScope();

    // Cache loaded views to avoid re-parsing FXML
    private final Map<Class<?>, ViewTuple<?, ?>> viewCache = new HashMap<>();

    // History stack of navigation actions
    private final List<Runnable> backStack = new ArrayList<>();

    private ViewTuple<?, ?> currentViewTuple;

    public NavigationService(Pane container) {
        this.container = container;
    }

    /**
     * Navigate to a view with an optional initializer, recording history.
     */
    public <T extends FxmlView<V>, V extends ViewModel> void navigateTo(
            Class<T> viewClass,
            Consumer<V> initializer
    ) {
        navigate(viewClass, initializer, true);
    }

    /**
     * Navigate to a view without initializer.
     */
    public void navigateTo(Class<? extends FxmlView<?>> viewClass) {
        //noinspection unchecked
        navigate((Class<FxmlView<ViewModel>>) viewClass, null, true);
    }

    /**
     * Internal navigation logic; optionally record history.
     */
    public <T extends FxmlView<V>, V extends ViewModel> void navigate(
            Class<T> viewClass,
            Consumer<V> initializer,
            boolean recordHistory
    ) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> navigate(viewClass, initializer, recordHistory));
            return;
        }

        // 1) Notify old view-model dat we vertrekken
        if (currentViewTuple != null) {
            System.out.println("Navigating from: " + currentViewTuple.getCodeBehind().getClass().getSimpleName() + " to: " + viewClass.getSimpleName() + " (recordHistory: " + recordHistory + ")" );
            ViewModel oldVm = currentViewTuple.getViewModel();
            if (oldVm instanceof Navigable) {
                ((Navigable) oldVm).onNavigatedFrom();
            }
        }


        PerformanceTimer.start("navigateTo");

        @SuppressWarnings("unchecked")
        ViewTuple<T, V> viewTuple = (ViewTuple<T, V>) viewCache.get(viewClass);
        if (viewTuple == null) {
            viewTuple = FluentViewLoader
                    .fxmlView(viewClass)
                    .providedScopes(sharedChatScope)
                    .load();
            if(!viewClass.equals(ChatView.class) && !viewClass.equals(SpeakingTestPage.class)) {
                viewCache.put(viewClass, viewTuple);
            }
        }

        // Apply initializer if provided
        if (initializer != null) {
            initializer.accept(viewTuple.getViewModel());
        }

        // Record history as a Runnable, avoiding recursive history on replay
        if (recordHistory) {
            backStack.add(() -> navigate(viewClass, initializer, recordHistory));
        }
        //backStack.add(()-> navigate((viewClass), initializer, false));

        // Display the view
        Node view = viewTuple.getView();
        if (container instanceof BorderPane) {
            ((BorderPane) container).setCenter(view);
        } else {
            container.getChildren().setAll(view);
        }

        // Keep the new view-model
        currentViewTuple = viewTuple;

        // Notify view-model if it wants to reload data
        ViewModel vm = viewTuple.getViewModel();
        if (vm instanceof Navigable) {
            ((Navigable) vm).onNavigatedTo();
        }

        PerformanceTimer.stop("navigateTo");
    }

    /**
     * Navigate back to the previous view, if available.
     */
    public void navigateBack() {
        if (backStack.size() < 2) {
            return;
        }
        // Pop current
        backStack.remove(backStack.size() - 1);
        // Execute the previous
        Runnable prev = backStack.remove(backStack.size() - 1);
        prev.run();

    }

    /**
     * Clears the cached views and history. Call this on logout or when fresh instances are needed.
     */
    public void clearCache() {
        viewCache.clear();
        backStack.clear();
    }
}
