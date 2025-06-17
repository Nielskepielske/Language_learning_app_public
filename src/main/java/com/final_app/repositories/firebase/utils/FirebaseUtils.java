package com.final_app.repositories.firebase.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FirebaseUtils {
    /**
     *
     * @param ref
     * @param itemClass
     * @return
     * @param <T>
     */
    public static <T> CompletableFuture<List<T>> readListOnce(DatabaseReference ref, Class<T> itemClass) {
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        List<T> list = new ArrayList<>();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    T item = childSnapshot.getValue(itemClass);
                    if (item != null) {
                        list.add(item);
                    }
                }
                future.complete(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        };

        ref.addListenerForSingleValueEvent(listener);
        return future;
    }

    /**
     *
     * @param ref
     * @param filterKey
     * @param filterValue
     * @param itemClass
     * @return
     * @param <T>
     */
    public static <T> CompletableFuture<List<T>> readFilteredListOnce(
            DatabaseReference ref,
            String filterKey,
            Object filterValue,
            Class<T> itemClass) {
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        List<T> list = new ArrayList<>();

        Query filteredQuery = ref.orderByChild(filterKey).equalTo(filterValue.toString());

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    T item = childSnapshot.getValue(itemClass);
                    if (item != null) {
                        list.add(item);
                    }
                }
                future.complete(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        };

        filteredQuery.addListenerForSingleValueEvent(listener);
        return future;
    }
}
