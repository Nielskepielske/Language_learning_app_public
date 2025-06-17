package com.final_app.repositories.local;

import com.final_app.db.dao.UserSpeakingTestResponseDAO;
import com.final_app.interfaces.IResponseRepository;
import com.final_app.models.UserSpeakingTestResponse;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalResponseRepository implements IResponseRepository {
    private UserSpeakingTestResponseDAO responseDAO = new UserSpeakingTestResponseDAO();

    private static LocalResponseRepository instance = null;

    public static IResponseRepository getInstance() {
        if(instance == null){
            instance = new LocalResponseRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addResponse(UserSpeakingTestResponse response) {
        try {
            responseDAO.insert(response);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateResponse(UserSpeakingTestResponse response) {
        try {
            responseDAO.update(response);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<UserSpeakingTestResponse>> getResponseById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(responseDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteResponseById(String id) {
        try {
            responseDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<UserSpeakingTestResponse>> getAllResponses() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return responseDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<UserSpeakingTestResponse>> getAllResponsesFromUserTest(String userTestId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return responseDAO.findByUserSpeakingTestId(userTestId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
