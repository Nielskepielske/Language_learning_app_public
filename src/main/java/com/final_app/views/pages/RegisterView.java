package com.final_app.views.pages;

import com.final_app.viewmodels.RegisterViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegisterView implements FxmlView<RegisterViewModel> {
    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnBackToLogin;

    @FXML
    private Label lblError;

    @FXML
    private VBox root;

    @InjectViewModel
    private RegisterViewModel viewModel;

    public void initialize() {
        Platform.runLater(() -> {
            // Bind text fields to view model properties
            txtUsername.textProperty().bindBidirectional(viewModel.usernameProperty());
            txtEmail.textProperty().bindBidirectional(viewModel.emailProperty());
            txtPassword.textProperty().bindBidirectional(viewModel.passwordProperty());
            txtConfirmPassword.textProperty().bindBidirectional(viewModel.confirmPasswordProperty());
            lblError.textProperty().bind(viewModel.errorMessageProperty());

            // Set button actions
            btnRegister.setOnAction(e -> viewModel.register());
            btnBackToLogin.setOnAction(e -> viewModel.navigateToLogin());

            // Focus on username field
            txtUsername.requestFocus();
        });
    }
}
