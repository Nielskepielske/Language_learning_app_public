package com.final_app.repositories.firebase;

import com.final_app.factories.RepositoryFactory;
// Assuming ILanguageRepository is now the Firestore-based implementation
import com.final_app.interfaces.ILanguageRepository;
import com.final_app.interfaces.IUserLanguageRepository;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.User;
import com.final_app.models.UserLanguage;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils; // Ensure this utility exists
// Remove RTDB specific utils if no longer needed
// import com.final_app.repositories.firebase.utils.FirebaseUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
// Remove RTDB imports
// import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBUserLanguageRepository implements IUserLanguageRepository {

    private static final Logger log = LoggerFactory.getLogger(FBUserLanguageRepository.class);
    private static final String COLLECTION_NAME = "userLanguages";

    // Firestore instance and CollectionReference
    private final Firestore firestoreDb;
    private final CollectionReference userLangCollection;

    // Assumes RepositoryFactory provides the Firestore-based ILanguageRepository
    private final ILanguageRepository languageRepository = RepositoryFactory.getLanguageRepository();

    private static FBUserLanguageRepository instance = null;

    public FBUserLanguageRepository() {
        this.firestoreDb = FirebaseManager.getDb(); // Get Firestore instance
        this.userLangCollection = firestoreDb.collection(COLLECTION_NAME);
    }

    // Static synchronized method for Singleton access
    public static IUserLanguageRepository getInstance() {
        if (instance == null) {
            instance = new FBUserLanguageRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addUserLanguage(UserLanguage userLanguage) {
        String id = (userLanguage.getId() == null || userLanguage.getId().isEmpty()) ?
                userLangCollection.document().getId() : userLanguage.getId();
        userLanguage.setId(id);

        ApiFuture<WriteResult> future = userLangCollection.document(id).set(userLanguage);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding user language {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateUserLanguage(UserLanguage userLanguage) {
        if (userLanguage.getId() == null || userLanguage.getId().isEmpty()) {
            log.warn("Attempted to update UserLanguage with null or empty ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("UserLanguage ID missing for update."));
        }
        ApiFuture<WriteResult> future = userLangCollection.document(userLanguage.getId()).set(userLanguage, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating user language {}", userLanguage.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<UserLanguage>> getUserLanguageById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("getUserLanguageById called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<DocumentSnapshot> futureSnapshot = userLangCollection.document(id).get();
        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        return cfSnapshot.thenCompose(snapshot -> {
            if (snapshot.exists()) {
                UserLanguage userLang = snapshot.toObject(UserLanguage.class);
                if (userLang != null) {
                    userLang.setId(snapshot.getId());
                    // Map related Language and Level asynchronously
                    return mapConnectedObjects(userLang)
                            .thenApply(v -> Optional.of(userLang));
                } else {
                    log.warn("UserLanguage document {} exists but failed to map.", id);
                    return CompletableFuture.completedFuture(Optional.<UserLanguage>empty());
                }
            } else {
                log.debug("UserLanguage document {} not found.", id);
                return CompletableFuture.completedFuture(Optional.<UserLanguage>empty());
            }
        }).exceptionally(ex -> {
            log.error("Error getting user language by ID {} or mapping", id, ex);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<UserLanguage>> getUserLanguageByLanguageIdAndUserId(String languageId, String userId) {
        if (languageId == null || languageId.isEmpty() || userId == null || userId.isEmpty()) {
            log.warn("getUserLanguageByLanguageIdAndUserId called with null or empty IDs.");
            return CompletableFuture.completedFuture(Optional.empty());
        }

        // **IMPORTANT**: This query requires a composite index on 'languageId' and 'userId' in Firestore.
        // Create this index in your Firebase console.
        ApiFuture<QuerySnapshot> futureQuery = userLangCollection
                .whereEqualTo("languageId", languageId)
                .whereEqualTo("userId", userId)
                .limit(1) // Expecting only one or zero result
                .get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                UserLanguage userLang = snapshot.toObject(UserLanguage.class);
                if (userLang != null) {
                    userLang.setId(snapshot.getId());
                    // Map related Language and Level asynchronously
                    return mapConnectedObjects(userLang)
                            .thenApply(v -> Optional.of(userLang));
                } else {
                    log.warn("UserLanguage document found for lang {} user {} but failed to map.", languageId, userId);
                    return CompletableFuture.completedFuture(Optional.<UserLanguage>empty());
                }
            } else {
                log.debug("UserLanguage document not found for lang {} user {}.", languageId, userId);
                return CompletableFuture.completedFuture(Optional.<UserLanguage>empty());
            }
        }).exceptionally(ex -> {
            log.error("Error getting user language for language ID {} and user ID {} or mapping", languageId, userId, ex);
            // Check if exception is due to missing index and log appropriately if possible
            if (ex.getMessage() != null && ex.getMessage().contains("requires an index")) {
                log.error("Firestore query failed. Potential missing composite index for 'languageId' and 'userId' on collection '{}'. Please create it in the Firebase console.", COLLECTION_NAME);
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Void> deleteUserLanguageById(String id) {
        if(id == null || id.isEmpty()) {
            log.warn("Attempted to delete UserLanguage with null or empty ID.");
            return CompletableFuture.completedFuture(null); // Or fail?
        }
        ApiFuture<WriteResult> future = userLangCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting user language {}", id, ex); });
    }

    @Override
    public CompletableFuture<Iterable<UserLanguage>> getAllUserLanguagesFromUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("getAllUserLanguagesFromUser called with null or empty userId.");
            return CompletableFuture.completedFuture(List.of()); // Return empty list
        }
        ApiFuture<QuerySnapshot> futureQuery = userLangCollection.whereEqualTo("userId", userId).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            List<UserLanguage> userLanguages = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        UserLanguage ul = snapshot.toObject(UserLanguage.class);
                        if (ul != null) ul.setId(snapshot.getId());
                        return ul;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (userLanguages.isEmpty()) {
                return CompletableFuture.completedFuture(userLanguages);
            }

            // Map Language and Level for each UserLanguage entry
            List<CompletableFuture<Void>> mappingFutures = userLanguages.stream()
                    .map(this::mapConnectedObjects)
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(mappingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (Iterable<UserLanguage>) userLanguages);
        }).exceptionally(ex -> {
            log.error("Error getting user languages for user ID {} or mapping", userId, ex);
            return List.of(); // Return empty list on error
        });
    }

    /**
     * Maps the Language and LanguageLevel objects to the UserLanguage object
     * by fetching them using the ILanguageRepository (assumed Firestore-based).
     */
    private CompletableFuture<Void> mapConnectedObjects(UserLanguage userLanguage){
        if(userLanguage == null) return CompletableFuture.completedFuture(null);

        // Assumes languageRepository methods return CompletableFuture<Optional<...>>
        CompletableFuture<Optional<Language>> langFuture = (userLanguage.getLanguage() == null) ? languageRepository.getLanguageById(userLanguage.getLanguageId()) : CompletableFuture.completedFuture(Optional.of(userLanguage.getLanguage()));
        CompletableFuture<Optional<LanguageLevel>> levelFuture = (userLanguage.getLevel() == null) ? languageRepository.getLanguageLevelById(userLanguage.getLevelId()) : CompletableFuture.completedFuture(Optional.of(userLanguage.getLevel()));

        // Combine and set when both futures complete
        return CompletableFuture.allOf(langFuture, levelFuture)
                .thenAccept(voidResult -> {
                    langFuture.join().ifPresent(userLanguage::setLanguage); // Use join as allOf guarantees completion
                    levelFuture.join().ifPresent(userLanguage::setLevel);
                }).exceptionally(ex -> {
                    log.error("Error mapping Language or Level for UserLanguage {}", userLanguage.getId(), ex);
                    // Decide error handling: keep objects null?
                    return null;
                });
    }
}