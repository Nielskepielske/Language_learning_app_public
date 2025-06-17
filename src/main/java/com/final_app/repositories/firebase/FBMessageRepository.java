package com.final_app.repositories.firebase;

import com.final_app.interfaces.IMessageRepository;
import com.final_app.models.Message;
import com.final_app.repositories.firebase.utils.FirestoreFutureUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FBMessageRepository implements IMessageRepository {

    private static final Logger log = LoggerFactory.getLogger(FBMessageRepository.class);
    private static final String COLLECTION_NAME = "messages";
    private final CollectionReference messageCollection;

    private static FBMessageRepository instance = null;

    public FBMessageRepository() {
        if(instance == null){
            instance = this;
        }
        Firestore db = FirebaseManager.getDb();
        this.messageCollection = db.collection(COLLECTION_NAME);
    }

    public static IMessageRepository getInstance() {
        if(instance == null){
            return new FBMessageRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addMessage(Message message) {
        String id = (message.getId() == null || message.getId().isEmpty()) ?
                messageCollection.document().getId() : message.getId();
        message.setId(id);
        ApiFuture<WriteResult> future = messageCollection.document(id).set(message);
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error adding message {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> updateMessage(Message message) {
        if (message.getId() == null || message.getId().isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Message ID missing for update."));
        }
        ApiFuture<WriteResult> future = messageCollection.document(message.getId()).set(message, SetOptions.merge());
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error updating message {}", message.getId(), ex); });
    }

    @Override
    public CompletableFuture<Optional<Message>> getMessageById(String id) {
        ApiFuture<DocumentSnapshot> future = messageCollection.document(id).get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(snapshot -> {
                    if (snapshot.exists()) {
                        Message msg = snapshot.toObject(Message.class);
                        if (msg != null) msg.setId(snapshot.getId());
                        return Optional.ofNullable(msg);
                    }
                    return Optional.<Message>empty();
                })
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting message by ID {}", id, ex); });
    }

    @Override
    public CompletableFuture<Void> deleteMessageById(String id) {
        ApiFuture<WriteResult> future = messageCollection.document(id).delete();
        return FirestoreFutureUtils.toVoidCompletableFuture(future)
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error deleting message {}", id, ex); });
    }

    @Override
    public CompletableFuture<Iterable<Message>> getAllMessages() {
        ApiFuture<QuerySnapshot> future = messageCollection.orderBy("timestamp").get(); // Example: order by timestamp
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<Message>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    Message msg = snapshot.toObject(Message.class);
                                    if (msg != null) msg.setId(snapshot.getId());
                                    return msg;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> { if (ex != null) log.error("Error getting all messages", ex); });
    }

    @Override
    public CompletableFuture<Iterable<Message>> getAllMessagesFromUserConversation(String userConversationId) {
        ApiFuture<QuerySnapshot> future = messageCollection
                .whereEqualTo("userConversationId", userConversationId) // Adjust field if needed
                .orderBy("timestamp") // Order messages chronologically
                .get();
        return FirestoreFutureUtils.toCompletableFuture(future)
                .thenApply(querySnapshot ->
                        (Iterable<Message>) querySnapshot.getDocuments().stream()
                                .map(snapshot -> {
                                    Message msg = snapshot.toObject(Message.class);
                                    if (msg != null) msg.setId(snapshot.getId());
                                    return msg;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Error getting messages for UserConversationId {}", userConversationId, ex);
                });
    }
}