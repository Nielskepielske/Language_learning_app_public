package com.final_app.interfaces;

import com.final_app.models.UserSpeakingTestResponse;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IResponseRepository {
    CompletableFuture<Void> addResponse(UserSpeakingTestResponse response);
    CompletableFuture<Void> updateResponse(UserSpeakingTestResponse response);
    CompletableFuture<Optional<UserSpeakingTestResponse>> getResponseById(String id);
    CompletableFuture<Void> deleteResponseById(String id);
    CompletableFuture<Iterable<UserSpeakingTestResponse>> getAllResponses();
    CompletableFuture<Iterable<UserSpeakingTestResponse>> getAllResponsesFromUserTest(String userTestId);
}
