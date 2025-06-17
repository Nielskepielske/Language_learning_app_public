package com.final_app.interfaces;

import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.LanguageLevelSystem;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ILanguageRepository {
    CompletableFuture<Void> addLanguage(Language language);
    CompletableFuture<Void> updateLanguage(Language language);
    CompletableFuture<Void> deleteLanguage(String id);

    CompletableFuture<Optional<Language>> getLanguage(Language language);
    CompletableFuture<Optional<Language>> getLanguageById(String id);
    CompletableFuture<Optional<Language>> getLanguageByName(String name);

    CompletableFuture<Iterable<Language>> getAllLanguages();

    CompletableFuture<Void> addLanguageLevel(LanguageLevel languageLevel);
    CompletableFuture<Void> updateLanguageLevel(LanguageLevel languageLevel);
    CompletableFuture<Void> deleteLanguageLevel(LanguageLevel languageLevel);
    CompletableFuture<Optional<LanguageLevel>> getLanguageLevelById(String id);
    CompletableFuture<Optional<LanguageLevel>> getLanguageLevelByName(String name);
    CompletableFuture<List<LanguageLevel>> getAllLanguageLevels();

    CompletableFuture<Void> addLanguageSystem(LanguageLevelSystem languageLevelSystem);
    CompletableFuture<Void> updateLanguageSystem(LanguageLevelSystem languageLevelSystem);
    CompletableFuture<Void> deleteLanguageSystem(LanguageLevelSystem languageLevelSystem);

    CompletableFuture<Optional<LanguageLevelSystem>> getLanguageLevelSystemById(String id);
    CompletableFuture<Optional<LanguageLevelSystem>> getLanguageLevelSystemByName(String name);

    CompletableFuture<Iterable<LanguageLevelSystem>> getAllLanguageSystems();
}
