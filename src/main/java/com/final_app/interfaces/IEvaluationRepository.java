package com.final_app.interfaces;

import com.final_app.models.Evaluation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IEvaluationRepository {
    CompletableFuture<Void> addEvaluation(Evaluation evaluation);
    CompletableFuture<Void> updateEvaluation(Evaluation evaluation);
    CompletableFuture<Optional<Evaluation>> getEvaluationById(String id);
    CompletableFuture<Optional<Evaluation>> getEvaluationByUserConversationId(String userConversationId);
    CompletableFuture<Void> deleteEvaluationById(String id);
    CompletableFuture<Iterable<Evaluation>> getAllEvaluations();
}
