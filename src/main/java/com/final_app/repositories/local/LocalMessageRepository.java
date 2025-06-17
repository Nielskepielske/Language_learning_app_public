package com.final_app.repositories.local;

import com.final_app.db.dao.MessageDAO;
import com.final_app.interfaces.IMessageRepository;
import com.final_app.models.Message;

import javax.swing.text.html.Option;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalMessageRepository implements IMessageRepository {
    private MessageDAO messageDAO = new MessageDAO();

    private static LocalMessageRepository instance = null;

    public static IMessageRepository getInstance() {
        if(instance == null)
            instance = new LocalMessageRepository();
        return instance;
    }

    @Override
    public CompletableFuture<Void> addMessage(Message message) {
        try {
            messageDAO.insert(message);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateMessage(Message message) {
        try {
            messageDAO.update(message);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<Message>> getMessageById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(messageDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteMessageById(String id) {
        try {
            messageDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<Message>> getAllMessages() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return messageDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<Message>> getAllMessagesFromUserConversation(String userConversationId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return messageDAO.findByUserConversationId(userConversationId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
