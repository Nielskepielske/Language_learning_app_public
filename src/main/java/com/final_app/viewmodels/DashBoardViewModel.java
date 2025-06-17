package com.final_app.viewmodels;

import com.final_app.events.EventBus;
import com.final_app.events.UserChangeEvent;
import com.final_app.factories.RepositoryFactory;
import com.final_app.globals.TKey;
import com.final_app.models.*;
import com.final_app.services.*;
import com.final_app.tools.PerformanceTimer;
import com.final_app.tools.TranslationManager;
import com.final_app.views.pages.LoginView;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class DashBoardViewModel extends BaseViewModel {
    // Thread pool instead of raw threads
    private static final int THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors());
    // ExecutorService with dynamic pool size for background tasks
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    private final StringProperty welcomeMessage = new SimpleStringProperty();
    private final StringProperty currentLevel    = new SimpleStringProperty();
    private final IntegerProperty level = new SimpleIntegerProperty(0);
    private final StringProperty progressText    = new SimpleStringProperty();
    private final StringProperty xpToNextLevel   = new SimpleStringProperty();
    private final DoubleProperty progressValue   = new SimpleDoubleProperty(0);

    public final ObservableList<SimpleStat> statList           = FXCollections.observableArrayList();
    public final ObservableList<UserLanguage> userLanguages    = FXCollections.observableArrayList();
    public final ObservableList<Language> availableLanguages   = FXCollections.observableArrayList();

    private final WeekFields weekFields = WeekFields.of(Locale.getDefault());
    private final ZoneId     zoneId     = ZoneId.systemDefault();

    public enum SyncState { SYNCING, SYNCED, NOT_SYNCED }
    public final ObjectProperty<SyncState> syncState = new SimpleObjectProperty<>(SyncState.NOT_SYNCED);

    public DashBoardViewModel() {
        // Subscribe once to login events
        EventBus.getInstance().subscribe(UserChangeEvent.LOGIN, event -> initialize());
    }

    @Override
    public void onNavigatedTo() {
        initialize();
    }

    @Override
    public void onNavigatedFrom() {
        System.out.println("DashBoardViewModel.onNavigatedFrom()");
        //executor.shutdownNow();
    }

    private void initialize() {
        try {
            User currentUser = appService.getCurrentUser();
            if (RepositoryFactory.getInstance().getState() == RepositoryFactory.State.ONLINE
                    && currentUser != null && syncState.get() != SyncState.SYNCING) {
                syncState.set(SyncState.SYNCING);
                appService.getDataSynchronizeService()
                        .synchronizeDB(currentUser, DataSynchronizeService.SyncType.ONLINE_TO_LOCAL)
                        .thenCompose(result -> {
                            RepositoryFactory.getInstance().changeToOffline();
                            AppService.setTimesSynchronizedForUser(1);
                            return CompletableFuture.completedFuture(null);
                        })
                        .thenRun(this::refreshAllData)
                        .thenRun(() -> syncState.set(SyncState.SYNCED));
            } else if (currentUser != null && syncState.get() != SyncState.SYNCING) {
                RepositoryFactory.getInstance().changeToOffline();
                refreshAllData();
            }
        }catch (Exception e) {
            Logger.getLogger("DashBoardViewModel").warning("Error initializing dashboard: " + e.getMessage());
        }

    }

    private void refreshAllData() {
        PerformanceTimer.start("refreshUserData");
        User user = appService.getCurrentUser();
        if (user != null) {
            fetchStats(user);
            fetchLanguages(user);
            fetchUserSettings(user);
            refreshTranslations();
        }
        PerformanceTimer.stop("refreshUserData");
    }

    private void fetchStats(User user) {
        executor.submit(() -> {
            PerformanceTimer.start("fetchUserStats");
            UserStats stats;
            try {
                stats = appService.getUserService().getUserStats(user.getId());
                if (stats == null) {
                    appService.getUserService().updateUserStats(new UserStats(user.getId(), 1, 0, 0));
                    stats = appService.getUserService().getUserStats(user.getId());
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
            PerformanceTimer.stop("fetchUserStats");

            int level = stats.getLevel();
            long totalXp = stats.getTotalXp();

            // Calculate progress and XP required
            CompletableFuture<Long> xpForNextLevel  = CompletableFuture.supplyAsync(() -> appService.getXpService().calculateXpForLevel(level + 1) - appService.getXpService().calculateXpForLevel(level), executor);
            CompletableFuture<Long> currentXp       = CompletableFuture.supplyAsync(() -> totalXp - appService.getXpService().calculateXpForLevel(level), executor);
            CompletableFuture<Long> xpToLevelUp     = CompletableFuture.supplyAsync(() -> {
                try { return appService.getUserService().getXpRequiredForNextLevel(user.getId()); }
                catch (SQLException e) { throw new CompletionException(e); }
            }, executor);

            UserStats finalStats = stats;
            CompletableFuture.allOf(xpForNextLevel, currentXp, xpToLevelUp)
                    .thenRun(() -> Platform.runLater(() -> {
                        long xpNeededThisLevel = xpForNextLevel.join();
                        long xpEarned          = currentXp.join();
                        long xpRemaining       = xpToLevelUp.join() - totalXp;

                        welcomeMessage.concat( " " + user.getUserName() + "!");
                        //currentLevel.(TranslationManager.get().t(TKey.DLEVELPROGRESSH) + " " + level);
                        progressValue.set(xpNeededThisLevel == 0 ? 0 : (double) xpEarned / xpNeededThisLevel);
                        progressText.set(xpEarned + " XP / " + xpNeededThisLevel + " XP");
                        xpToNextLevel.unbind();
                        xpToNextLevel.bind(Bindings.concat(xpRemaining , " " , TranslationManager.get().t(TKey.XPTO) , " " , TranslationManager.get().t(TKey.DLEVELPROGRESSH) , " " , (level + 1)));
                        this.level.set(level);

                        updateStatsDetail(user, finalStats);
                    }));
        });
    }

    private void updateStatsDetail(User user, UserStats stats) {
        executor.submit(() -> {
            List<SimpleStat> temp = new ArrayList<>();
            temp.add(new SimpleStat(TranslationManager.get().t(TKey.DTOTALXP).get(), (int) stats.getTotalXp(), "", "medal.svg"));
            try {
                int streak = appService.getUserService().getUserStreak(user.getId());
                temp.add(new SimpleStat(TranslationManager.get().t(TKey.DDAYSTREAK).get(), streak, TranslationManager.get().t(TKey.DAYS).get(), "stat_up.svg"));

                List<UserConversation> convs = appService.getConversationService().getUserConversations(user.getId());
                long thisWeekConvs = convs.stream().filter(u -> isInCurrentWeek(u.getUpdatedAt())).count();
                temp.add(new SimpleStat(TranslationManager.get().t(TKey.CONVERSATIONS).get(), convs.size(), thisWeekConvs + " "+ TranslationManager.get().t(TKey.THISWEEK).get(), "conversation.svg"));

                List<UserSpeakingTest> tests = appService.getSpeakingTestService().getUserSpeakingTests(user.getId());
                long thisWeekTests = tests.stream().filter(u -> isInCurrentWeek(u.getCompletedAt())).count();
                temp.add(new SimpleStat(TranslationManager.get().t(TKey.SPEAKINGTESTS).get(), tests.size(), thisWeekTests + " "+ TranslationManager.get().t(TKey.THISWEEK).get(), "microphone_light.svg"));

            } catch (SQLException e) {
                throw new CompletionException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> statList.setAll(temp));
        });
    }

    private boolean isInCurrentWeek(Date date) {
        if (date == null) return false;
        LocalDateTime dt = date.toInstant().atZone(zoneId).toLocalDateTime();
        int week  = dt.get(weekFields.weekOfWeekBasedYear());
        int year  = dt.get(weekFields.weekBasedYear());
        LocalDateTime now = LocalDateTime.now(zoneId);
        return week == now.get(weekFields.weekOfWeekBasedYear()) && year == now.get(weekFields.weekBasedYear());
    }

    public void addUserLanguage(UserLanguage userLanguage) {
        executor.submit(() -> {
            User user = appService.getCurrentUser();
            if (user == null) return;
            try {
                Optional<UserLanguage> added = appService.getLanguageService()
                        .addUserLanguage(user.getId(), userLanguage.getLanguage().getId(), userLanguage.getLevelId())
                        .get();
                if (added.isPresent()) {
                    fetchLanguages(user);
                }
            } catch (InterruptedException | ExecutionException | SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    private void fetchLanguages(User user) {
        executor.submit(() -> {
            try {
                List<UserLanguage> langs = appService.getLanguageService().getUserLanguages(user.getId());
                List<Language> all   = appService.getLanguageService().getAllLanguages();
                List<String> ownedIds = langs.stream().map(UserLanguage::getLanguageId).toList();

                Platform.runLater(() -> {
                    userLanguages.setAll(langs);
                    availableLanguages.setAll(
                            all.stream()
                                    .filter(l -> !ownedIds.contains(l.getId()))
                                    .toList()
                    );
                });
            } catch (SQLException | InterruptedException | ExecutionException e) {
                throw new CompletionException(e);
            }
        });
    }

    public void logout() {
        appService.logout();
        Platform.runLater(() -> {
            welcomeMessage.set("");
            currentLevel.set("");
            progressText.set("");
            xpToNextLevel.set("");
            progressValue.set(0);
            statList.clear();
            userLanguages.clear();
            availableLanguages.clear();
        });
        RootViewModel.getInstance().getNavigationService().navigateTo(LoginView.class);
    }

    private void fetchUserSettings(User user){
        executor.submit(() -> {
            try {
                CompletableFuture<Settings> settings = appService.getUserService().getUserSettings(user.getId());

                settings.thenAccept(u -> {
                    if (u != null) {
                        try {
                            TranslationManager.get().setLanguage(u.getLanguage());
                            //refreshTranslations();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

            } catch (SQLException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void refreshTranslations(){
        Platform.runLater(() -> {
            welcomeMessage.bind(TranslationManager.get().t(TKey.DWELCOME).concat(" " + appService.getCurrentUser().getUserName() + "!"));
            //progressText.bind();
            //xpToNextLevel.set(xpToNextLevel.get());
            welcomeDescriptionMessageProperty.bind(TranslationManager.get().t(TKey.DEXPLANATION));
            levelTitleProperty.bind(TranslationManager.get().t(TKey.DLEVELPROGRESSH));
            levelDescriptionProperty.bind(TranslationManager.get().t(TKey.DLEVELDESCRIPTION));
            btnAddLanguageTextProperty.bind(TranslationManager.get().t(TKey.DADDLANGUAGE));
            btnCreateItemsTextProperty.bind(TranslationManager.get().t(TKey.DCREATEITEMS));
            languageProgressTitleProperty.bind(TranslationManager.get().t(TKey.DLANGUAGEPTITLE));
            languageProgressDescriptionProperty.bind(TranslationManager.get().t(TKey.DLANGUAGEPDESCRIPTION));

            //currentLevel.bind(Bindings.format("%s %d", TranslationManager.get().t(TKey.DLEVELPROGRESSH), level));
            currentLevel.bind(Bindings.concat(TranslationManager.get().t(TKey.DLEVELPROGRESSH) , " " , level));

        });
    }

    // Getters
    public StringProperty welcomeMessageProperty() { return welcomeMessage; }
    public StringProperty currentLevelProperty()   { return currentLevel;   }
    public DoubleProperty currentProgressProperty(){ return progressValue; }
    public StringProperty progressTextProperty()  { return progressText;  }
    public StringProperty xpToNextLevelProperty() { return xpToNextLevel; }
    public ObservableList<SimpleStat> getStatList()          { return statList;          }
    public ObservableList<UserLanguage> getUserLanguages()   { return userLanguages;     }
    public ObservableList<Language> getAvailableLanguages()  { return availableLanguages;}

    // Extra for translation
    public StringProperty welcomeDescriptionMessageProperty = new SimpleStringProperty();
    public StringProperty levelTitleProperty = new SimpleStringProperty();
    public StringProperty levelDescriptionProperty = new SimpleStringProperty();
    public StringProperty btnAddLanguageTextProperty = new SimpleStringProperty();
    public StringProperty btnCreateItemsTextProperty = new SimpleStringProperty();
    public StringProperty languageProgressTitleProperty = new SimpleStringProperty();
    public StringProperty languageProgressDescriptionProperty = new SimpleStringProperty();

//    @Override
//    public void destroy() {
//        executor.shutdownNow();
//    }

}

