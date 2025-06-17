package com.final_app.services;

import com.final_app.db.DatabaseManager;
import com.final_app.events.EventBus;
import com.final_app.events.UserChangeEvent;
import com.final_app.factories.RepositoryFactory;
import com.final_app.models.User;
import com.final_app.tools.SessionManager;
import com.final_app.viewmodels.RootViewModel;
import com.final_app.views.pages.LoginView;
import com.final_app.views.pages.RootView;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.SQLException;

/**
 * Central service class to manage application state and coordinate other services
 */
public class AppService {
    // Singleton pattern
    private static AppService instance;

    // Services
    private final UserService userService = new UserService();
    private final LanguageService languageService = new LanguageService();
    private final ConversationService conversationService = new ConversationService();
    private final SpeakingTestService speakingTestService = new SpeakingTestService();
    private final DataService dataService = new DataService();
    private final EvaluationService evaluationService = new EvaluationService();
    private final ScenarioService scenarioService = new ScenarioService();
    private final XpService xpService = new XpService();
    private final DataSynchronizeService dataSynchronizeService = new DataSynchronizeService();

    // Application state
    private User currentUser;
    private boolean dbInitialized = false;
    private boolean authenticated = false;

    private static int timesSynchronizedForUser = 0;

    private AppService() {
        // Private constructor for singleton
    }

    public static synchronized AppService getInstance() {
        if (instance == null) {
            instance = new AppService();
        }
        return instance;
    }

    public static int getTimesSynchronizedForUser() {
        return timesSynchronizedForUser;
    }
    public static void setTimesSynchronizedForUser(int timesSynchronized) {
        AppService.timesSynchronizedForUser = timesSynchronized;
    }

    /**
     * Initialize the application
     */
    public void initialize() throws SQLException {
        // Initialize database
        DatabaseManager.getInstance();

        // Initialize default data if needed
        dataService.initializeDefaultData();

        dbInitialized = true;
    }

    /**
     * Login a user
     * Returns the authenticated user or null if authentication fails
     */
    public User login(String email, String password) throws SQLException {
        if (!dbInitialized) {
            initialize();
        }

        currentUser = userService.authenticateUser(email, password);
        authenticated = (currentUser != null);

        if (authenticated) {
            // Update login streak
            userService.checkAndUpdateDailyStreak(currentUser.getId());

            // Fire user change event
            UserChangeEvent event = new UserChangeEvent(currentUser, UserChangeEvent.LOGIN);
            EventBus.getInstance().post(event);
        }

        return currentUser;
    }

    /**
     * Register a new user
     * Returns the newly created user
     */
    public User register(String userName, String email, String password) throws SQLException {
        if (!dbInitialized) {
            initialize();
        }

        currentUser = userService.registerUser(userName, email, password);
        authenticated = (currentUser != null);

        if (authenticated && currentUser != null) {
            // Fire user change event for login
            UserChangeEvent event = new UserChangeEvent(currentUser, UserChangeEvent.LOGIN);
            EventBus.getInstance().post(event);
        }

        return currentUser;
    }

    /**
     * Logout the current user
     */
    public void logout() {
        User oldUser = currentUser;
        currentUser = null;
        authenticated = false;
        dataSynchronizeService.synchronizeDB(oldUser, DataSynchronizeService.SyncType.LOCAL_TO_ONLINE);

        // Fire user change event for logout
        if (oldUser != null) {
            UserChangeEvent event = new UserChangeEvent(null, UserChangeEvent.LOGOUT);

            this.currentUser = null;
            EventBus.getInstance().post(event);
            SessionManager.getInstance().clearSession();
            RootViewModel.getInstance().getNavigationService().navigateTo(LoginView.class);
        }
        setTimesSynchronizedForUser(0);
        if(Dotenv.load().get("DB_MODE").equals("ONLINE")) RepositoryFactory.getInstance().changeToOnline();
    }

    /**
     * Get the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is authenticated
     */
    public boolean isAuthenticated() {
        return authenticated && currentUser != null;
    }

    /**
     * Get the user service
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * Get the language service
     */
    public LanguageService getLanguageService() {
        return languageService;
    }

    /**
     * Get the conversation service
     */
    public ConversationService getConversationService() {
        return conversationService;
    }

    /**
     * Get the speaking test service
     */
    public SpeakingTestService getSpeakingTestService() {
        return speakingTestService;
    }

    /**
     * Get the data service
     */
    public DataService getDataService() {
        return dataService;
    }

    /**
     * Get the scenario service
     */
    public ScenarioService getScenarioService() {
        return scenarioService;
    }

    /**
     * Get the evaluation service
     */
    public EvaluationService getEvaluationService() {
        return evaluationService;
    }

    /**
     * Get the xp service
     */
    public XpService getXpService() {
        return xpService;
    }

    /**
     * Get the data sychronization service
     */
    public DataSynchronizeService getDataSynchronizeService() {return dataSynchronizeService;}
}