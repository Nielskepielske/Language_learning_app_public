package com.final_app.repositories.firebase;

import com.final_app.interfaces.IQuestionRepository;
import com.final_app.models.SpeakingTestQuestion;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBQuestionRepository implements IQuestionRepository {

    private static final Logger log = LoggerFactory.getLogger(FBQuestionRepository.class);
    // Naming convention might map this to SpeakingTestQuestion
    private static final String COLLECTION_NAME = "speakingTestQuestions";
    private final CollectionReference questionCollection;

    private static FBQuestionRepository instance = null;

    public FBQuestionRepository() {
        if(instance == null){
            instance = this;
        }
        Firestore db = FirebaseManager.getDb();
        this.questionCollection = db.collection(COLLECTION_NAME);
    }

    public static IQuestionRepository getInstance() {
        if(instance == null){
            return new FBQuestionRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addQuestion(SpeakingTestQuestion question) {
        String id = (question.getId() == null || question.getId().isEmpty()) ?
                questionCollection.document().getId() : question.getId();
        question.setId(id);
        ApiFuture<WriteResult> future = questionCollection.document(id).set(question);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding question {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateQuestion(SpeakingTestQuestion question) {
        if (question.getId() == null || question.getId().isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Question ID missing for update."));
        }
        ApiFuture<WriteResult> future = questionCollection.document(question.getId()).set(question, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating question {}", question.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<SpeakingTestQuestion>> getQuestionById(String id) {
        ApiFuture<DocumentSnapshot> future = questionCollection.document(id).get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(snapshot -> {
                    if (snapshot.exists()) {
                        SpeakingTestQuestion q = snapshot.toObject(SpeakingTestQuestion.class);
                        if (q != null) q.setId(snapshot.getId());
                        return Optional.ofNullable(q);
                    }
                    return Optional.<SpeakingTestQuestion>empty();
                })
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting question by ID {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> deleteQuestionById(String id) {
        ApiFuture<WriteResult> future = questionCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting question {}", id, ex); });
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTestQuestion>> getAllQuestions() {
        ApiFuture<QuerySnapshot> future = questionCollection.orderBy("order").get(); // Example ordering
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<SpeakingTestQuestion>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    SpeakingTestQuestion q = snapshot.toObject(SpeakingTestQuestion.class);
                                    if (q != null) q.setId(snapshot.getId());
                                    return q;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting all questions", ex); });
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTestQuestion>> getAllQuestionsFromTest(String testId) {
        ApiFuture<QuerySnapshot> future = questionCollection
                .whereEqualTo("testId", testId) // Adjust field name if needed
                .orderBy("order") // Order questions within a test
                .get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<SpeakingTestQuestion>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    SpeakingTestQuestion q = snapshot.toObject(SpeakingTestQuestion.class);
                                    if (q != null) q.setId(snapshot.getId());
                                    return q;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting questions for test ID {}", testId, ex);
                });
    }
}