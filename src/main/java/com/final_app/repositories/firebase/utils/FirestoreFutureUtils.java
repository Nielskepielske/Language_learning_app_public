package com.final_app.repositories.firebase.utils; // Or appropriate package

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors; // Use appropriate executor

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FirestoreFutureUtils {

    // Use a direct executor or inject a specific one if needed
    private static final Executor executor = MoreExecutors.directExecutor();

    public static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        ApiFutures.addCallback(apiFuture, new ApiFutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                completableFuture.complete(result);
            }

            @Override
            public void onFailure(Throwable t) {
                completableFuture.completeExceptionally(t);
            }
        }, executor); // Use desired executor
        return completableFuture;
    }

    // Helper to convert WriteResult future to CompletableFuture<Void>
    public static CompletableFuture<Void> toVoidCompletableFuture(ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture) {
        return toCompletableFuture(apiFuture).thenApply(writeResult -> null); // Discard WriteResult
    }
}
