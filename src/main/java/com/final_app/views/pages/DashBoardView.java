package com.final_app.views.pages;

import com.final_app.events.UserChangeEvent;
import com.final_app.events.XpEarnedEvent;
import com.final_app.globals.GlobalVariables;
import com.final_app.models.SimpleStat;
import com.final_app.models.UserLanguage;
import com.final_app.services.AppService;
import com.final_app.services.UserService;
import com.final_app.tools.PerformanceTimer;
import com.final_app.tools.SVGUtil;
import com.final_app.viewmodels.DashBoardViewModel;
import com.final_app.views.components.*;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class DashBoardView implements FxmlView<DashBoardViewModel> {
    @FXML private VBox root;
    @FXML private FlowPane simpleStats;
    @FXML private Label lblLevel;
    @FXML private Label lblProgress;
    @FXML private Label lblXpToGo;
    @FXML private ProgressBar totalProgress;
    @FXML private Label lblWelcomeMessage;
    @FXML private VBox languageProgress;
    @FXML private Button btnToggleXpView;
    @FXML private TabPane tabPane;
    @FXML private Tab tabSummary;
    @FXML private Tab tabAnalytics;
    @FXML private StackPane xpSummaryContainer;
    @FXML private StackPane xpAnalyticsContainer;
    @FXML private StackPane notificationOverlay;

    @FXML private HBox btnAddLanguage;
    @FXML private LanguageSelectionView languageSelectionView;
    @FXML private HBox btnCreateItems;
    @FXML private CreateItemsView createItemsView;

    // New logout button
    @FXML private Button btnLogout;

    // New user profile button
    @FXML
    private HBox userProfileBox;

    @InjectViewModel
    private DashBoardViewModel viewModel;

    private XpNotificationView xpNotificationView;

    private LoadingScreen loadingScreen;

    // Icons
    @FXML private ImageView imgAddLanguage;
    @FXML private ImageView imgCreateItems;


    // Extra's for translation
    @FXML private Label lblDescription;
    @FXML private Label lblLevelTitle;
    @FXML private Label lblLevelDescription;
    @FXML private Label lblLanguageProgressTitle;
    @FXML private Label lblLanguageProgressDescription;
    @FXML private Label lblAddLanguage;
    @FXML private Label lblCreateItems;

    @FXML
    public void initialize() {
        Platform.runLater(()->{
            PerformanceTimer.start("DashBoardView.initialize");
            try{
                viewModel.syncState.addListener((observable, oldValue, newValue) -> {
                    if(newValue == DashBoardViewModel.SyncState.SYNCED){
                        setupBindingsAndUI();
                        Platform.runLater(()->{
                            loadingScreen.close();
                        });
                    }else{
                        Platform.runLater(()->{
                            loadingScreen.show();
                        });
                    }
                });

                if(viewModel.syncState.get() == DashBoardViewModel.SyncState.SYNCED){
                    setupBindingsAndUI();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            setupBindingsAndUI();
            PerformanceTimer.stop("DashBoardView.initialize");
        });
    }

    private void setupBindingsAndUI() {
            PerformanceTimer.start("DashBoardView");
            //initializeNotificationOverlay();

            totalProgress.progressProperty().bind(viewModel.currentProgressProperty());
            lblLevel.textProperty().bind(viewModel.currentLevelProperty());
            lblProgress.textProperty().bind(viewModel.progressTextProperty());
            lblXpToGo.textProperty().bind(viewModel.xpToNextLevelProperty());
            lblWelcomeMessage.textProperty().bind(viewModel.welcomeMessageProperty());

            lblDescription.textProperty().bind(viewModel.welcomeDescriptionMessageProperty);
            lblLevelTitle.textProperty().bind(viewModel.levelTitleProperty);
            lblLevelDescription.textProperty().bind(viewModel.levelDescriptionProperty);
            lblAddLanguage.textProperty().bind(viewModel.btnAddLanguageTextProperty);
            lblCreateItems.textProperty().bind(viewModel.btnCreateItemsTextProperty);
            lblLanguageProgressTitle.textProperty().bind(viewModel.languageProgressTitleProperty);
            lblLanguageProgressDescription.textProperty().bind(viewModel.languageProgressDescriptionProperty);

            imgAddLanguage.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/book_light.svg", 20, 20));
            imgCreateItems.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/create_dark.svg", 20, 20));


            initializeStats();
            languageProgress.setSpacing(20);
            loadLanguages();

            if (btnLogout != null) {
                btnLogout.setOnAction(e -> viewModel.logout());
                ImageView logoutIcon = new ImageView(
                        SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/logout_light.svg", 20, 20)
                );
                btnLogout.setGraphic(logoutIcon);
            }

            setupUserProfile();

            //viewModel.refreshUserData();

            simpleStats.widthProperty().addListener((obs, oldVal, newVal) -> {
                simpleStats.getChildren().clear();
                initializeStats();
            });

            btnAddLanguage.setOnMouseClicked(e -> {
                languageSelectionView.initializeRows(viewModel.availableLanguages);
                languageSelectionView.setVisible(true);
            });
            languageSelectionView.setOnBackButtonClicked(e -> languageSelectionView.setVisible(false));
            languageSelectionView.setOnAddClicked(lng -> {
                try {
                    viewModel.addUserLanguage(lng);
                    languageSelectionView.setVisible(false);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        viewModel.userLanguages.addListener((ListChangeListener<UserLanguage>) e -> {
            Platform.runLater(this::loadLanguages);
        });
        root.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                loadingScreen = new LoadingScreen((Stage) root.getScene().getWindow(), "Loading...");
            }
        });


            btnCreateItems.setOnMouseClicked(e -> createItemsView.setVisible(true));
            createItemsView.setOnClosedClicked(e -> createItemsView.setVisible(false));

            root.addEventHandler(UserChangeEvent.ANY, event -> {
                Platform.runLater(() -> {
                    //viewModel.refreshUserData();
                    setupUserProfile();
                });
            });

        PerformanceTimer.stop("DashBoardView");

    }


    private void setupUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.getChildren().clear();

            // Get current user
            if (AppService.getInstance().isAuthenticated()) {
                String userName = AppService.getInstance().getCurrentUser().getUserName();

                // Create user avatar or initials
                Label userLabel = new Label(getUserInitials(userName));
                userLabel.getStyleClass().addAll("user-avatar", "primary");

                userProfileBox.getChildren().add(userLabel);
            }
        }
    }

    private String getUserInitials(String userName) {
        if (userName == null || userName.isEmpty()) {
            return "?";
        }

        String[] parts = userName.split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));

                // Limit to 2 characters
                if (initials.length() >= 2) {
                    break;
                }
            }
        }

        return initials.toString().toUpperCase();
    }


    private void loadLanguages(){
        Platform.runLater(()->{
            languageProgress.getChildren().clear();
            viewModel.userLanguages.forEach(language -> {
                LanguageLevelSimple ls = new LanguageLevelSimple();
                ls.setAll(language);
                ls.setId(language.getId());
                languageProgress.getChildren().add(ls);
            });
        });
    }

    private void initializeStats() {
        Platform.runLater(()->{
            simpleStats.prefWidthProperty().bind(root.widthProperty());
            simpleStats.setHgap(10);
            simpleStats.setVgap(10);
            simpleStats.getChildren().clear();
            viewModel.statList.forEach(stat -> {
                StatCard statCard = new StatCard();
                statCard.setAll(stat.title, stat.value, stat.extra, stat.icon);
                statCard.setPrefWidth((simpleStats.getWidth() - (simpleStats.getHgap() * (viewModel.statList.size()))) / viewModel.statList.size());
                statCard.setMinWidth(250);
                simpleStats.getChildren().add(statCard);
            });
            viewModel.statList.addListener((ListChangeListener<? super SimpleStat>) change -> {
                simpleStats.getChildren().clear();
                viewModel.statList.forEach(stat -> {
                    StatCard statCard = new StatCard();
                    statCard.setAll(stat.title, stat.value, stat.extra, stat.icon);
                    statCard.setPrefWidth((simpleStats.getWidth() - (simpleStats.getHgap() * (viewModel.statList.size()))) / viewModel.statList.size());
                    statCard.setMinWidth(250);
                    simpleStats.getChildren().add(statCard);
                });
            });
        });
    }
}