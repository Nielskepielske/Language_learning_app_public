package com.final_app.repositories.local;

import com.final_app.db.dao.UserConversationChainItemDAO;
import com.final_app.db.dao.UserConversationDAO;
import com.final_app.interfaces.IUserConversationsRepository;
import com.final_app.models.User;
import com.final_app.models.UserConversation;
import com.final_app.models.UserConversationChainItem;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalUserConversationRepository implements IUserConversationsRepository {
    private UserConversationDAO userConversationDAO = new UserConversationDAO();
    private UserConversationChainItemDAO userConversationChainItemDAO = new UserConversationChainItemDAO();

    private static LocalUserConversationRepository instance = null;

    public static IUserConversationsRepository getInstance() {
        if(instance == null){
            instance = new LocalUserConversationRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addUserConversation(UserConversation userConversation) {
        try {
            userConversationDAO.insert(userConversation);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateUserConversation(UserConversation userConversation) {
        try {
            userConversationDAO.update(userConversation);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<UserConversation>> getUserConversationById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userConversationDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<UserConversation>> getUserConversationsByUserAndConversationId(String userId, String conversationId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return userConversationDAO.findByUserIdAndConversationId(userId, conversationId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteUserConversationById(String id) {
        try {
            userConversationDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<UserConversation>> getAllUserConversationsFromUser(String userId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return userConversationDAO.findByUserId(userId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<UserConversationChainItem>> getUserConversationChainItemByUserConversationId(String userConversationId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userConversationChainItemDAO.findByUserConversationId(userConversationId));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> addUserConversationChainItem(UserConversationChainItem userConversationChainItem) {
        try {
            userConversationChainItemDAO.insert(userConversationChainItem);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<UserConversationChainItem>> getUserConversationChainItemById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userConversationChainItemDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteUserConversationChainItemById(String id) {
        try {
            userConversationChainItemDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<UserConversationChainItem>> getAllUserConversationChainItemsFromUser(User user) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<UserConversationChainItem>> getAllUserConversationChainItemsFromConversationChainId(String conversationId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return userConversationChainItemDAO.findByConversationChainId(conversationId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}