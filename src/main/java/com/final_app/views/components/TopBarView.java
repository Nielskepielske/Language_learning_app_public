package com.final_app.views.components;

import com.final_app.events.EventBus;
import com.final_app.events.UserChangeEvent;
import com.final_app.events.XpEarnedEvent;
import com.final_app.globals.TKey;
import com.final_app.models.User;
import com.final_app.models.UserStats;
import com.final_app.services.AppService;
import com.final_app.services.XpService;
import com.final_app.tools.TranslationManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Popup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Consumer;

public class TopBarView extends StackPane {
    @FXML private Label lblLevel;
    @FXML private ProgressBar pbLevel;
    @FXML private Label lblUser;

    private Popup profilePopup;


    private int level = 0;
    private StringProperty levelString = new SimpleStringProperty("Level 0");
    private IntegerProperty levelInt = new SimpleIntegerProperty(0);
    private DoubleProperty progressLevel = new SimpleDoubleProperty(0.0);
    private StringProperty user = new SimpleStringProperty("JD");

    private AppService appService = AppService.getInstance();

    private Consumer<Void> onUserClicked;
    public void setOnUserClicked(Consumer<Void> onUserClicked) {this.onUserClicked = onUserClicked;}

    public TopBarView(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/TopBarView.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            lblLevel.textProperty().bind(this.levelString);
            lblLevel.setTextFill(Paint.valueOf("white"));
            lblLevel.getStyleClass().add("f-semi-bold");

            pbLevel.progressProperty().bind(this.progressLevel);
            pbLevel.setPrefHeight(10);

            lblUser.textProperty().bind(this.user);

            levelString.bind(Bindings.concat(TranslationManager.get().t(TKey.DLEVELPROGRESSH), " ", levelInt));

            //HBox.setMargin(levelProgress, new Insets(0, 40, 0 , 0));

            this.getStyleClass().add("topBar");

            EventBus.getInstance().subscribe(UserChangeEvent.ANY, e -> {
                try {
                    if(e.getEventType().equals(UserChangeEvent.LOGOUT)){
                        user.set(null);
                        //levelString.unbind();
                        //levelString.set(null);
                        level = 0;
                        levelInt.set(level);
                        progressLevel.set(0);
                        pbLevel.setVisible(false);
                    }else if(e.getEventType().equals(UserChangeEvent.LOGIN)){
                        pbLevel.setVisible(true);
                        getAllValuesFromUser();
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });
            TranslationManager.get().addLanguageChangeListener(newLanguage -> {
                try {
                    getAllValuesFromUser();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

//            lblUser.setOnMouseClicked(event -> {
//                if (profilePopup == null) {
//                    profilePopup = new Popup();
//                    profilePopup.setAutoHide(true);
//
//                    VBox popupContent = new VBox();
//                    popupContent.getStyleClass().addAll("card", "p-2");
//                    popupContent.setBackground(Background.EMPTY);
//
//                    this.setClip(null);
//
//                    HBox btnLogout = new HBox();
//                    btnLogout.getStyleClass().addAll("btn-modern", "border-light", "border-1", "primary");
//                    btnLogout.setPadding(new Insets(10));
//                    Label lblLogout = new Label("Logout");
//                    lblLogout.getStyleClass().addAll("primary");
//                    btnLogout.getChildren().add(lblLogout);
//
//                    popupContent.getChildren().add(btnLogout);
//
//                    profilePopup.getContent().add(popupContent);
//
//                    btnLogout.setOnMouseClicked(e -> {
//                        appService.logout();
//                        profilePopup.hide();
//                    });
//                }
//
//                if (profilePopup.isShowing()) {
//                    profilePopup.hide();
//                } else {
//                    Bounds bounds = lblUser.localToScreen(lblUser.getBoundsInLocal());
//                    profilePopup.show(lblUser, bounds.getMinX(), bounds.getMaxY() + 20);
//                }
//            });

            getAllValuesFromUser();

            EventBus.getInstance().subscribe(XpEarnedEvent.ANY, e -> {
                try {
                    getAllValuesFromUser();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }catch (IOException | SQLException e){
            throw new RuntimeException(e);
        }

    }

    private void getAllValuesFromUser() throws SQLException {
        Platform.runLater(() -> {
            User currentUser = appService.getCurrentUser();
            UserStats currentUserStats = null;
            if(currentUser != null){
                try {
                    currentUserStats = appService.getUserService().getUserStats(currentUser.getId());
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                if(currentUserStats != null){
                    int level = appService.getXpService().calculateLevelFromXp(currentUserStats.getTotalXp());
                    levelInt.set(level);
                    //levelString.set(TranslationManager.get().t(TKey.DLEVELPROGRESSH).get() + " " + level);
                    //levelString.(" " + level);
                    progressLevel.set(appService.getXpService().calculateProgressToNextLevel(currentUserStats.getTotalXp(), level));
                    user.set(currentUser.getUserName().substring(0, 2).toUpperCase());
                }
            }

        });

    }

    public void setAll(int level, int progressLevel, String user){
        this.level = level;
        this.progressLevel.set(progressLevel);
        this.user.set(user);
    }

    public void setLevel(int level){
        this.level = level;
        this.levelString.set(TranslationManager.get().t(TKey.DLEVELPROGRESSH) + " " + this.level);
    }
    public void setProgressLevel(double progressLevel){
        this.progressLevel.set(progressLevel);
    }
    public void setUser(String user){
        this.user.set(user);
    }
}
