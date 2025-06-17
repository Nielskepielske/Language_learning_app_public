package com.final_app.interfaces;

import com.final_app.models.SpeakingTestQuestion;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IQuestionRepository {
    CompletableFuture<Void> addQuestion(SpeakingTestQuestion question);
    CompletableFuture<Void> updateQuestion(SpeakingTestQuestion question);
    CompletableFuture<Optional<SpeakingTestQuestion>> getQuestionById(String id);
    CompletableFuture<Void> deleteQuestionById(String id);
    CompletableFuture<Iterable<SpeakingTestQuestion>> getAllQuestions();
    CompletableFuture<Iterable<SpeakingTestQuestion>> getAllQuestionsFromTest(String testId);
}
