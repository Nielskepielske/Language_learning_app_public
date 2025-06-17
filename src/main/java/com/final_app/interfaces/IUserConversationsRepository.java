package com.final_app.interfaces;

import com.final_app.models.User;
import com.final_app.models.UserConversation;
import com.final_app.models.UserConversationChainItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IUserConversationsRepository {
    CompletableFuture<Void> addUserConversation(UserConversation userConversation);
    CompletableFuture<Void> updateUserConversation(UserConversation userConversation);
    CompletableFuture<Optional<UserConversation>> getUserConversationById(String id);
    CompletableFuture<Void> deleteUserConversationById(String id);
    CompletableFuture<Iterable<UserConversation>> getAllUserConversationsFromUser(String userId);
    CompletableFuture<Iterable<UserConversation>> getUserConversationsByUserAndConversationId(String userId, String conversationId);

    CompletableFuture<Void> addUserConversationChainItem(UserConversationChainItem userConversationChainItem);
    CompletableFuture<Optional<UserConversationChainItem>> getUserConversationChainItemById(String id);
    CompletableFuture<Optional<UserConversationChainItem>> getUserConversationChainItemByUserConversationId(String userConversationId);

    CompletableFuture<Void> deleteUserConversationChainItemById(String id);
    CompletableFuture<List<UserConversationChainItem>> getAllUserConversationChainItemsFromUser(User user);
    CompletableFuture<List<UserConversationChainItem>> getAllUserConversationChainItemsFromConversationChainId(String conversationChainId);
}
