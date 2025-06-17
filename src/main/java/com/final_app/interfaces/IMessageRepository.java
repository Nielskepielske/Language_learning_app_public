package com.final_app.interfaces;

import com.final_app.models.Message;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IMessageRepository {
    CompletableFuture<Void> addMessage(Message message);
    CompletableFuture<Void> updateMessage(Message message);
    CompletableFuture<Optional<Message>> getMessageById(String id);
    CompletableFuture<Void> deleteMessageById(String id);
    CompletableFuture<Iterable<Message>> getAllMessages();
    CompletableFuture<Iterable<Message>> getAllMessagesFromUserConversation(String userConversationId);
}
