package com.final_app.repositories.local;

import com.final_app.db.dao.UserLanguageDAO;
import com.final_app.interfaces.IUserLanguageRepository;
import com.final_app.models.UserLanguage;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalUserLanguageRepository implements IUserLanguageRepository {
    private UserLanguageDAO userLanguageDAO = new UserLanguageDAO();

    private static LocalUserLanguageRepository instance = null;

    public static IUserLanguageRepository getInstance() {
        if(instance == null){
            instance = new LocalUserLanguageRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addUserLanguage(UserLanguage userLanguage) {
        try {
            userLanguageDAO.insert(userLanguage);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null); // Return a completed CompletableFuture
    }

    @Override
    public CompletableFuture<Void> updateUserLanguage(UserLanguage userLanguage) {
        try {
            userLanguageDAO.update(userLanguage);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null); // Return a completed CompletableFuture
    }

    @Override
    public CompletableFuture<Optional<UserLanguage>> getUserLanguageById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.of(userLanguageDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<UserLanguage>> getUserLanguageByLanguageIdAndUserId(String languageId, String userId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userLanguageDAO.findByUserIdAndLanguageId(userId, languageId));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteUserLanguageById(String id) {
        try {
            userLanguageDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null); // Return a completed CompletableFuture
    }

    @Override
    public CompletableFuture<Iterable<UserLanguage>> getAllUserLanguagesFromUser(String userId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return userLanguageDAO.findByUserId(userId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}