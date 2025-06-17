package com.final_app.services;

import com.final_app.factories.RepositoryFactory;
import com.final_app.interfaces.ILanguageRepository;
import com.final_app.interfaces.IUserLanguageRepository;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.LanguageLevelSystem;
import com.final_app.models.UserLanguage;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service class for language-related operations
 */
public class LanguageService {
    /**
     * Add a new language to the system
     */
    public Language addLanguage(String systemId, String name, String iso, String color, long maxXp) throws SQLException, ExecutionException, InterruptedException {
        Language language = new Language(systemId, name, iso, color, maxXp);
        RepositoryFactory.getLanguageRepository().addLanguage(language).get(); // Wait for completion
        return language;
    }

    /**
     * Get all available languages
     */
    public List<Language> getAllLanguages() throws SQLException, ExecutionException, InterruptedException {
        Iterable<Language> iterableLanguages = RepositoryFactory.getLanguageRepository().getAllLanguages().get();
        return StreamSupport.stream(iterableLanguages.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get a language by ID
     */
    public Language getLanguageById(String id) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getLanguageRepository().getLanguageById(id).get().orElseThrow();
    }

    /**
     * Get a language by name
     */
    public Language getLanguageByName(String name) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getLanguageRepository().getLanguageByName(name).get().orElseThrow();
    }

    /**
     * Update an existing language
     */
    public void updateLanguage(Language language) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getLanguageRepository().updateLanguage(language).get();
    }

    /**
     * Delete a language
     */
    public void deleteLanguage(String id) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getLanguageRepository().deleteLanguage(id).get();
    }

    /**
     * Get all language levels
     */
    public List<LanguageLevel> getAllLanguageLevels() throws SQLException, ExecutionException, InterruptedException {
        Iterable<LanguageLevel> iterableLevels = RepositoryFactory.getLanguageRepository().getAllLanguageLevels().get();
        return StreamSupport.stream(iterableLevels.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get a language level by ID
     */
    public LanguageLevel getLanguageLevelById(String id) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getLanguageRepository().getLanguageLevelById(id).get().orElseThrow();
    }

    /**
     * Add a language to a user's learning list
     */
    public CompletableFuture<Optional<UserLanguage>> addUserLanguage(String userId, String languageId, String levelId) throws SQLException, ExecutionException, InterruptedException {
        // Check if user already has this language
        UserLanguage existing = RepositoryFactory.getUserLanguageRepository().getUserLanguageByLanguageIdAndUserId(languageId, userId).get().orElse(null);
        if (existing != null) {
            return CompletableFuture.completedFuture(null); // User already has this language
        }
        String tempLevelId;
        if (levelId == null) {
            tempLevelId = StreamSupport.stream(RepositoryFactory.getLanguageRepository().getAllLanguageLevels().get().spliterator(), false)
                    .filter(lngLvl -> {
                        try {
                            return lngLvl.getSystemId().equals(RepositoryFactory.getLanguageRepository().getLanguageById(languageId).get().orElse(null).getSystemId());
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .findFirst()
                    .orElseThrow()
                    .getId();
        } else {
            tempLevelId = levelId;
        }
        LanguageLevel level = RepositoryFactory.getLanguageRepository().getLanguageLevelById(tempLevelId).get().orElse(null);

        final XpService xpService = AppService.getInstance().getXpService();
        // Add new user language with 0 XP
        UserLanguage userLanguage = new UserLanguage(userId, languageId, tempLevelId,(level != null) ? xpService.calculateXpForLevel(level.getLevelThreshold()) : 0 );
        RepositoryFactory.getUserLanguageRepository().addUserLanguage(userLanguage).join();
        return CompletableFuture.completedFuture(Optional.of(userLanguage));
    }

    /**
     * Get all languages a user is learning
     */
    public List<UserLanguage> getUserLanguages(String userId) throws SQLException, ExecutionException, InterruptedException {
        Iterable<UserLanguage> iterableUserLanguages = RepositoryFactory.getUserLanguageRepository().getAllUserLanguagesFromUser(userId).get();
        return StreamSupport.stream(iterableUserLanguages.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get specific user language
     */
    public UserLanguage getUserLanguage(String userId, String languageId) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getUserLanguageRepository().getUserLanguageByLanguageIdAndUserId(languageId, userId).get().orElse(null);
    }

    /**
     * Add XP to a user's language
     */
    public void addLanguageXp(String userLanguageId, long xpAmount) throws SQLException, ExecutionException, InterruptedException {
        UserLanguage userLanguage = RepositoryFactory.getUserLanguageRepository().getUserLanguageById(userLanguageId).get().orElse(null);
        if (userLanguage != null) {
            userLanguage.setXp(userLanguage.getXp() + xpAmount);

            // Update level based on XP if needed
            LanguageLevel currentLevel = RepositoryFactory.getLanguageRepository().getLanguageLevelById(userLanguage.getLevelId()).get().orElse(null);
            LanguageLevelSystem system = RepositoryFactory.getLanguageRepository().getLanguageLevelSystemById(currentLevel.getSystemId()).get().orElse(null);
            int nextLevelValue = currentLevel.getValue() + 1;

            // Simple level up logic based on XP thresholds
            if (nextLevelValue <= 3 && userLanguage.getXp() >= calculateXpThreshold(nextLevelValue)) {
                // Find next level
                LanguageLevel nextLevel = system.getLevels().stream().filter(lvl -> lvl.getValue() == nextLevelValue).findFirst().orElse(null);
                if (nextLevel != null) {
                    userLanguage.setLevelId(nextLevel.getId());
                }
            }
            RepositoryFactory.getUserLanguageRepository().updateUserLanguage(userLanguage).get();
        }
    }

    /**
     * Remove a language from a user's learning list
     */
    public void removeUserLanguage(String userLanguageId) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getUserLanguageRepository().deleteUserLanguageById(userLanguageId).get();
    }

    /**
     * Calculate XP threshold for a specific level (simple formula, adjust as needed)
     */
    private long calculateXpThreshold(int level) {
        // Example: 500, 2000, 5000 XP for levels 2, 3, 4 respectively
        return (long) Math.pow(level * 500, 1.2);
    }
}