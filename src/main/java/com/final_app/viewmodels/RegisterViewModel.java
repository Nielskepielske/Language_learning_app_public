package com.final_app.viewmodels;

import com.final_app.models.User;
import com.final_app.services.AppService;
import com.final_app.views.pages.DashBoardView;
import com.final_app.views.pages.LoginView;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterViewModel extends BaseViewModel {
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty confirmPassword = new SimpleStringProperty("");
    private final StringProperty errorMessage = new SimpleStringProperty("");



    // Regular expression for validating email
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public void register() {
        // Clear any previous errors
        errorMessage.set("");

        // Validate form
        if (username.get().isEmpty() || email.get().isEmpty() ||
                password.get().isEmpty() || confirmPassword.get().isEmpty()) {
            errorMessage.set("Please fill in all fields");
            return;
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email.get()).matches()) {
            errorMessage.set("Please enter a valid email address");
            return;
        }

        // Validate password match
        if (!password.get().equals(confirmPassword.get())) {
            errorMessage.set("Passwords do not match");
            return;
        }

        // Validate password strength (minimum 6 characters)
        if (password.get().length() < 6) {
            errorMessage.set("Password must be at least 6 characters long");
            return;
        }

        try {
            // Check if email already exists
            User existingUser = appService.getUserService().getUserByEmail(email.get());
            if (existingUser != null) {
                errorMessage.set("Email already registered. Please log in.");
                return;
            }

            // Register the user
            User newUser = appService.register(username.get(), email.get(), password.get());

            if (newUser != null) {
                // Registration successful - navigate to dashboard
                // Note: No need to explicitly fire UserChangeEvent as it's handled in AppService.register
                RootViewModel.getInstance().getNavigationService().navigateTo(DashBoardView.class);
            } else {
                // Registration failed
                errorMessage.set("Registration failed. Please try again.");
            }
        } catch (SQLException e) {
            // Database error
            errorMessage.set("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void navigateToLogin() {
        RootViewModel.getInstance().getNavigationService().navigateTo(LoginView.class);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public StringProperty confirmPasswordProperty() {
        return confirmPassword;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    @Override
    public void onNavigatedTo() {

    }

    @Override
    public void onNavigatedFrom() {

    }
}