package com.final_app.repositories.local;

import com.final_app.db.dao.SettingsDAO;
import com.final_app.factories.RepositoryFactory;
import com.final_app.interfaces.ISettingsRepository;
import com.final_app.models.Language;
import com.final_app.models.Settings;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalSettingsRepository implements ISettingsRepository {
    private final SettingsDAO settingsDAO = new SettingsDAO();

    private static LocalSettingsRepository instance;
    public static LocalSettingsRepository getInstance() {
        if (instance == null) {
            instance = new LocalSettingsRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> saveSettings(Settings settings) {
        try {
            if(settings.getId() == null) {
                settingsDAO.insert(settings);
            }
            else{
                Settings existing = settingsDAO.findById(settings.getId());
                if (existing != null) {
                    settingsDAO.update(settings);
                }
                else{
                    settingsDAO.insert(settings);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<Settings>> getSettingsFromUser(String userId) {
        try {
            if(userId == null || userId.isEmpty()) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            Settings userSettings = settingsDAO.findByUserId(userId);
            if(userSettings != null) {
                return CompletableFuture.completedFuture(Optional.of(userSettings));
            }else{
                Settings newSettings = new Settings();
                newSettings.setUserId(userId);
                return RepositoryFactory.getLanguageRepository()
                        .getAllLanguages()
                        .thenCompose(languages -> {
                            List<Language> languageList = (List<Language>) languages;
                            if(!languageList.isEmpty()) {
                                newSettings.setLanguage(languageList.stream().filter(language -> language.getName().toLowerCase().contains("english")).findFirst().orElse(null));
                                newSettings.setSelectedLanguages("");
                                return saveSettings(newSettings)
                                        .thenCompose(_->{
                                            return CompletableFuture.completedFuture(Optional.of(newSettings));
                                        });
                            }else{
                                return CompletableFuture.completedFuture(Optional.empty());
                            }
                        });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }
}
