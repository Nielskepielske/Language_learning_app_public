package com.final_app.repositories.local;

import com.final_app.db.dao.UserSpeakingTestDAO;
import com.final_app.interfaces.IUserSpeakingTestRepository;
import com.final_app.models.UserSpeakingTest;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalUserSpeakingTestRepository implements IUserSpeakingTestRepository {
    private UserSpeakingTestDAO userSpeakingTestDAO = new UserSpeakingTestDAO();

    private static LocalUserSpeakingTestRepository instance = null;

    public static IUserSpeakingTestRepository getInstance() {
        if (instance == null) {
            instance = new LocalUserSpeakingTestRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addUserSpeakingTest(UserSpeakingTest userSpeakingTest) {
        try {
            userSpeakingTestDAO.insert(userSpeakingTest);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateUserSpeakingTest(UserSpeakingTest userSpeakingTest) {
        try {
            userSpeakingTestDAO.update(userSpeakingTest);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<UserSpeakingTest>> getUserSpeakingTestById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userSpeakingTestDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<UserSpeakingTest>> getUserSpeakingTestByUserIdAndTestId(String userId, String testId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userSpeakingTestDAO.findByUserIdAndTestId(userId, testId));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteUserSpeakingTestById(String id) {
        try {
            userSpeakingTestDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<UserSpeakingTest>> getAllUserSpeakingTestsFromUser(String userId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return userSpeakingTestDAO.findByUserId(userId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
