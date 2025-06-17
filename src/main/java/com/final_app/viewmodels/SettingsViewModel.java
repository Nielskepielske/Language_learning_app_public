package com.final_app.viewmodels;

import com.final_app.factories.RepositoryFactory;
import com.final_app.globals.TKey;
import com.final_app.interfaces.IUserRepository;
import com.final_app.models.Language;
import com.final_app.models.Settings;
import com.final_app.models.User;
import com.final_app.services.AppService;
import com.final_app.services.UserService;
import com.final_app.tools.TranslationManager;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsViewModel extends BaseViewModel {
    private final AppService appService = AppService.getInstance();
    private final UserService userService = appService.getUserService();
    private final IUserRepository userRepository = RepositoryFactory.getInstance().getUserRepository();

    private User user;
    private Settings settings;

    public StringProperty email = new SimpleStringProperty();
    public StringProperty username = new SimpleStringProperty();

    private ObservableList<Language> languages = FXCollections.observableArrayList();
    public ObservableList<Language> getLanguages() {
        return languages;
    }

    public User getUser() {
        return user;
    }
    public ObjectProperty<Language> selectedLanguageProperty = new SimpleObjectProperty<>(new Language(null, "en", "English", "#000000", 0));
    public ObservableList<String> selectedLanguages = FXCollections.observableArrayList();
    public Settings getSettings() {
        return settings;
    }

    // Thread pool instead of raw threads
    private static final int THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors());
    // ExecutorService with dynamic pool size for background tasks
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    public void initialize() {
        executor.submit(() -> {
            user = appService.getCurrentUser();
            try {
                userService.getUserSettings(user.getId())
                        .thenAccept(s -> {
                            settings = s;
                            Platform.runLater(()->{
                                email.set(user.getEmail());
                                username.set(user.getUserName());
                            });
                        });
                Platform.runLater(()->{
                    try {
                        languages.setAll(appService.getLanguageService().getAllLanguages());
                        if (settings != null) {
                            selectedLanguageProperty.set(settings.getLanguage());
                            selectedLanguages.setAll(settings.getSelectedLanguages());
                        } else {
                            selectedLanguageProperty.set(languages.get(0));
                        }
                        reloadSystemText();
                    } catch (SQLException | ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void reloadSystemText(){
        Platform.runLater(()->{
//            lblTitleProperty.set(TranslationManager.get().t(TKey.SETTINGS));
//            lblDescriptionProperty.set(TranslationManager.get().t(TKey.SETDESCRIPTION));
//            lblAccountTitleProperty.set(TranslationManager.get().t(TKey.SETACCOUNTINFO));
//            lblAccountDescriptionProperty.set(TranslationManager.get().t(TKey.SETACCOUNTDESCRIPTION));
//            lblAccountUsernameProperty.set(TranslationManager.get().t(TKey.SETACCOUNTUN));
//            lblAccountEmailProperty.set(TranslationManager.get().t(TKey.SETACCOUNTEM));
//            lblLanguageTitleProperty.set(TranslationManager.get().t(TKey.SETLANGUAGETITLE));
//            lblLanguageDescriptionProperty.set(TranslationManager.get().t(TKey.SETLANGUAGEDES));
//            lblLanguageSubTitleProperty.set(TranslationManager.get().t(TKey.SETLANGUAGESUB));
//            btnTextSaveProperty.set(TranslationManager.get().t(TKey.SETSAVE));
        });
    }

    public void saveUser(String email, String username){
        user.setEmail(email);
        user.setUserName(username);
        userRepository.updateUser(user);
    }
    public void saveSettings(Settings settings){
        executor.submit(() -> {
            this.settings = settings;
            settings.setSelectedLanguages(selectedLanguages);
            try {
                userService.saveUserSettings(this.settings);

                executor.submit(() -> {
                    try {
                        TranslationManager.get().setLanguage(this.settings.getLanguage());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    reloadSystemText();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void onNavigatedTo() {
        initialize();
    }

    @Override
    public void onNavigatedFrom() {

    }

    // System text properties
    public StringProperty lblTitleProperty = TranslationManager.get().t(TKey.SETTINGS);
    public StringProperty lblDescriptionProperty = TranslationManager.get().t(TKey.SETDESCRIPTION);
    public StringProperty lblAccountTitleProperty = TranslationManager.get().t(TKey.SETACCOUNTINFO);
    public StringProperty lblAccountDescriptionProperty = TranslationManager.get().t(TKey.SETACCOUNTDESCRIPTION);
    public StringProperty lblAccountUsernameProperty = TranslationManager.get().t(TKey.SETACCOUNTUN);
    public StringProperty lblAccountEmailProperty = TranslationManager.get().t(TKey.SETACCOUNTEM);
    public StringProperty lblLanguageTitleProperty = TranslationManager.get().t(TKey.SETLANGUAGETITLE);
    public StringProperty lblLanguageDescriptionProperty = TranslationManager.get().t(TKey.SETLANGUAGEDES);
    public StringProperty lblLanguageSubTitleProperty = TranslationManager.get().t(TKey.SETLANGUAGESUB);
    public StringProperty btnTextSaveProperty = TranslationManager.get().t(TKey.SETSAVE);
}
