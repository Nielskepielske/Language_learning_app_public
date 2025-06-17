package com.final_app.repositories.firebase;

import com.final_app.interfaces.IUserRepository;
import com.final_app.models.User;
import com.final_app.models.UserStats;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils; // Ensure this utility exists
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
// Remove RTDB imports if no longer needed
// import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBUserRepository implements IUserRepository {

    private static final Logger log = LoggerFactory.getLogger(FBUserRepository.class);
    private static final String USER_COLLECTION = "users";
    private static final String STATS_COLLECTION = "userStats"; // Assuming user stats are stored here

    private final Firestore firestoreDb; // Keep Firestore instance if needed elsewhere, or remove if collections are sufficient
    private final CollectionReference userCollection;
    private final CollectionReference statsCollection;

    // Remove RTDB instance
    // private final FirebaseDatabase db = FirebaseManager.getFirebaseDatabase();

    private static FBUserRepository instance = null;

    // Private constructor for Singleton
    public FBUserRepository() {
        this.firestoreDb = FirebaseManager.getDb(); // Get Firestore instance
        this.userCollection = firestoreDb.collection(USER_COLLECTION);
        this.statsCollection = firestoreDb.collection(STATS_COLLECTION);
    }

    public static IUserRepository getInstance() {
        if (instance == null) {
            instance = new FBUserRepository();
        }
        return instance;
    }

    // --- User Methods (Firestore Based) ---

    @Override
    public CompletableFuture<Void> addUser(User user) {
        // Assume ID should be set externally or generated before calling,
        // or use Firestore's auto-generated ID. Let's use Firestore's auto-ID.
        if (user.getId() == null || user.getId().isEmpty()) {
            // Let Firestore generate the ID
            DocumentReference newUserRef = userCollection.document();
            user.setId(newUserRef.getId());
            ApiFuture<WriteResult> future = newUserRef.set(user);
            return FirestoreFutureUtils.toVoidCompletableFuture(future)
                    .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding user {}", user.getId(), ex); });
        } else {
            // Use the provided ID
            ApiFuture<WriteResult> future = userCollection.document(user.getId()).set(user);
            return FirestoreFutureUtils.toVoidCompletableFuture(future)
                    .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding user with provided ID {}", user.getId(), ex); });
        }
    }

    @Override
    public CompletableFuture<Void> updateUser(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            log.warn("Attempted to update user with null or empty ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("User ID missing for update."));
        }
        // Use set with merge to only update provided fields
        ApiFuture<WriteResult> future = userCollection.document(user.getId()).set(user, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating user {}", user.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<User>> getUserById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("getUserById called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<DocumentSnapshot> futureSnapshot = userCollection.document(id).get();
        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        return cfSnapshot.thenApply(snapshot -> {
            if (snapshot.exists()) {
                User user = snapshot.toObject(User.class);
                if (user != null) {
                    user.setId(snapshot.getId()); // Ensure ID is set from snapshot
                    return Optional.of(user);
                } else {
                    log.warn("User document {} exists but failed to map.", id);
                    return Optional.<User>empty();
                }
            } else {
                log.debug("User document {} not found.", id);
                return Optional.<User>empty();
            }
        }).exceptionally(ex -> {
            log.error("Error getting user by ID {}", id, ex);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<User>> getUserByUsername(String username) {
        if (username == null || username.isEmpty()) {
            log.warn("getUserByUsername called with null or empty username.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        // Query Firestore directly instead of fetching all users
        ApiFuture<QuerySnapshot> futureQuery = userCollection.whereEqualTo("userName", username).limit(1).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                User user = snapshot.toObject(User.class);
                if (user != null) {
                    user.setId(snapshot.getId());
                    return Optional.of(user);
                } else {
                    log.warn("User document found for username '{}' but failed to map.", username);
                    return Optional.<User>empty();
                }
            } else {
                log.debug("User document not found for username '{}'.", username);
                return Optional.<User>empty();
            }
        }).exceptionally(ex -> {
            log.error("Error getting user by username '{}'", username, ex);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<User>> getUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            log.warn("getUserByEmail called with null or empty email.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        // Query Firestore directly
        ApiFuture<QuerySnapshot> futureQuery = userCollection.whereEqualTo("email", email).limit(1).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                User user = snapshot.toObject(User.class);
                if (user != null) {
                    user.setId(snapshot.getId());
                    return Optional.of(user);
                } else {
                    log.warn("User document found for email '{}' but failed to map.", email);
                    return Optional.<User>empty();
                }
            } else {
                log.debug("User document not found for email '{}'.", email);
                return Optional.<User>empty();
            }
        }).exceptionally(ex -> {
            log.error("Error getting user by email '{}'", email, ex);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Void> deleteUserById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("Attempted to delete user with null or empty ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("User ID missing for delete."));
        }
        // Consider deleting associated data (UserStats, UserLanguage, etc.) or using Cloud Functions trigger.
        // This only deletes the user document.
        ApiFuture<WriteResult> future = userCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting user {}", id, ex); });
    }

    @Override
    public CompletableFuture<List<User>> getAllUsers() {
        ApiFuture<QuerySnapshot> futureQuery = userCollection.get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot ->
                querySnapshot.getDocuments().stream()
                        .map(snapshot -> {
                            User user = snapshot.toObject(User.class);
                            if (user != null) user.setId(snapshot.getId());
                            return user;
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList()) // Return List<User> which is Iterable
        ).exceptionally(ex -> {
            log.error("Error getting all users", ex);
            return List.of(); // Return empty list on error
        });
    }

    @Override
    public CompletableFuture<Boolean> emailExists(String email) {
        if (email == null || email.isEmpty()) {
            log.warn("emailExists called with null or empty email.");
            return CompletableFuture.completedFuture(false);
        }
        // Query efficiently, only need to know if >= 1 exists
        ApiFuture<QuerySnapshot> futureQuery = userCollection.whereEqualTo("email", email).limit(1).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot -> !querySnapshot.isEmpty()) // True if query returned any document
                .exceptionally(ex -> {
                    log.error("Error checking if email exists: {}", email, ex);
                    return false; // Assume not exists on error
                });
    }

    @Override
    public CompletableFuture<Boolean> usernameExists(String username) {
        if (username == null || username.isEmpty()) {
            log.warn("usernameExists called with null or empty username.");
            return CompletableFuture.completedFuture(false);
        }
        // Query efficiently
        ApiFuture<QuerySnapshot> futureQuery = userCollection.whereEqualTo("userName", username).limit(1).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot -> !querySnapshot.isEmpty())
                .exceptionally(ex -> {
                    log.error("Error checking if username exists: {}", username, ex);
                    return false; // Assume not exists on error
                });
    }

    // --- UserStats Methods (Firestore Based) ---
    // Assuming UserStats document ID is the same as the User ID

    @Override
    public CompletableFuture<Void> saveUserStats(User user, UserStats userStats) {
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            log.warn("Attempted to save stats for user with null object or ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("User object or ID missing for saving stats."));
        }
        if (userStats == null) {
            log.warn("Attempted to save null UserStats for user {}", user.getId());
            return CompletableFuture.failedFuture(new IllegalArgumentException("UserStats object is null."));
        }
        // Set the UserStats document ID to be the same as the User ID
        userStats.setId(user.getId()); // Ensure ID is consistent if UserStats has an ID field
        ApiFuture<WriteResult> future = statsCollection.document(user.getId()).set(userStats, SetOptions.merge()); // Use merge to update
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error saving user stats for user {}", user.getId(), ex); });
    }

    // This seems redundant if getUserStatsByUserId exists and the ID is the user ID. Keeping for interface compliance.
    @Override
    public CompletableFuture<Optional<UserStats>> getUserStatsById(String id) {
        log.warn("getUserStatsById(String id) called. Prefer getUserStatsByUserId(String userId) if stats document ID matches user ID.");
        return getUserStatsByUserId(id); // Delegate to the more likely correct method
    }

    @Override
    public CompletableFuture<Optional<UserStats>> getUserStatsByUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("getUserStatsByUserId called with null or empty userId.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<DocumentSnapshot> futureSnapshot = statsCollection.document(userId).get();
        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        return cfSnapshot.thenApply(snapshot -> {
            if (snapshot.exists()) {
                UserStats stats = snapshot.toObject(UserStats.class);
                if (stats != null) {
                    stats.setId(snapshot.getId()); // Ensure ID is set from snapshot
                    return Optional.of(stats);
                } else {
                    log.warn("UserStats document {} exists but failed to map.", userId);
                    return Optional.<UserStats>empty();
                }
            } else {
                log.debug("UserStats document {} not found.", userId);
                return Optional.<UserStats>empty();
            }
        }).exceptionally(ex -> {
            log.error("Error getting user stats for user ID {}", userId, ex);
            return Optional.empty();
        });
    }

    // This seems redundant if deleteUserStatsByUserId exists. Keeping for interface compliance.
    @Override
    public CompletableFuture<Void> deleteUserStatsById(String id) {
        log.warn("deleteUserStatsById(String id) called. Prefer deleteUserStatsByUserId(String userId) if stats document ID matches user ID.");
        return deleteUserStatsByUserId(id); // Delegate
    }

    @Override
    public CompletableFuture<Void> deleteUserStatsByUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("Attempted to delete user stats with null or empty userId.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("User ID missing for deleting stats."));
        }
        ApiFuture<WriteResult> future = statsCollection.document(userId).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting user stats for user {}", userId, ex); });
    }

    @Override
    public CompletableFuture<List<UserStats>> getAllUserStats() {
        ApiFuture<QuerySnapshot> futureQuery = statsCollection.get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot ->
                querySnapshot.getDocuments().stream()
                        .map(snapshot -> {
                            UserStats stats = snapshot.toObject(UserStats.class);
                            if (stats != null) stats.setId(snapshot.getId());
                            return stats;
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList())
        ).exceptionally(ex -> {
            log.error("Error getting all user stats", ex);
            return List.of(); // Return empty list on error
        });
    }
}