package com.final_app.repositories.firebase;

import com.final_app.interfaces.IUserSpeakingTestRepository;
import com.final_app.models.UserSpeakingTest;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBUserSpeakingTestRepository implements IUserSpeakingTestRepository {

    private static final Logger log = LoggerFactory.getLogger(FBUserSpeakingTestRepository.class);
    private static final String COLLECTION_NAME = "userSpeakingTests";
    private final CollectionReference userTestCollection;

    private static FBUserSpeakingTestRepository instance = null;

    public FBUserSpeakingTestRepository() {
        if(instance == null){
            instance = this;
        }
        Firestore db = FirebaseManager.getDb();
        this.userTestCollection = db.collection(COLLECTION_NAME);
    }


    public static IUserSpeakingTestRepository getInstance() {
        if(instance == null){
            return new FBUserSpeakingTestRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addUserSpeakingTest(UserSpeakingTest userSpeakingTest) {
        String id = (userSpeakingTest.getId() == null || userSpeakingTest.getId().isEmpty()) ?
                userTestCollection.document().getId() : userSpeakingTest.getId();
        userSpeakingTest.setId(id);
        ApiFuture<WriteResult> future = userTestCollection.document(id).set(userSpeakingTest);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding user speaking test {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateUserSpeakingTest(UserSpeakingTest userSpeakingTest) {
        if (userSpeakingTest.getId() == null || userSpeakingTest.getId().isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("UserSpeakingTest ID missing for update."));
        }
        ApiFuture<WriteResult> future = userTestCollection.document(userSpeakingTest.getId()).set(userSpeakingTest, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating user speaking test {}", userSpeakingTest.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<UserSpeakingTest>> getUserSpeakingTestById(String id) {
        ApiFuture<DocumentSnapshot> future = userTestCollection.document(id).get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(snapshot -> {
                    if (snapshot.exists()) {
                        UserSpeakingTest ut = snapshot.toObject(UserSpeakingTest.class);
                        if (ut != null) ut.setId(snapshot.getId());
                        return Optional.ofNullable(ut);
                    }
                    return Optional.<UserSpeakingTest>empty();
                })
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting user speaking test by ID {}", id, ex); });
    }

    @Override
    public CompletableFuture<Optional<UserSpeakingTest>> getUserSpeakingTestByUserIdAndTestId(String userId, String testId) {
        ApiFuture<QuerySnapshot> future = userTestCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("testId", testId) // Adjust field names if needed
                .limit(1)
                .get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                        UserSpeakingTest ut = snapshot.toObject(UserSpeakingTest.class);
                        if (ut != null) ut.setId(snapshot.getId());
                        return Optional.ofNullable(ut);
                    }
                    return Optional.<UserSpeakingTest>empty();
                })
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting user speaking test for user ID {} and test ID {}", userId, testId, ex);
                });
    }

    @Override
    public CompletableFuture<Void> deleteUserSpeakingTestById(String id) {
        // Consider implications: Delete associated responses? Service layer responsibility?
        ApiFuture<WriteResult> future = userTestCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting user speaking test {}", id, ex); });
    }

    @Override
    public CompletableFuture<Iterable<UserSpeakingTest>> getAllUserSpeakingTestsFromUser(String userId) {
        ApiFuture<QuerySnapshot> future = userTestCollection
                .whereEqualTo("userId", userId) // Adjust field name if needed
                .orderBy("startTime", Query.Direction.DESCENDING) // Example order
                .get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<UserSpeakingTest>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    UserSpeakingTest ut = snapshot.toObject(UserSpeakingTest.class);
                                    if (ut != null) ut.setId(snapshot.getId());
                                    return ut;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting user speaking tests for user ID {}", userId, ex);
                });
    }
}