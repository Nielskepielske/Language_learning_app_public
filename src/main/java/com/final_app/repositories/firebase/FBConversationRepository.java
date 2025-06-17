package com.final_app.repositories.firebase;

import com.final_app.factories.RepositoryFactory;
import com.final_app.interfaces.IConversationRepository;
// Assuming these interfaces remain, but their implementations (accessed via RepositoryFactory)
// will be updated to return CompletableFuture and potentially use Firestore.
import com.final_app.interfaces.ILanguageRepository;
import com.final_app.interfaces.IScenarioRepository;
import com.final_app.models.*;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils; // Ensure this utility is available and correct
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Remove Realtime Database imports if no longer needed anywhere in the class
// import com.google.firebase.database.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBConversationRepository implements IConversationRepository {
    private static final Logger log = LoggerFactory.getLogger(FBConversationRepository.class); // Consistent logger name
    private static final String CONVERSATIONS_COLLECTION_NAME = "conversations";
    private static final String CONVERSATION_CHAINS_COLLECTION_NAME = "conversationChains";
    private static final String CONVERSATION_CHAIN_ITEMS_COLLECTION_NAME = "conversationChainItems";

    private final Firestore firestoreDb;
    private final CollectionReference conversationCollection;
    private final CollectionReference conversationChainCollection;
    private final CollectionReference conversationChainItemCollection;

    // IMPORTANT: Assumes RepositoryFactory provides instances (potentially Firestore-based)
    // that return CompletableFuture for async operations.
    private final ILanguageRepository languageRepository = RepositoryFactory.getLanguageRepository();
    private final IScenarioRepository scenarioRepository = RepositoryFactory.getScenarioRepository(); // Assuming this exists and is needed

    private static FBConversationRepository instance = null;

    public FBConversationRepository() {
        // Basic Singleton pattern (consider dependency injection frameworks for larger apps)
        if(instance == null){
            instance = this;
        }
        this.firestoreDb = FirebaseManager.getDb(); // Assumes FirebaseManager provides Firestore instance
        this.conversationCollection = firestoreDb.collection(CONVERSATIONS_COLLECTION_NAME);
        this.conversationChainCollection = firestoreDb.collection(CONVERSATION_CHAINS_COLLECTION_NAME);
        this.conversationChainItemCollection = firestoreDb.collection(CONVERSATION_CHAIN_ITEMS_COLLECTION_NAME);
    }

    // Consider making the constructor private and using a static getInstance method for Singleton
    public static FBConversationRepository getInstance(){
        if(instance == null){
            // This might lead to multiple instances if called concurrently before first assignment.
            // A static synchronized method or eager initialization is safer for singletons.
            return new FBConversationRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addConversation(Conversation conversation) {
        String id = (conversation.getId() == null || conversation.getId().isEmpty()) ?
                conversationCollection.document().getId() : conversation.getId();
        conversation.setId(id);

        ApiFuture<WriteResult> future = conversationCollection.document(id).set(conversation);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding conversation {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateConversation(Conversation conversation) {
        if(conversation.getId() == null || conversation.getId().isEmpty()){
            log.warn("Attempted to update conversation with null or empty ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("Conversation ID missing for update."));
        }
        // Use set with merge to avoid overwriting fields not included in the conversation object
        ApiFuture<WriteResult> future = conversationCollection.document(conversation.getId()).set(conversation, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating conversation {}", conversation.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<Conversation>> getConversationById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("getConversationById called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        DocumentReference docRef = conversationCollection.document(id);
        ApiFuture<DocumentSnapshot> futureSnapshot = docRef.get();

        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        return cfSnapshot.thenCompose(snapshot -> {
            if (snapshot.exists()) {
                Conversation conversation = snapshot.toObject(Conversation.class);
                if (conversation != null) {
                    conversation.setId(snapshot.getId());
                    // Asynchronously map related objects and return Optional<Conversation>
                    return mapConnectedObjects(conversation)
                            .thenApply(v -> Optional.of(conversation));
                } else {
                    log.warn("Conversation document {} exists but failed to map to object.", id);
                    return CompletableFuture.completedFuture(Optional.<Conversation>empty());
                }
            } else {
                log.debug("Conversation document {} not found.", id);
                return CompletableFuture.completedFuture(Optional.<Conversation>empty());
            }
        }).exceptionally(ex -> { // Catch errors during Firestore access or mapping
            log.error("Error getting conversation by ID {} or mapping related objects", id, ex);
            return Optional.empty(); // Return empty Optional on error
        });
    }


    @Override
    public CompletableFuture<Void> deleteConversationById(String id) {
        if(id == null || id.isEmpty()) {
            log.warn("Attempted to delete conversation with null or empty ID.");
            return CompletableFuture.completedFuture(null); // Or failedFuture? Decide policy.
        }
        ApiFuture<WriteResult> future = conversationCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting conversation {}", id, ex); });
    }

    @Override
    public CompletableFuture<Iterable<Conversation>> getAllConversations() {
        ApiFuture<QuerySnapshot> futureQuery = conversationCollection.get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            List<Conversation> conversations = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        Conversation conv = snapshot.toObject(Conversation.class);
                        if (conv != null) conv.setId(snapshot.getId());
                        return conv;
                    })
                    .filter(java.util.Objects::nonNull) // Filter out mapping failures
                    .collect(Collectors.toList());

            if (conversations.isEmpty()) {
                return CompletableFuture.completedFuture(conversations); // Return empty list directly
            }

            List<CompletableFuture<Void>> mappingFutures = conversations.stream()
                    .map(this::mapConnectedObjects)
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(mappingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (Iterable<Conversation>) conversations); // Return mapped list
        }).exceptionally(ex -> { // Handle errors during query or mapping
            log.error("Error getting all conversations or mapping related objects", ex);
            return List.of(); // Return empty list on error
        });
    }


    @Override
    public CompletableFuture<Iterable<Conversation>> getAllConversationsByLanguage(String languageId) {
        if (languageId == null || languageId.isEmpty()) {
            log.warn("getAllConversationsByLanguage called with null or empty languageId.");
            return CompletableFuture.completedFuture(List.of());
        }
        ApiFuture<QuerySnapshot> futureQuery = conversationCollection.whereEqualTo("languageId", languageId).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            List<Conversation> conversations = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        Conversation conv = snapshot.toObject(Conversation.class);
                        if (conv != null) conv.setId(snapshot.getId());
                        return conv;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (conversations.isEmpty()) {
                return CompletableFuture.completedFuture(conversations);
            }

            List<CompletableFuture<Void>> mappingFutures = conversations.stream()
                    .map(this::mapConnectedObjects)
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(mappingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (Iterable<Conversation>) conversations);
        }).exceptionally(ex -> {
            log.error("Error getting conversations by language ID {} or mapping", languageId, ex);
            return List.of();
        });
    }

    @Override
    public CompletableFuture<Iterable<Conversation>> getAllConversationsByLevel(String levelId) {
        if (levelId == null || levelId.isEmpty()) {
            log.warn("getAllConversationsByLevel called with null or empty levelId.");
            return CompletableFuture.completedFuture(List.of());
        }
        ApiFuture<QuerySnapshot> futureQuery = conversationCollection.whereEqualTo("levelId", levelId).get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            List<Conversation> conversations = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        Conversation conv = snapshot.toObject(Conversation.class);
                        if (conv != null) conv.setId(snapshot.getId());
                        return conv;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (conversations.isEmpty()) {
                return CompletableFuture.completedFuture(conversations);
            }

            List<CompletableFuture<Void>> mappingFutures = conversations.stream()
                    .map(this::mapConnectedObjects)
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(mappingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (Iterable<Conversation>) conversations);
        }).exceptionally(ex -> {
            log.error("Error getting conversations by level ID {} or mapping", levelId, ex);
            return List.of();
        });
    }

    // --- ConversationChain Methods ---

    @Override
    public CompletableFuture<Void> addConversationChain(ConversationChain conversationChain) {
        // Using Batch Write for atomicity
        WriteBatch batch = firestoreDb.batch();

        String chainId = (conversationChain.getId() == null || conversationChain.getId().isEmpty()) ?
                conversationChainCollection.document().getId() : conversationChain.getId();
        conversationChain.setId(chainId);

        DocumentReference chainRef = conversationChainCollection.document(chainId);
        // Ensure ConversationChain model is Firestore compatible (e.g., no List<Conversation>)
        // You might need a DTO or adapt the model if it contains nested objects directly.
        batch.set(chainRef, conversationChain);

        if (conversationChain.getConversations() != null) {
            for (ConversationChainItem item : conversationChain.getConversations()) {
                String itemId = (item.getId() == null || item.getId().isEmpty()) ?
                        conversationChainItemCollection.document().getId() : item.getId();
                item.setId(itemId);
                item.setConversationChainId(chainId); // Link to parent

                DocumentReference itemRef = conversationChainItemCollection.document(itemId);
                // Ensure ConversationChainItem model is Firestore compatible
                batch.set(itemRef, item);
            }
        }

        ApiFuture<List<WriteResult>> future = batch.commit();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(results -> (Void) null) // Convert List<WriteResult> future to Void future
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding conversation chain {}", chainId, ex); });
    }

    @Override
    public CompletableFuture<Void> updateConversationChain(ConversationChain conversationChain) {
        // Updating chains and items atomically is complex.
        // A simple merge on the chain document might be insufficient if items change.
        // Consider using a transaction or a more sophisticated batch update strategy.
        // This example only merges the main chain document. Item updates are NOT handled here.
        if (conversationChain.getId() == null || conversationChain.getId().isEmpty()) {
            log.warn("Attempted to update conversation chain with null or empty ID.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("ConversationChain ID missing for update."));
        }
        ApiFuture<WriteResult> future = conversationChainCollection.document(conversationChain.getId()).set(conversationChain, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating conversation chain {}", conversationChain.getId(), ex); });
        // TODO: Implement logic to add/update/delete items within the chain if needed.
    }

    @Override
    public CompletableFuture<Optional<ConversationChain>> getConversationChainById(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("getConversationChainById called with null or empty ID.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        ApiFuture<DocumentSnapshot> futureSnapshot = conversationChainCollection.document(id).get();
        CompletableFuture<DocumentSnapshot> cfSnapshot = FirestoreFutureUtils.toCompletableFuture(futureSnapshot);

        return cfSnapshot.thenCompose(snapshot -> {
            if (snapshot.exists()) {
                ConversationChain chain = snapshot.toObject(ConversationChain.class);
                if (chain != null) {
                    chain.setId(snapshot.getId());
                    // Map related language, level, and fetch/map associated items
                    return mapConnectedObjects(chain)
                            .thenApply(v -> Optional.of(chain));
                } else {
                    log.warn("ConversationChain document {} exists but failed to map to object.", id);
                    return CompletableFuture.completedFuture(Optional.<ConversationChain>empty());
                }
            } else {
                log.debug("ConversationChain document {} not found.", id);
                return CompletableFuture.completedFuture(Optional.<ConversationChain>empty());
            }
        }).exceptionally(ex -> {
            log.error("Error getting conversation chain {} or mapping", id, ex);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Void> deleteConversationChainById(String id) {
        // Requires deleting the chain document AND all associated items.
        // Best done atomically using Batch Write or a Cloud Function trigger.
        if (id == null || id.isEmpty()) {
            log.warn("Attempted to delete conversation chain with null or empty ID.");
            return CompletableFuture.completedFuture(null);
        }

        // 1. Find all items belonging to the chain
        ApiFuture<QuerySnapshot> itemsQueryFuture = conversationChainItemCollection.whereEqualTo("conversationChainId", id).get();
        CompletableFuture<QuerySnapshot> cfItemsQuery = FirestoreFutureUtils.toCompletableFuture(itemsQueryFuture);

        // 2. Use thenCompose to perform deletion after query completes
        return cfItemsQuery.thenCompose(querySnapshot -> {
                    WriteBatch batch = firestoreDb.batch();
                    // Delete all found items
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    // Delete the main chain document
                    batch.delete(conversationChainCollection.document(id));
                    // Commit the batch
                    ApiFuture<List<WriteResult>> commitFuture = batch.commit();
                    return FirestoreFutureUtils.toCompletableFuture(commitFuture); // Return future for commit
                }).thenApply(results -> (Void) null) // Convert List<WriteResult> future to Void future
                .exceptionally(ex -> { // Handle errors during query or delete
                    log.error("Error deleting conversation chain {} and its items", id, ex);
                    // Depending on policy, you might want to re-throw or just return null
                    return null;
                });
    }

    @Override
    public CompletableFuture<Iterable<ConversationChain>> getAllConversationChains() {
        ApiFuture<QuerySnapshot> futureQuery = conversationChainCollection.get();
        CompletableFuture<QuerySnapshot> cfQuery = FirestoreFutureUtils.toCompletableFuture(futureQuery);

        return cfQuery.thenCompose(querySnapshot -> {
            List<ConversationChain> chains = querySnapshot.getDocuments().stream()
                    .map(snapshot -> {
                        ConversationChain chain = snapshot.toObject(ConversationChain.class);
                        if (chain != null) chain.setId(snapshot.getId());
                        return chain;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (chains.isEmpty()) {
                return CompletableFuture.completedFuture(chains);
            }

            List<CompletableFuture<Void>> mappingFutures = chains.stream()
                    .map(this::mapConnectedObjects) // Map language, level, items for each chain
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(mappingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> (Iterable<ConversationChain>) chains);
        }).exceptionally(ex -> {
            log.error("Error getting all conversation chains or mapping", ex);
            return List.of();
        });
    }


    // --- Mapping Helper Methods ---
    // IMPORTANT: These assume languageRepository and scenarioRepository have been updated
    // to return CompletableFuture<Optional<...>> (likely fetching from Firestore).

    private CompletableFuture<Void> mapConnectedObjects(Conversation conversation) {
        if (conversation == null) return CompletableFuture.completedFuture(null);

        // Fetch related objects asynchronously
        // Assumes ILanguageRepository and IScenarioRepository methods now return CompletableFuture

        CompletableFuture<Optional<Language>> langFuture = (conversation.getLanguage() == null) ? languageRepository.getLanguageById(conversation.getLanguageId()) : CompletableFuture.completedFuture(Optional.of(conversation.getLanguage()));
        CompletableFuture<Optional<LanguageLevel>> levelFuture = (conversation.getLanguageLevel() == null) ? languageRepository.getLanguageLevelById(conversation.getLevelId()) : CompletableFuture.completedFuture(Optional.of(conversation.getLanguageLevel()));
        CompletableFuture<Optional<Scenario>> scenarioFuture = (conversation.getScenario() == null) ? scenarioRepository.getScenarioById(conversation.getScenarioId()) : CompletableFuture.completedFuture(Optional.of(conversation.getScenario()));

        // Combine futures and set objects when all complete
        return CompletableFuture.allOf(langFuture, levelFuture, scenarioFuture)
                .thenAccept(voidResult -> {
                    // Use join() here because allOf ensures they are complete
                    if(conversation.getLanguage() == null){
                        langFuture.join().ifPresent(conversation::setLanguage);
                    }
                    if(conversation.getLanguageLevel() == null){
                        levelFuture.join().ifPresent(conversation::setLanguageLevel);
                    }
                    if(conversation.getScenario() == null){
                        scenarioFuture.join().ifPresent(conversation::setScenario);
                    }
                    updateConversation(conversation);
                }).exceptionally(ex -> {
                    log.error("Error mapping connected objects for Conversation {}", conversation.getId(), ex);
                    // Decide error handling: return null, throw, log?
                    return null;
                });
    }

    private CompletableFuture<Void> mapConnectedObjects(ConversationChain conversationChain) {
        if (conversationChain == null) return CompletableFuture.completedFuture(null);

        // Fetch Language and Level
        CompletableFuture<Optional<Language>> langFuture = (conversationChain.getLanguage() == null) ? languageRepository.getLanguageById(conversationChain.getLanguageId()) : CompletableFuture.completedFuture(Optional.of(conversationChain.getLanguage()));
        CompletableFuture<Optional<LanguageLevel>> levelFuture = (conversationChain.getLanguageLevel() == null) ? languageRepository.getLanguageLevelById(conversationChain.getLevelId()) : CompletableFuture.completedFuture(Optional.of(conversationChain.getLanguageLevel()));

        // Fetch associated ConversationChainItems from Firestore
        ApiFuture<QuerySnapshot> itemsQueryFuture =  conversationChainItemCollection
                .whereEqualTo("conversationChainId", conversationChain.getId())
                .orderBy("conversationIndex") // Assuming index determines order
                .get();
        CompletableFuture<QuerySnapshot> cfItemsQuery = (conversationChain.getConversations().isEmpty()) ? FirestoreFutureUtils.toCompletableFuture(itemsQueryFuture) : null;


        // Process items after query: map to objects and fetch their linked Conversation
        CompletableFuture<List<ConversationChainItem>> populatedItemsFuture = (cfItemsQuery != null) ? cfItemsQuery
                .thenCompose(querySnapshot -> {
                    List<ConversationChainItem> items = querySnapshot.getDocuments().stream()
                            .map(doc -> {
                                ConversationChainItem item = doc.toObject(ConversationChainItem.class);
                                if (item != null) item.setId(doc.getId());
                                return item;
                            })
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toList());

                    if (items.isEmpty()) {
                        return CompletableFuture.completedFuture(items); // No conversations to fetch
                    }

                    // Fetch the actual Conversation object for each item (using the Firestore version)
                    List<CompletableFuture<Void>> conversationFetchFutures = items.stream()
                            .map(item -> getConversationById(item.getConversationId()) // Recursive call to Firestore version
                                    .thenAccept(optConv -> optConv.ifPresent(item::setConversation))) // Set conversation on item if found
                            .collect(Collectors.toList());

                    // Return the list of items once all their conversations are fetched (or attempted)
                    return CompletableFuture.allOf(conversationFetchFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> items);
                })
                : CompletableFuture.completedFuture(conversationChain.getConversations());

        // Combine all futures (language, level, items)
        return CompletableFuture.allOf(langFuture, levelFuture, populatedItemsFuture)
                .thenAccept(voidResult -> {
                    langFuture.join().ifPresent(conversationChain::setLanguage);
                    levelFuture.join().ifPresent(conversationChain::setLanguageLevel);
                    // Set the fully populated list of items
                    conversationChain.setConversations(populatedItemsFuture.join());
                }).exceptionally(ex -> {
                    log.error("Error mapping connected objects for ConversationChain {}", conversationChain.getId(), ex);
                    return null;
                });
    }
}