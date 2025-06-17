package com.final_app.viewmodels;

import com.final_app.models.User;
import com.final_app.services.AppService;
import com.final_app.tools.SessionManager;
import com.final_app.views.pages.DashBoardView;
import com.final_app.views.pages.RegisterView;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.SQLException;

public class LoginViewModel extends BaseViewModel {
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty rememberMe = new SimpleBooleanProperty(false);

    private final SessionManager sessionManager = SessionManager.getInstance();



    public void login() {
        // Clear any previous errors
        errorMessage.set("");

        // Validate form
        if (email.get().isEmpty() || password.get().isEmpty()) {
            errorMessage.set("Please enter both email and password");
            return;
        }

        try {
            // Attempt login
            User user = appService.login(email.get(), password.get());

            if (user != null) {
                // Save session information if remember me is checked
                if (rememberMe.get()) {
                    sessionManager.saveSession(email.get(), true);
                } else {
                    sessionManager.clearSession();
                }

                // Login successful - navigate to dashboard
                RootViewModel.getInstance().getNavigationService().navigateTo(DashBoardView.class);
            } else {
                // Login failed
                errorMessage.set("Invalid email or password");
            }
        } catch (SQLException e) {
            // Database error
            errorMessage.set("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load saved credentials if available
     */
    public void loadSavedCredentials() {
        if (sessionManager.hasSavedSession()) {
            email.set(sessionManager.getSavedEmail());

            // Here hardcoded autologin for sample user
            if(!email.get().isEmpty()) {
                if(email.get().equals("user@example.com")) {
                    try {
                        User user = appService.login(email.get(), "password123");
                        if(user != null) {
                            RootViewModel.getInstance().getNavigationService().navigateTo(DashBoardView.class);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            rememberMe.set(sessionManager.isRememberMeEnabled());

            // Attempt auto-login if session is valid
            if (sessionManager.restoreSession()) {
                try {
                    User user = appService.getUserService().getUserByEmail(email.get());
                    if (user == null) {
                        sessionManager.clearSession();
                        return;
                    }else{
                        appService.login(email.get(), user.getPassword());
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                RootViewModel.getInstance().getNavigationService().navigateTo(DashBoardView.class);
            }
        }
    }

    public void navigateToRegister() {
        RootViewModel.getInstance().getNavigationService().navigateTo(RegisterView.class);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public BooleanProperty rememberMeProperty() {
        return rememberMe;
    }

    @Override
    public void onNavigatedTo() {

    }

    @Override
    public void onNavigatedFrom() {

    }
}