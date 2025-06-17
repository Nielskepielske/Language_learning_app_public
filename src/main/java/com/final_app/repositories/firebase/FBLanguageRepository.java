package com.final_app.repositories.firebase;

import com.final_app.interfaces.ILanguageRepository;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.LanguageLevelSystem;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils; // Ensure this utility exists
// Remove RTDB specific utils if no longer needed
// import com.final_app.repositories.firebase.utils.FirebaseUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
// Remove RTDB imports
// import com.google.firebase.database.DataSnapshot;
// import com.google.firebase.database.DatabaseError;
// import com.google.firebase.database.FirebaseDatabase;
// import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBLanguageRepository implements ILanguageRepository {

    private static final Logger log = LoggerFactory.getLogger(FBLanguageRepository.class);
    private static final String LANG_COLLECTION = "languages";
    private static final String LEVEL_COLLECTION = "languageLevels";
    private static final String SYSTEM_COLLECTION = "languageLevelSystems";

    // Firestore instances
    private final Firestore firestoreDb;
    private final CollectionReference languageCollection;
    private final CollectionReference levelCollection;
    private final CollectionReference systemCollection;


    private static FBLanguageRepository instance = null;

    // Private constructor for Singleton pattern
    public FBLanguageRepository() {
        this.firestoreDb = FirebaseManager.getDb(); // Get Firestore instance
        this.languageCollection = firestoreDb.collection(LANG_COLLECTION);
        this.levelCollection = firestoreDb.collection(LEVEL_COLLECTION);
        this.systemCollection = firestoreDb.collection(SYSTEM_COLLECTION);
    }

    // Static method for Singleton access (thread-safe lazy initialization)
    public static FBLanguageRepository getInstance() {
        if (instance == null) {
            instance = new FBLanguageRepository();
        }
        return instance;
    }

    // --- Language Methods ---

    @Override
    public CompletableFuture<Void> addLanguage(Language language) {
        String id = (language.getId() == null || language.getId().isEmpty()) ?
                languageCollection.document().getId() : language.getId();
        language.setId(id);

        ApiFuture<WriteResult> future = languageCollection.document(id).set(language);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding language {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateLanguage(Language language) {
        if (language.getId() == null || language.getId().isEmpty()) {
            log.warn("Attempted to update language with null or empty ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("Language ID missing for update."));
        }
        // Use set with merge to avoid overwriting unrelated fields
        ApiFuture<WriteResult> future = languageCollection.document(language.getId()).set(language, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating language {}", language.getId(), ex); });
    }

    @Override
    public CompletableFuture<Void> deleteLanguage(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("Attempted to delete language with null or empty ID.");
            // Decide on policy: fail or complete silently?
            return CompletableFuture.failedFuture(new IllegalArgumentException("Language ID missing for delete."));
        }
        // Deleting a language might require checking/handling dependencies (e.g., UserLanguage)
        ApiFuture<WriteResult> future = languageCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting language {}", id, ex); });
    }

    @Override
    public CompletableFuture<Optional<Language>> getLanguage(Language language) {
        return getLanguageById(language.getId());
    }

    // getLanguage(Language language) seems redundant if getLanguageById exists, omitting refactor unless needed.

    @Override
    public CompletableFuture<Optional<Language>> getLanguageById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("getLanguageById called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<DocumentSnapshot> futureSnapshot = languageCollection.document(id).get();
        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        return cfSnapshot.thenCompose(snapshot -> {
            if (snapshot.exists()) {
                Language lang = snapshot.toObject(Language.class);
                if (lang != null) {
                    lang.setId(snapshot.getId());
                    // Map related system asynchronously
                    return mapConnectedObjectsAsync(lang)
                            .thenApply(v -> Optional.of(lang));
                } else {
                    log.warn("Language document {} exists but failed to map.", id);
                    return CompletableFuture.completedFuture(Optional.<Language>empty());
                }
            } else {
                log.debug("Language document {} not found.", id);
                return CompletableFuture.completedFuture(Optional.<Language>empty());
            }
        }).exceptionally(ex -> {
            log.error("Error getting language by ID {} or mapping system", id, ex);
            return Optional.empty(); // Return empty on error
        });
    }

    @Override
    public CompletableFuture<Optional<Language>> getLanguageByName(String name) {
        if (name == null || name.isEmpty()) {
            log.warn("getLanguageByName called with null or empty name.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        // Query Firestore for the language by name
        ApiFuture<QuerySnapshot> futureQuery = languageCollection.whereEqualTo("name", name).limit(1).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0); // Get the first result
                Language lang = snapshot.toObject(Language.class);
                if (lang != null) {
                    lang.setId(snapshot.getId());
                    // Map related system asynchronously
                    return mapConnectedObjectsAsync(lang)
                            .thenApply(v -> Optional.of(lang));
                } else {
                    log.warn("Language document found for name '{}' but failed to map.", name);
                    return CompletableFuture.completedFuture(Optional.<Language>empty());
                }
            } else {
                log.debug("Language document not found for name '{}'.", name);
                return CompletableFuture.completedFuture(Optional.<Language>empty());
            }
        }).exceptionally(ex -> {
            log.error("Error getting language by name '{}' or mapping system", name, ex);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Iterable<Language>> getAllLanguages() {
        ApiFuture<QuerySnapshot> futureQuery = languageCollection.get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            List<Language> languages = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        Language lang = snapshot.toObject(Language.class);
                        if (lang != null) lang.setId(snapshot.getId());
                        return lang;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (languages.isEmpty()) {
                return CompletableFuture.completedFuture(languages);
            }

            // Map system for each language
            List<CompletableFuture<Void>> mappingFutures = languages.stream()
                    .map(this::mapConnectedObjectsAsync)
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(mappingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (Iterable<Language>) languages); // Return the mapped list
        }).exceptionally(ex -> {
            log.error("Error getting all languages or mapping systems", ex);
            return List.of(); // Return empty list on error
        });
    }

    // --- LanguageLevel Methods ---

    @Override
    public CompletableFuture<Void> addLanguageLevel(LanguageLevel languageLevel) {
        String id = (languageLevel.getId() == null || languageLevel.getId().isEmpty()) ?
                levelCollection.document().getId() : languageLevel.getId();
        languageLevel.setId(id);

        ApiFuture<WriteResult> future = levelCollection.document(id).set(languageLevel);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding language level {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateLanguageLevel(LanguageLevel languageLevel) {
        if (languageLevel.getId() == null || languageLevel.getId().isEmpty()) {
            log.warn("Attempted to update language level with null or empty ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("LanguageLevel ID missing for update."));
        }
        ApiFuture<WriteResult> future = levelCollection.document(languageLevel.getId()).set(languageLevel, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating language level {}", languageLevel.getId(), ex); });
    }

    @Override
    public CompletableFuture<Void> deleteLanguageLevel(LanguageLevel languageLevel) {
        // The interface takes an object, but usually deletion is by ID. Assuming ID is primary.
        if (languageLevel == null || languageLevel.getId() == null || languageLevel.getId().isEmpty()) {
            log.warn("Attempted to delete language level with null object or ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("LanguageLevel object or ID missing for delete."));
        }
        // Consider implications: Does deleting a level affect existing user data or conversations?
        ApiFuture<WriteResult> future = levelCollection.document(languageLevel.getId()).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting language level {}", languageLevel.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<LanguageLevel>> getLanguageLevelById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("getLanguageLevelById called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<DocumentSnapshot> futureSnapshot = levelCollection.document(id).get();
        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        return cfSnapshot.thenApply(snapshot -> {
            if (snapshot.exists()) {
                LanguageLevel level = snapshot.toObject(LanguageLevel.class);
                if (level != null) {
                    level.setId(snapshot.getId());
                    return Optional.of(level);
                } else {
                    log.warn("LanguageLevel document {} exists but failed to map.", id);
                    return Optional.<LanguageLevel>empty();
                }
            } else {
                log.debug("LanguageLevel document {} not found.", id);
                return Optional.<LanguageLevel>empty();
            }
        }).exceptionally(ex -> {
            log.error("Error getting language level by ID {}", id, ex);
            return Optional.empty();
        });
    }


    @Override
    public CompletableFuture<Optional<LanguageLevel>> getLanguageLevelByName(String name) {
        if (name == null || name.isEmpty()) {
            log.warn("getLanguageLevelByName called with null or empty name.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<QuerySnapshot> futureQuery = levelCollection.whereEqualTo("name", name).limit(1).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                LanguageLevel level = snapshot.toObject(LanguageLevel.class);
                if (level != null) {
                    level.setId(snapshot.getId());
                    return Optional.of(level);
                } else {
                    log.warn("LanguageLevel document found for name '{}' but failed to map.", name);
                    return Optional.<LanguageLevel>empty();
                }
            } else {
                log.debug("LanguageLevel document not found for name '{}'.", name);
                return Optional.<LanguageLevel>empty();
            }
        }).exceptionally(ex -> {
            log.error("Error getting language level by name {}", name, ex);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<List<LanguageLevel>> getAllLanguageLevels() {
        ApiFuture<QuerySnapshot> futureQuery = levelCollection.get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenApply(querySnapshot ->
                querySnapshot.getDocuments().stream()
                        .map(snapshot -> {
                            LanguageLevel level = snapshot.toObject(LanguageLevel.class);
                            if (level != null) level.setId(snapshot.getId());
                            return level;
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList()) // Return List which is Iterable
        ).exceptionally(ex -> {
            log.error("Error getting all language levels", ex);
            return Collections.emptyList();
        });
    }

    // --- LanguageLevelSystem Methods ---

    @Override
    public CompletableFuture<Void> addLanguageSystem(LanguageLevelSystem languageLevelSystem) {
        String id = (languageLevelSystem.getId() == null || languageLevelSystem.getId().isEmpty()) ?
                systemCollection.document().getId() : languageLevelSystem.getId();
        languageLevelSystem.setId(id);

        ApiFuture<WriteResult> future = systemCollection.document(id).set(languageLevelSystem);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding language system {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateLanguageSystem(LanguageLevelSystem languageLevelSystem) {
        if (languageLevelSystem.getId() == null || languageLevelSystem.getId().isEmpty()) {
            log.warn("Attempted to update language system with null or empty ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("LanguageLevelSystem ID missing for update."));
        }
        // Note: Merging here only updates the system document itself.
        // If the list of levels *within* the system object changes, this won't automatically update level documents.
        // Managing relationships (system <-> levels) might require more complex logic or denormalization.
        ApiFuture<WriteResult> future = systemCollection.document(languageLevelSystem.getId()).set(languageLevelSystem, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating language system {}", languageLevelSystem.getId(), ex); });
    }

    @Override
    public CompletableFuture<Void> deleteLanguageSystem(LanguageLevelSystem languageLevelSystem) {
        // Deleting a system likely requires deleting or unlinking associated levels. Complex!
        // This example ONLY deletes the system document.
        if (languageLevelSystem == null || languageLevelSystem.getId() == null || languageLevelSystem.getId().isEmpty()) {
            log.warn("Attempted to delete language system with null object or ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("LanguageLevelSystem object or ID missing for delete."));
        }
        ApiFuture<WriteResult> future = systemCollection.document(languageLevelSystem.getId()).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting language system {}", languageLevelSystem.getId(), ex); });
        // TODO: Add logic to handle associated LanguageLevels (e.g., delete them or set their systemId to null).
    }


    @Override
    public CompletableFuture<Optional<LanguageLevelSystem>> getLanguageLevelSystemById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("getLanguageLevelSystemById called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<DocumentSnapshot> futureSnapshot = systemCollection.document(id).get();
        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        return cfSnapshot.thenCompose(snapshot -> {
            if (snapshot.exists()) {
                LanguageLevelSystem system = snapshot.toObject(LanguageLevelSystem.class);
                if (system != null) {
                    system.setId(snapshot.getId());
                    // Fetch and attach associated levels
                    return mapConnectedObjectsAsync(system)
                            .thenApply(v -> Optional.of(system));
                } else {
                    log.warn("LanguageLevelSystem document {} exists but failed to map.", id);
                    return CompletableFuture.completedFuture(Optional.<LanguageLevelSystem>empty());
                }
            } else {
                log.debug("LanguageLevelSystem document {} not found.", id);
                return CompletableFuture.completedFuture(Optional.<LanguageLevelSystem>empty());
            }
        }).exceptionally(ex -> {
            log.error("Error getting language system by ID {} or mapping levels", id, ex);
            return Optional.empty();
        });
    }


    @Override
    public CompletableFuture<Optional<LanguageLevelSystem>> getLanguageLevelSystemByName(String name) {
        if (name == null || name.isEmpty()) {
            log.warn("getLanguageLevelSystemByName called with null or empty name.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<QuerySnapshot> futureQuery = systemCollection.whereEqualTo("name", name).limit(1).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                LanguageLevelSystem system = snapshot.toObject(LanguageLevelSystem.class);
                if (system != null) {
                    system.setId(snapshot.getId());
                    // Fetch and attach associated levels
                    return mapConnectedObjectsAsync(system)
                            .thenApply(v -> Optional.of(system));
                } else {
                    log.warn("LanguageLevelSystem document found for name '{}' but failed to map.", name);
                    return CompletableFuture.completedFuture(Optional.<LanguageLevelSystem>empty());
                }
            } else {
                log.debug("LanguageLevelSystem document not found for name '{}'.", name);
                return CompletableFuture.completedFuture(Optional.<LanguageLevelSystem>empty());
            }
        }).exceptionally(ex -> {
            log.error("Error getting language system by name '{}' or mapping levels", name, ex);
            return Optional.empty();
        });
    }


    @Override
    public CompletableFuture<Iterable<LanguageLevelSystem>> getAllLanguageSystems() {
        ApiFuture<QuerySnapshot> futureQuery = systemCollection.get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            List<LanguageLevelSystem> systems = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        LanguageLevelSystem system = snapshot.toObject(LanguageLevelSystem.class);
                        if (system != null) system.setId(snapshot.getId());
                        return system;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (systems.isEmpty()) {
                return CompletableFuture.completedFuture(systems);
            }

            // Map levels for each system
            List<CompletableFuture<Void>> mappingFutures = systems.stream()
                    .map(this::mapConnectedObjectsAsync)
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(mappingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (Iterable<LanguageLevelSystem>) systems);
        }).exceptionally(ex -> {
            log.error("Error getting all language systems or mapping levels", ex);
            return List.of();
        });
    }

    // --- Mapping Helper Methods (Firestore Based) ---

    /**
     * Fetches LanguageLevels associated with a LanguageLevelSystem from Firestore
     * and sets them on the system object.
     */
    private CompletableFuture<Void> mapConnectedObjectsAsync(LanguageLevelSystem system) {
        if (system == null || system.getId() == null) {
            return CompletableFuture.completedFuture(null);
        }
        // Query levels collection for levels matching the system's ID
        ApiFuture<QuerySnapshot> futureQuery = levelCollection.whereEqualTo("systemId", system.getId()).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return (system.getLevels().isEmpty()) ? cfQuery.thenAccept(querySnapshot -> {
            List<LanguageLevel> levels = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        LanguageLevel level = snapshot.toObject(LanguageLevel.class);
                        if (level != null) level.setId(snapshot.getId());
                        return level;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            system.setLevels(levels); // Set the fetched levels on the system object
        }).exceptionally(ex -> {
            log.error("Error fetching levels for system ID {}", system.getId(), ex);
            // Decide if system should have empty list or null on error
            system.setLevels(List.of()); // Set empty list on error
            return null; // Suppress exception from propagating further in chain if desired
        })
                : CompletableFuture.completedFuture(null);
    }

    /**
     * Fetches the LanguageLevelSystem associated with a Language from Firestore
     * and sets it on the language object.
     */
    private CompletableFuture<Void> mapConnectedObjectsAsync(Language language) {
        if (language == null || language.getSystemId() == null || language.getSystemId().isEmpty() || language.getLanguageLevelSystem() != null) {
            return CompletableFuture.completedFuture(null); // No system ID to fetch
        }
        // Use the already refactored getLanguageLevelSystemById method
        return getLanguageLevelSystemById(language.getSystemId())
                .thenAccept(optionalSystem -> optionalSystem.ifPresent(language::setLanguageLevelSystem))
                .exceptionally(ex -> {
                    log.error("Error mapping system for language ID {}", language.getId(), ex);
                    // Decide error handling: keep system null?
                    return null;
                });
    }
}