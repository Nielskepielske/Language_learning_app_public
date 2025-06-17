package com.final_app;

import com.final_app.factories.RepositoryFactory;
import com.final_app.globals.GlobalVariables;
import com.final_app.models.User;
import com.final_app.scopes.ChatScope;
import com.final_app.services.AppService;
import com.final_app.services.DataSynchronizeService;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.sql.SQLException;

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
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