package com.final_app.interfaces;

import com.final_app.models.SpeakingTest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ISpeakingTestRepository {
    CompletableFuture<Void> addSpeakingTest(SpeakingTest speakingTest);
    CompletableFuture<Void> updateSpeakingTest(SpeakingTest speakingTest);

    CompletableFuture<Optional<SpeakingTest>> getSpeakingTestById(String id);

    CompletableFuture<Void> deleteSpeakingTestById(String id);

    CompletableFuture<Iterable<SpeakingTest>> getAllSpeakingTests();
    CompletableFuture<Iterable<SpeakingTest>> getAllSpeakingTestsFromLanguage(String languageId);
    CompletableFuture<Iterable<SpeakingTest>> getAllSpeakingTestsFromLevel(String levelId);
}
