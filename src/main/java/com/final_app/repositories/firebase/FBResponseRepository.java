package com.final_app.repositories.firebase;

import com.final_app.interfaces.IResponseRepository;
import com.final_app.models.UserSpeakingTestResponse;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBResponseRepository implements IResponseRepository {

    private static final Logger log = LoggerFactory.getLogger(FBResponseRepository.class);
    // Naming convention might map this to UserSpeakingTestResponse
    private static final String COLLECTION_NAME = "userSpeakingTestResponses";
    private final CollectionReference responseCollection;

    private static FBResponseRepository instance = null;

    public FBResponseRepository() {
        if(instance == null){
            instance = this;
        }
        Firestore db = FirebaseManager.getDb();
        this.responseCollection = db.collection(COLLECTION_NAME);
    }


    public static IResponseRepository getInstance() {
        if(instance == null){
            return new FBResponseRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addResponse(UserSpeakingTestResponse response) {
        String id = (response.getId() == null || response.getId().isEmpty()) ?
                responseCollection.document().getId() : response.getId();
        response.setId(id);
        ApiFuture<WriteResult> future = responseCollection.document(id).set(response);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding response {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateResponse(UserSpeakingTestResponse response) {
        if (response.getId() == null || response.getId().isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Response ID missing for update."));
        }
        ApiFuture<WriteResult> future = responseCollection.document(response.getId()).set(response, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating response {}", response.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<UserSpeakingTestResponse>> getResponseById(String id) {
        ApiFuture<DocumentSnapshot> future = responseCollection.document(id).get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(snapshot -> {
                    if (snapshot.exists()) {
                        UserSpeakingTestResponse r = snapshot.toObject(UserSpeakingTestResponse.class);
                        if (r != null) r.setId(snapshot.getId());
                        return Optional.ofNullable(r);
                    }
                    return Optional.<UserSpeakingTestResponse>empty();
                })
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting response by ID {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> deleteResponseById(String id) {
        ApiFuture<WriteResult> future = responseCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting response {}", id, ex); });
    }

    @Override
    public CompletableFuture<Iterable<UserSpeakingTestResponse>> getAllResponses() {
        ApiFuture<QuerySnapshot> future = responseCollection.orderBy("timestamp").get(); // Example order
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<UserSpeakingTestResponse>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    UserSpeakingTestResponse r = snapshot.toObject(UserSpeakingTestResponse.class);
                                    if (r != null) r.setId(snapshot.getId());
                                    return r;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting all responses", ex); });
    }

    @Override
    public CompletableFuture<Iterable<UserSpeakingTestResponse>> getAllResponsesFromUserTest(String userTestId) {
        ApiFuture<QuerySnapshot> future = responseCollection
                .whereEqualTo("userTestId", userTestId) // Adjust field name if needed
                .orderBy("timestamp")
                .get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<UserSpeakingTestResponse>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    UserSpeakingTestResponse r = snapshot.toObject(UserSpeakingTestResponse.class);
                                    if (r != null) r.setId(snapshot.getId());
                                    return r;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting responses for UserTest ID {}", userTestId, ex);
                });
    }
}