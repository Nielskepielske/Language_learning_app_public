package com.final_app.repositories.firebase;

import com.final_app.interfaces.ISettingsRepository;
import com.final_app.models.Settings;
import com.final_app.repositories.firebase.utils.FirebaseUtils;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FBSettingsRepository implements ISettingsRepository {
    private static final String COLLECTION_NAME = "settings";
    private static FBSettingsRepository instance = null;
    private final CollectionReference settingsCollection;

    public FBSettingsRepository() {
        if (instance == null) {
            instance = this;
        }
        settingsCollection = FirebaseManager.getDb().collection(COLLECTION_NAME);
    }
    public static ISettingsRepository getInstance() {
        if (instance == null) {
            instance = new FBSettingsRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> saveSettings(Settings settings) {
       if(settings.getId() == null || settings.getId().isEmpty()) {
           settings.setId(UUID.randomUUID().toString());
        }
        ApiFuture<WriteResult> future = settingsCollection.document().set(settings);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        throw new RuntimeException("Error saving settings", ex);
                    }
                });
    }

    @Override
    public CompletableFuture<Optional<Settings>> getSettingsFromUser(String userId) {
        ApiFuture<QuerySnapshot> future = settingsCollection.whereEqualTo("userId", userId).get();

        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenCompose(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    Settings settings = document.toObject(Settings.class);
                    settings.setId(document.getId());
                    return CompletableFuture.completedFuture(Optional.of(settings));
                });
    }
}
