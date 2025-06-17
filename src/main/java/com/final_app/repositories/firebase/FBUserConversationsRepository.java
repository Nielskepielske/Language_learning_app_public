package com.final_app.repositories.firebase;

import com.final_app.factories.RepositoryFactory;
// Import necessary interfaces for mapping
import com.final_app.interfaces.IConversationRepository;
import com.final_app.interfaces.IUserConversationsRepository;
import com.final_app.interfaces.IUserRepository;
import com.final_app.models.*;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import List and Optional if not already present
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBUserConversationsRepository implements IUserConversationsRepository {

    private static final Logger log = LoggerFactory.getLogger(FBUserConversationsRepository.class);
    private static final String USER_CONVO_COLLECTION = "userConversations";
    private static final String USER_CHAIN_ITEM_COLLECTION = "userConversationChainItems";

    private final Firestore firestoreDb; // Keep Firestore instance
    private final CollectionReference userConvoCollection;
    private final CollectionReference userChainItemCollection;

    // Dependencies needed for mapping connected objects
    private final IUserRepository userRepository;
    private final IConversationRepository conversationRepository;


    private static FBUserConversationsRepository instance = null; // Corrected instance type

    // Private constructor for Singleton
    public FBUserConversationsRepository() {
        this.firestoreDb = FirebaseManager.getDb();
        this.userConvoCollection = firestoreDb.collection(USER_CONVO_COLLECTION);
        this.userChainItemCollection = firestoreDb.collection(USER_CHAIN_ITEM_COLLECTION);

        // Get dependent repositories via Factory (assuming they are Firestore-based)
        this.userRepository = RepositoryFactory.getUserRepository();
        this.conversationRepository = RepositoryFactory.getConversationRepository();
    }

    // Static synchronized method for Singleton access
    public static IUserConversationsRepository getInstance() {
        if (instance == null) {
            instance = new FBUserConversationsRepository();
        }
        return instance;
    }


    // --- UserConversation Methods ---

    @Override
    public CompletableFuture<Void> addUserConversation(UserConversation userConversation) {
        String id = (userConversation.getId() == null || userConversation.getId().isEmpty()) ?
                userConvoCollection.document().getId() : userConversation.getId();
        userConversation.setId(id);
        // Ensure related objects are not persisted, only their IDs
        //UserConversation cleanUC = userConversation.cloneWithoutConnectedObjects(); // Assumes such a method exists or implement here
        ApiFuture<WriteResult> future = userConvoCollection.document(id).set(userConversation);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding user conversation {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateUserConversation(UserConversation userConversation) {
        if (userConversation.getId() == null || userConversation.getId().isEmpty()) {
            log.warn("Attempted to update UserConversation with null or empty ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("UserConversation ID missing for update."));
        }
        // Ensure related objects are not persisted, only their IDs
        //UserConversation cleanUC = userConversation.cloneWithoutConnectedObjects(); // Assumes such a method exists or implement here
        ApiFuture<WriteResult> future = userConvoCollection.document(userConversation.getId()).set(userConversation, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating user conversation {}", userConversation.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<UserConversation>> getUserConversationById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("getUserConversationById called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<DocumentSnapshot> futureSnapshot = userConvoCollection.document(id).get();
        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        // Chain mapping after fetching
        return cfSnapshot.thenCompose(snapshot -> { // Use thenCompose for async mapping
            if (snapshot.exists()) {
                UserConversation uc = snapshot.toObject(UserConversation.class);
                if (uc != null) {
                    uc.setId(snapshot.getId());
                    // Call mapping function which returns CompletableFuture<Void>
                    return mapConnectedObjects(uc)
                            .thenApply(v -> Optional.of(uc)); // Return Optional<UserConversation> after mapping
                } else {
                    log.warn("UserConversation document {} exists but failed to map.", id);
                    return CompletableFuture.completedFuture(Optional.<UserConversation>empty());
                }
            } else {
                log.debug("UserConversation document {} not found.", id);
                return CompletableFuture.completedFuture(Optional.<UserConversation>empty());
            }
        }).exceptionally(ex -> {
            log.error("Error getting user conversation by ID {} or mapping", id, ex);
            return Optional.empty(); // Return empty Optional on error
        });
    }


    @Override
    public CompletableFuture<Void> deleteUserConversationById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("Attempted to delete UserConversation with null or empty ID.");
            return CompletableFuture.completedFuture(null); // Or fail
        }
        // Consider deleting related UserConversationChainItems? This only deletes the main doc.
        ApiFuture<WriteResult> future = userConvoCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting user conversation {}", id, ex); });
    }

    @Override
    public CompletableFuture<Iterable<UserConversation>> getAllUserConversationsFromUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("getAllUserConversationsFromUser called with null or empty userId.");
            return CompletableFuture.completedFuture(List.of());
        }
        ApiFuture<QuerySnapshot> futureQuery = userConvoCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING) // Example order
                .get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> { // Use thenCompose for async mapping
            List<UserConversation> userConversations = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        UserConversation uc = snapshot.toObject(UserConversation.class);
                        if (uc != null) uc.setId(snapshot.getId());
                        return uc;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (userConversations.isEmpty()) {
                return CompletableFuture.completedFuture(userConversations); // Return empty list directly
            }

            // Map connected objects for each item in the list
            List<CompletableFuture<Void>> mappingFutures = userConversations.stream()
                    .map(this::mapConnectedObjects) // mapConnectedObjects returns CompletableFuture<Void>
                    .collect(Collectors.toList());

            // Wait for all mappings to complete, then return the list
            return CompletableFuture.allOf(mappingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (Iterable<UserConversation>) userConversations);
        }).exceptionally(ex -> {
            log.error("Error getting user conversations for user ID {} or mapping", userId, ex);
            return List.of(); // Return empty list on error
        });
    }

    @Override
    public CompletableFuture<Iterable<UserConversation>> getUserConversationsByUserAndConversationId(String userId, String conversationId) {
        if (userId == null || userId.isEmpty() || conversationId == null || conversationId.isEmpty()) {
            log.warn("getUserConversationsByUserAndConversationId called with null or empty IDs.");
            return CompletableFuture.completedFuture(List.of());
        }
        // **IMPORTANT**: Requires a composite index on (userId, conversationId)
        ApiFuture<QuerySnapshot> futureQuery = userConvoCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("conversationId", conversationId)
                .get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> { // Use thenCompose for async mapping
            List<UserConversation> userConversations = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        UserConversation uc = snapshot.toObject(UserConversation.class);
                        if (uc != null) uc.setId(snapshot.getId());
                        return uc;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (userConversations.isEmpty()) {
                return CompletableFuture.completedFuture(userConversations);
            }

            // Map connected objects
            List<CompletableFuture<Void>> mappingFutures = userConversations.stream()
                    .map(this::mapConnectedObjects)
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(mappingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (Iterable<UserConversation>) userConversations);
        }).exceptionally(ex -> {
            log.error("Error getting user conversations for user ID {} and conversation ID {} or mapping", userId, conversationId, ex);
            if (ex.getMessage() != null && ex.getMessage().contains("requires an index")) {
                log.error("Firestore query failed. Potential missing composite index for 'userId' and 'conversationId' on collection '{}'. Please create it in the Firebase console.", USER_CONVO_COLLECTION);
            }
            return List.of();
        });
    }

    // --- UserConversationChainItem Methods ---
    // (Keeping previous implementation, mapping not added here for brevity, but could be done similarly)

    @Override
    public CompletableFuture<Void> addUserConversationChainItem(UserConversationChainItem userConversationChainItem) {
        String id = (userConversationChainItem.getId() == null || userConversationChainItem.getId().isEmpty()) ?
                userChainItemCollection.document().getId() : userConversationChainItem.getId();
        userConversationChainItem.setId(id);
        ApiFuture<WriteResult> future = userChainItemCollection.document(id).set(userConversationChainItem);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding user conversation chain item {}", id, ex); });
    }

    @Override
    public CompletableFuture<Optional<UserConversationChainItem>> getUserConversationChainItemById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("getUserConversationChainItemById called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<DocumentSnapshot> futureSnapshot = userChainItemCollection.document(id).get();
        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        return cfSnapshot.thenApply(snapshot -> {
            if (snapshot.exists()) {
                UserConversationChainItem uci = snapshot.toObject(UserConversationChainItem.class);
                if (uci != null) {
                    uci.setId(snapshot.getId());
                    // Potential place to add mapping for UserConversationChainItem if needed
                    return Optional.of(uci);
                } else {
                    log.warn("UserConversationChainItem document {} exists but failed to map.", id);
                    return Optional.<UserConversationChainItem>empty();
                }
            } else {
                log.debug("UserConversationChainItem document {} not found.", id);
                return Optional.<UserConversationChainItem>empty();
            }
        }).exceptionally(ex -> {
            log.error("Error getting user conversation chain item by ID {}", id, ex);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<UserConversationChainItem>> getUserConversationChainItemByUserConversationId(String userConversationId) {
        if (userConversationId == null || userConversationId.isEmpty()) {
            log.warn("getUserConversationChainItemByUserConversationId called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        // Query by userConversationId field (ensure this field exists on the model)
        ApiFuture<QuerySnapshot> futureQuery = userChainItemCollection
                .whereEqualTo("userConversationId", userConversationId)
                .limit(1)
                .get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                UserConversationChainItem uci = snapshot.toObject(UserConversationChainItem.class);
                if (uci != null) {
                    uci.setId(snapshot.getId());
                    // Potential place to add mapping
                    return Optional.of(uci);
                } else {
                    log.warn("UserConversationChainItem found for userConversationId {} but failed to map.", userConversationId);
                    return Optional.<UserConversationChainItem>empty();
                }
            } else {
                log.debug("UserConversationChainItem not found for userConversationId {}.", userConversationId);
                return Optional.<UserConversationChainItem>empty();
            }
        }).exceptionally(ex -> {
            log.error("Error getting user conversation chain item by userConversation ID {}", userConversationId, ex);
            return Optional.empty();
        });
    }


    @Override
    public CompletableFuture<Void> deleteUserConversationChainItemById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("Attempted to delete UserConversationChainItem with null or empty ID.");
            return CompletableFuture.completedFuture(null); // Or fail
        }
        ApiFuture<WriteResult> future = userChainItemCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting user conversation chain item {}", id, ex); });
    }

    @Override
    public CompletableFuture<List<UserConversationChainItem>> getAllUserConversationChainItemsFromUser(User user) {
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            log.warn("getAllUserConversationChainItemsFromUser called with null user or user ID.");
            return CompletableFuture.completedFuture(List.of());
        }
        ApiFuture<QuerySnapshot> futureQuery = userChainItemCollection
                .whereEqualTo("userId", user.getId())
                .get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot ->
                querySnapshot.getDocuments().stream()
                        .map(snapshot -> {
                            UserConversationChainItem uci = snapshot.toObject(UserConversationChainItem.class);
                            if (uci != null) uci.setId(snapshot.getId());
                            // Potential place to add mapping for list items
                            return uci;
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList())
        ).exceptionally(ex -> {
            log.error("Error getting user conversation chain items for user ID {}", user.getId(), ex);
            return List.of();
        });
    }

    @Override
    public CompletableFuture<List<UserConversationChainItem>> getAllUserConversationChainItemsFromConversationChainId(String conversationChainId) {
        if (conversationChainId == null || conversationChainId.isEmpty()) {
            log.warn("getAllUserConversationChainItemsFromConversationChainId called with null or empty ID.");
            return CompletableFuture.completedFuture(List.of());
        }
        ApiFuture<QuerySnapshot> futureQuery = userChainItemCollection
                .whereEqualTo("conversationChainId", conversationChainId)
                .get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot ->
                querySnapshot.getDocuments().stream()
                        .map(snapshot -> {
                            UserConversationChainItem uci = snapshot.toObject(UserConversationChainItem.class);
                            if (uci != null) uci.setId(snapshot.getId());
                            // Potential place to add mapping for list items
                            return uci;
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList())
        ).exceptionally(ex -> {
            log.error("Error getting user conversation chain items for chain ID {}", conversationChainId, ex);
            return List.of();
        });
    }


    // --- Mapping Helper Method ---

    /**
     * Maps the connected User and Conversation objects based on IDs
     * stored in the UserConversation object.
     * Assumes userRepository and conversationRepository are Firestore-based
     * and return CompletableFuture<Optional<...>>.
     * @param userConversation The UserConversation object to populate.
     * @return A CompletableFuture<Void> indicating completion of mapping.
     */
    private CompletableFuture<Void> mapConnectedObjects(UserConversation userConversation) {
        if (userConversation == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Fetch User and Conversation concurrently
        CompletableFuture<Optional<User>> userFuture = (userConversation.getUser() == null) ? userRepository.getUserById(userConversation.getUserId()) : CompletableFuture.completedFuture(Optional.of(userConversation.getUser()));
        CompletableFuture<Optional<Conversation>> convoFuture = (userConversation.getConversation() == null) ? conversationRepository.getConversationById(userConversation.getConversationId()) : CompletableFuture.completedFuture(Optional.of(userConversation.getConversation()));
        CompletableFuture<Iterable<Message>> messageFuture = (userConversation.getMessages().isEmpty()) ? RepositoryFactory.getMessageRepository().getAllMessagesFromUserConversation(userConversation.getId()) : CompletableFuture.completedFuture(userConversation.getMessages());

        // Combine futures and set the results on the userConversation object
        return CompletableFuture.allOf(userFuture, convoFuture)
                .thenAccept(voidResult -> {
                    userFuture.join().ifPresent(userConversation::setUser); // join() is safe after allOf
                    convoFuture.join().ifPresent(userConversation::setConversation);
                    userConversation.setMessages((List<Message>) messageFuture.join());
                    updateUserConversation(userConversation);
                }).exceptionally(ex -> {
                    log.error("Error mapping User or Conversation for UserConversation {}", userConversation.getId(), ex);
                    // Decide error handling: leave objects null? Log and continue?
                    return null; // Suppress exception propagation if desired
                });
    }

    // Helper method potentially needed for add/update to avoid persisting full objects
    // Add this to your UserConversation model or implement similar logic here.
    /*
    private UserConversation cloneWithoutConnectedObjects(UserConversation original) {
        UserConversation clone = new UserConversation();
        // Copy only ID fields and primitive/simple fields
        clone.setId(original.getId());
        clone.setUserId(original.getUserId());
        clone.setConversationId(original.getConversationId());
        clone.setStartTime(original.getStartTime());
        clone.setEndTime(original.getEndTime());
        clone.setStatus(original.getStatus());
        clone.setUserMessageCount(original.getUserMessageCount());
        // DO NOT copy user, conversation fields
        return clone;
    }
    */

}