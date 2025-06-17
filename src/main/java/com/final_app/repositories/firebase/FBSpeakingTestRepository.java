package com.final_app.repositories.firebase;

import com.final_app.interfaces.ISpeakingTestRepository;
import com.final_app.models.SpeakingTest;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBSpeakingTestRepository implements ISpeakingTestRepository {

    private static final Logger log = LoggerFactory.getLogger(FBSpeakingTestRepository.class);
    private static final String COLLECTION_NAME = "speakingTests";
    private final CollectionReference testCollection;

    private static FBSpeakingTestRepository instance = null;

    public FBSpeakingTestRepository() {
        if(instance == null){
            instance = this;
        }
        Firestore db = FirebaseManager.getDb();
        this.testCollection = db.collection(COLLECTION_NAME);
    }


    public static ISpeakingTestRepository getInstance() {
        if(instance == null){
            return new FBSpeakingTestRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addSpeakingTest(SpeakingTest speakingTest) {
        String id = (speakingTest.getId() == null || speakingTest.getId().isEmpty()) ?
                testCollection.document().getId() : speakingTest.getId();
        speakingTest.setId(id);
        ApiFuture<WriteResult> future = testCollection.document(id).set(speakingTest);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding speaking test {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateSpeakingTest(SpeakingTest speakingTest) {
        if (speakingTest.getId() == null || speakingTest.getId().isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("SpeakingTest ID missing for update."));
        }
        ApiFuture<WriteResult> future = testCollection.document(speakingTest.getId()).set(speakingTest, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating speaking test {}", speakingTest.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<SpeakingTest>> getSpeakingTestById(String id) {
        ApiFuture<DocumentSnapshot> future = testCollection.document(id).get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(snapshot -> {
                    if (snapshot.exists()) {
                        SpeakingTest test = snapshot.toObject(SpeakingTest.class);
                        if (test != null) test.setId(snapshot.getId());
                        return Optional.ofNullable(test);
                    }
                    return Optional.<SpeakingTest>empty();
                })
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting speaking test by ID {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> deleteSpeakingTestById(String id) {
        ApiFuture<WriteResult> future = testCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting speaking test {}", id, ex); });
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTest>> getAllSpeakingTests() {
        ApiFuture<QuerySnapshot> future = testCollection.orderBy("title").get(); // Example order
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<SpeakingTest>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    SpeakingTest test = snapshot.toObject(SpeakingTest.class);
                                    if (test != null) test.setId(snapshot.getId());
                                    return test;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting all speaking tests", ex); });
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTest>> getAllSpeakingTestsFromLanguage(String languageId) {
        ApiFuture<QuerySnapshot> future = testCollection
                .whereEqualTo("languageId", languageId) // Adjust field name if needed
                .orderBy("title")
                .get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<SpeakingTest>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    SpeakingTest test = snapshot.toObject(SpeakingTest.class);
                                    if (test != null) test.setId(snapshot.getId());
                                    return test;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting speaking tests for language ID {}", languageId, ex);
                });
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTest>> getAllSpeakingTestsFromLevel(String levelId) {
        ApiFuture<QuerySnapshot> future = testCollection
                .whereEqualTo("levelId", levelId) // Adjust field name if needed
                .orderBy("title")
                .get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<SpeakingTest>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    SpeakingTest test = snapshot.toObject(SpeakingTest.class);
                                    if (test != null) test.setId(snapshot.getId());
                                    return test;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting speaking tests for level ID {}", levelId, ex);
                });
    }
}