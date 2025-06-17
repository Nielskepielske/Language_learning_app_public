package com.final_app.viewmodels;

import com.final_app.events.EventBus;
import com.final_app.events.UserChangeEvent;
import com.final_app.models.User;
import com.final_app.services.AppService;
import com.final_app.services.NavigationService;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;

/**
 * Base ViewModel class that provides common functionality for handling user changes
 */
public abstract class BaseViewModel implements ViewModel, NavigationService.Navigable {
    protected final AppService appService = AppService.getInstance();

    public BaseViewModel() {
        // Subscribe to user change events
        EventBus.getInstance().subscribe(UserChangeEvent.ANY, this::handleUserChange);
    }

    /**
     * Handle user change events (login/logout)
     */
    protected void handleUserChange(UserChangeEvent event) {
        Platform.runLater(() -> {
            if (event.getEventType() == UserChangeEvent.LOGIN) {
                onUserLogin(event.getUser());
            } else if (event.getEventType() == UserChangeEvent.LOGOUT) {
                onUserLogout();
            }
        });
    }

    /**
     * Called when a user logs in
     * Override in subclasses to handle specific actions
     */
    protected void onUserLogin(User user) {
        // Default implementation does nothing
    }

    /**
     * Called when a user logs out
     * Override in subclasses to handle specific actions
     */
    protected void onUserLogout() {
        // Default implementation does nothing
    }

    /**
     * Get current user (convenience method)
     */
    protected User getCurrentUser() {
        return appService.getCurrentUser();
    }

    /**
     * Check if a user is authenticated (convenience method)
     */
    protected boolean isAuthenticated() {
        return appService.isAuthenticated();
    }
}
