package com.final_app.interfaces;

import com.final_app.models.UserSpeakingTest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IUserSpeakingTestRepository {
    CompletableFuture<Void> addUserSpeakingTest(UserSpeakingTest userSpeakingTest);
    CompletableFuture<Void> updateUserSpeakingTest(UserSpeakingTest userSpeakingTest);
    CompletableFuture<Optional<UserSpeakingTest>> getUserSpeakingTestById(String id);
    CompletableFuture<Optional<UserSpeakingTest>> getUserSpeakingTestByUserIdAndTestId(String userId, String testId);
    CompletableFuture<Void> deleteUserSpeakingTestById(String id);
    CompletableFuture<Iterable<UserSpeakingTest>> getAllUserSpeakingTestsFromUser(String userId);
}
