package com.final_app.repositories.firebase;

import com.final_app.interfaces.IScenarioRepository;
import com.final_app.models.Scenario;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBScenarioRepository implements IScenarioRepository {

    private static final Logger log = LoggerFactory.getLogger(FBScenarioRepository.class);
    private static final String COLLECTION_NAME = "scenarios";
    private final CollectionReference scenarioCollection;

    private static FBScenarioRepository instance = null;

    public FBScenarioRepository() {
        if (instance == null) {
            instance = this;
        }
        Firestore db = FirebaseManager.getDb();
        this.scenarioCollection = db.collection(COLLECTION_NAME);
    }

    public static IScenarioRepository getInstance() {
        if (instance == null) {
            instance = new FBScenarioRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addScenario(Scenario scenario) {
        String id = (scenario.getId() == null || scenario.getId().isEmpty()) ?
                scenarioCollection.document().getId() : scenario.getId();
        scenario.setId(id);
        ApiFuture<WriteResult> future = scenarioCollection.document(id).set(scenario);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding scenario {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateScenario(Scenario scenario) {
        if (scenario.getId() == null || scenario.getId().isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Scenario ID missing for update."));
        }
        ApiFuture<WriteResult> future = scenarioCollection.document(scenario.getId()).set(scenario, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating scenario {}", scenario.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<Scenario>> getScenarioById(String id) {
        ApiFuture<DocumentSnapshot> future = scenarioCollection.document(id).get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(snapshot -> {
                    if (snapshot.exists()) {
                        Scenario s = snapshot.toObject(Scenario.class);
                        if (s != null) s.setId(snapshot.getId());
                        return Optional.ofNullable(s);
                    }
                    return Optional.<Scenario>empty();
                })
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting scenario by ID {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> deleteScenarioById(String id) {
        ApiFuture<WriteResult> future = scenarioCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting scenario {}", id, ex); });
    }

    @Override
    public CompletableFuture<Iterable<Scenario>> getAllScenarios() {
        ApiFuture<QuerySnapshot> future = scenarioCollection.get(); // Example order
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<Scenario>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    Scenario s = snapshot.toObject(Scenario.class);
                                    if (s != null) s.setId(snapshot.getId());
                                    return s;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting all scenarios", ex); });
    }
}