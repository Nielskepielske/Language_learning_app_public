package com.final_app.views.pages;

import com.final_app.viewmodels.LoginViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.awt.event.KeyEvent;

public class LoginView implements FxmlView<LoginViewModel> {
    @FXML private TextField txtEmail;

    @FXML private PasswordField txtPassword;

    @FXML private CheckBox chkRememberMe;

    @FXML private Button btnLogin;

    @FXML private Button btnRegister;

    @FXML private Label lblError;

    @FXML private VBox root;

    @InjectViewModel
    private LoginViewModel viewModel;

    public void initialize() {
        Platform.runLater(() -> {
            // Bind text fields to view model properties
            txtEmail.textProperty().bindBidirectional(viewModel.emailProperty());
            txtPassword.textProperty().bindBidirectional(viewModel.passwordProperty());
            chkRememberMe.selectedProperty().bindBidirectional(viewModel.rememberMeProperty());
            lblError.textProperty().bind(viewModel.errorMessageProperty());

            // Set button actions
            btnLogin.setOnAction(e -> viewModel.login());
            btnRegister.setOnAction(e -> viewModel.navigateToRegister());

            // Check for saved credentials
            viewModel.loadSavedCredentials();

            // Focus on email field
            //txtEmail.requestFocus();
            txtEmail.selectAll();

            this.root.requestFocus();
            this.root.setOnKeyReleased(k -> {
                if(k.getCode() == KeyCode.ENTER) {
                    viewModel.login();
                }
            });
        });
    }
}