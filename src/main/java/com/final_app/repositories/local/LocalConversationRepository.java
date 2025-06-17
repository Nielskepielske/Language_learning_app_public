package com.final_app.repositories.local;

import com.final_app.db.dao.ConversationChainDAO;
import com.final_app.db.dao.ConversationChainItemDAO;
import com.final_app.db.dao.ConversationDAO;
import com.final_app.interfaces.IConversationRepository;
import com.final_app.models.Conversation;
import com.final_app.models.ConversationChain;
import com.final_app.models.ConversationChainItem;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class LocalConversationRepository implements IConversationRepository {
    private ConversationDAO conversationDAO = new ConversationDAO();
    private ConversationChainDAO conversationChainDAO = new ConversationChainDAO();
    private ConversationChainItemDAO conversationChainItemDAO = new ConversationChainItemDAO();

    private static LocalConversationRepository instance = null;

    public static IConversationRepository getInstance() {
        if(instance == null){
            instance = new LocalConversationRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addConversation(Conversation conversation) {
        try {
            conversationDAO.insert(conversation);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateConversation(Conversation conversation) {
        try {
            conversationDAO.update(conversation);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<Conversation>> getConversationById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(conversationDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteConversationById(String id) {
        try {
            conversationDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<Conversation>> getAllConversations() {
        return CompletableFuture.supplyAsync(()->{
            try {
                //return conversationDAO.findAll();
                return conversationDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<Conversation>> getAllConversationsByLanguage(String languageId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return conversationDAO.findByLanguageId(languageId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<Conversation>> getAllConversationsByLevel(String levelId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return conversationDAO.findByLevelId(levelId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> addConversationChain(ConversationChain conversationChain) {
        try {
            conversationChainDAO.insert(conversationChain);
            for (ConversationChainItem conversationChainItem : conversationChain.getConversations()){
                conversationChainItem.setConversationChainId(conversationChain.getId());
                conversationChainItemDAO.insert(conversationChainItem);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateConversationChain(ConversationChain conversationChain) {
        try {
            conversationChainDAO.update(conversationChain);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<ConversationChain>> getConversationChainById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.of(conversationChainDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteConversationChainById(String id) {
        try {
            conversationChainDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<ConversationChain>> getAllConversationChains() {
        return CompletableFuture.supplyAsync(() ->{
            try {
                return conversationChainDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
