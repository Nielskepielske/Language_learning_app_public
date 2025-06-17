package com.final_app;

import com.final_app.factories.RepositoryFactory;
import com.final_app.globals.GlobalVariables;
import com.final_app.models.User;
import com.final_app.scopes.ChatScope;
import com.final_app.services.AppService;
import com.final_app.services.DataSynchronizeService;
import com.final_app.tools.ApiKeyManager;
import com.final_app.tools.SessionManager;
import com.final_app.tools.StyleLoader;
import com.final_app.viewmodels.RootViewModel;
import com.final_app.views.pages.DashBoardView;
import com.final_app.views.pages.LoginView;
import com.final_app.views.pages.RootView;
import com.zaxxer.hikari.HikariDataSource;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class Starter extends Application {
    @Override
    public void start(Stage stage) {
        try {
            //RepositoryFactory.getInstance().changeToOnline();
            Dotenv dotenv = Dotenv.load();
            // Initialize the AppService without auto-login
            AppService.getInstance().initialize();

            stage.setTitle("LinguaLeap");

            ChatScope scope = new ChatScope();

            stage.setFullScreenExitHint("");

            // Load the RootView (the main container)
            ViewTuple<RootView, RootViewModel> rootViewTuple =
                    FluentViewLoader.fxmlView(RootView.class)
                            .load();
            Parent root = rootViewTuple.getView();
            root.setOnKeyReleased(e -> {
                if(e.getCode() == KeyCode.F11) {
                    stage.setFullScreen(true);
                }
            });

            Scene scene = new Scene(root);

            StyleLoader.loadStyles(scene);
            stage.setScene(scene);
            stage.setResizable(true);


            // Check for saved session and attempt auto-login
            if (SessionManager.getInstance().hasSavedSession() &&
                    SessionManager.getInstance().restoreSession()) {
                // Session restored successfully, navigate to dashboard
                User user = AppService.getInstance().getUserService().getUserByEmail(SessionManager.getInstance().getSavedEmail());
                User currentUser = AppService.getInstance().login(user.getEmail(), user.getPassword());
                RootViewModel.getInstance().getNavigationService().navigateTo(DashBoardView.class);
            } else {
                // No valid session, start with login screen
                RootViewModel.getInstance().getNavigationService().navigateTo(LoginView.class);
            }
            stage.setFullScreen(true);
            stage.show();

            Platform.runLater(this::checkAndPromptForApiKey);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private String openAiApiKey;

    private void checkAndPromptForApiKey() {
        Optional<String> keyOptional = ApiKeyManager.loadApiKey();

        if (keyOptional.isPresent()) {
            this.openAiApiKey = keyOptional.get();
            //statusLabel.setText("Status: API Key is loaded and ready!");
            // You can now enable features that use the API key
        } else {
            //statusLabel.setText("Status: API Key not found. Prompting user...");
            Optional<String> newKeyOptional = showApiKeyInputDialog();

            if (newKeyOptional.isPresent()) {
                String newKey = newKeyOptional.get();
                if (ApiKeyManager.saveApiKey(newKey)) {
                    this.openAiApiKey = newKey;
                    //statusLabel.setText("Status: API Key saved and loaded!");
                } else {
                    //statusLabel.setText("Status: Failed to save API Key.");
                    // Handle failure (e.g., show an error alert)
                }
            } else {
                //statusLabel.setText("Status: Canceled. API Key is required to proceed.");
                // Disable features or close the app if the key is mandatory
            }
        }
    }

    private Optional<String> showApiKeyInputDialog() {
        // Create a custom dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("API Key Required");
        dialog.setHeaderText("Please enter your OpenAI API Key to continue.");

        // Set the button types (Save and Cancel)
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the layout for the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField apiKeyField = new PasswordField();
        apiKeyField.setPromptText("sk-...");

        grid.add(new Label("API Key:"), 0, 0);
        grid.add(apiKeyField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Enable/disable the Save button depending on whether a key was entered.
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        apiKeyField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        // Request focus on the api key field by default.
        Platform.runLater(apiKeyField::requestFocus);

        // Convert the result to the api key string when the save button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return apiKeyField.getText();
            }
            return null;
        });

        // Show the dialog and wait for the user to close it
        return dialog.showAndWait();
    }


    @Override
    public void stop(){
        User currentUser = AppService.getInstance().getCurrentUser();
        AppService.getInstance().getDataSynchronizeService().synchronizeDB(currentUser, DataSynchronizeService.SyncType.LOCAL_TO_ONLINE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}