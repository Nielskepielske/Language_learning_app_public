package com.final_app.repositories.firebase;

import com.final_app.interfaces.IEvaluationRepository;
import com.final_app.models.Evaluation;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils; // Import the helper
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport; // For Iterable stream

public class FBEvaluationRepository implements IEvaluationRepository {

    private static final Logger log = LoggerFactory.getLogger(FBEvaluationRepository.class);
    private static final String COLLECTION_NAME = "evaluations";
    private final CollectionReference evaluationCollection;

    private static FBEvaluationRepository instance = null;

    public FBEvaluationRepository() {
        if(instance == null){
            instance = this;
        }
        Firestore db = FirebaseManager.getDb(); // Assuming FirebaseManager.getDb() exists
        this.evaluationCollection = db.collection(COLLECTION_NAME);
    }

    public static IEvaluationRepository getInstance() {
        if(instance == null){
            return new FBEvaluationRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addEvaluation(Evaluation evaluation) {
        // Generate ID or use existing if set
        String id = (evaluation.getId() == null || evaluation.getId().isEmpty()) ?
                evaluationCollection.document().getId() : evaluation.getId();
        evaluation.setId(id); // Ensure ID is set on the object
        ApiFuture<WriteResult> future = evaluationCollection.document(id).set(evaluation);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error adding evaluation {}", id, ex);
                    else log.debug("Added evaluation {}", id);
                });
    }

    @Override
    public CompletableFuture<Void> updateEvaluation(Evaluation evaluation) {
        if (evaluation.getId() == null || evaluation.getId().isEmpty()) {
            log.error("Evaluation ID is missing for update operation.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("Evaluation ID cannot be null for update."));
        }
        ApiFuture<WriteResult> future = evaluationCollection.document(evaluation.getId()).set(evaluation, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error updating evaluation {}", evaluation.getId(), ex);
                    else log.debug("Updated evaluation {}", evaluation.getId());
                });
    }

    @Override
    public CompletableFuture<Optional<Evaluation>> getEvaluationById(String id) {
        ApiFuture<DocumentSnapshot> future = evaluationCollection.document(id).get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(snapshot -> {
                    if (snapshot.exists()) {
                        Evaluation eval = snapshot.toObject(Evaluation.class);
                        if (eval != null) {
                            eval.setId(snapshot.getId()); // Ensure ID is set
                            return Optional.of(eval);
                        }
                    }
                    return Optional.<Evaluation>empty();
                })
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting evaluation by ID {}", id, ex);
                });
    }

    @Override
    public CompletableFuture<Optional<Evaluation>> getEvaluationByUserConversationId(String userConversationId) {
        ApiFuture<QuerySnapshot> future = evaluationCollection
                .whereEqualTo("userConversationId", userConversationId) // Adjust field name if different
                .limit(1)
                .get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                        Evaluation eval = snapshot.toObject(Evaluation.class);
                        if (eval != null) {
                            eval.setId(snapshot.getId()); // Ensure ID is set
                            return Optional.of(eval);
                        }
                    }
                    return Optional.<Evaluation>empty();
                })
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting evaluation by UserConversationId {}", userConversationId, ex);
                });
    }


    @Override
    public CompletableFuture<Void> deleteEvaluationById(String id) {
        ApiFuture<WriteResult> future = evaluationCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error deleting evaluation {}", id, ex);
                    else log.debug("Deleted evaluation {}", id);
                });
    }

    @Override
    public CompletableFuture<Iterable<Evaluation>> getAllEvaluations() {
        ApiFuture<QuerySnapshot> future = evaluationCollection.get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<Evaluation>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    Evaluation eval = snapshot.toObject(Evaluation.class);
                                    if (eval != null) {
                                        eval.setId(snapshot.getId());
                                    }
                                    return eval;
                                })
                                .filter(java.util.Objects::nonNull) // Filter out potential nulls if mapping fails
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting all evaluations", ex);
                });
    }
}