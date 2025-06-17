package com.final_app.interfaces;

import com.final_app.models.Conversation;
import com.final_app.models.ConversationChain;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IConversationRepository {
    // Conversations
    CompletableFuture<Void> addConversation(Conversation conversation);
    CompletableFuture<Void> updateConversation(Conversation conversation);

    CompletableFuture<Optional<Conversation>> getConversationById(String id);

    CompletableFuture<Void> deleteConversationById(String id);

    CompletableFuture<Iterable<Conversation>> getAllConversations();
    CompletableFuture<Iterable<Conversation>> getAllConversationsByLanguage(String languageId);
    CompletableFuture<Iterable<Conversation>> getAllConversationsByLevel(String levelId);

    // ConversationChains
    CompletableFuture<Void> addConversationChain(ConversationChain conversationChain);
    CompletableFuture<Void> updateConversationChain(ConversationChain conversationChain);

    CompletableFuture<Optional<ConversationChain>> getConversationChainById(String id);

    CompletableFuture<Void> deleteConversationChainById(String id);

    CompletableFuture<Iterable<ConversationChain>> getAllConversationChains();
}
