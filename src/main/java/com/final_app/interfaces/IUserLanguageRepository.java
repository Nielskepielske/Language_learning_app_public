package com.final_app.interfaces;

import com.final_app.models.UserLanguage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IUserLanguageRepository {
    CompletableFuture<Void> addUserLanguage(UserLanguage userLanguage);
    CompletableFuture<Void> updateUserLanguage(UserLanguage userLanguage);
    CompletableFuture<Optional<UserLanguage>> getUserLanguageById(String id);
    CompletableFuture<Optional<UserLanguage>> getUserLanguageByLanguageIdAndUserId(String languageId, String userId);
    CompletableFuture<Void> deleteUserLanguageById(String id);
    CompletableFuture<Iterable<UserLanguage>> getAllUserLanguagesFromUser(String userId);
}
