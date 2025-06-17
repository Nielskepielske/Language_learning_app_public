package com.final_app.repositories.local;

import com.final_app.db.dao.*;
import com.final_app.interfaces.IUserRepository;
import com.final_app.models.User;
import com.final_app.models.UserStats;
import com.final_app.services.AppService;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalUserRepository implements IUserRepository {
    private UserDAO userDAO = new UserDAO();
    private UserLanguageDAO userLanguageDAO = new UserLanguageDAO();
    private UserConversationDAO userConversationDAO = new UserConversationDAO();
    private UserStatsDAO userStatsDAO = new UserStatsDAO();
    private UserSpeakingTestDAO userSpeakingTestDAO = new UserSpeakingTestDAO();

    private static LocalUserRepository instance = null;

    public static IUserRepository getInstance() {
        if (instance == null) {
            instance = new LocalUserRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addUser(User user) {
        try {
            userDAO.insert(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateUser(User user) {
        try {
            userDAO.update(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<User>> getUserById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<User>> getUserByUsername(String username) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userDAO.findAll().stream().filter(u -> u.getUserName().equals(username)).findFirst().orElse(null));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<User>> getUserByEmail(String email) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userDAO.findByEmail(email));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteUserById(String id) {
        try {
            userDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<User>> getAllUsers() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return userDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> emailExists(String email) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return userDAO.emailExists(email);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> usernameExists(String username) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return userDAO.usernameExists(username);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveUserStats(User user, UserStats userStats) {
        try {
            UserStats existing = userStatsDAO.findByUserId(user.getId());
            if(existing != null){
                userStats.setId(existing.getId());
                userStatsDAO.update(userStats);
            }else{
                userStatsDAO.insert(userStats);
            }
        } catch (SQLException e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<UserStats>> getUserStatsById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(userStatsDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<UserStats>> getUserStatsByUserId(String userId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                User localUser = getUserById(userId)
                        .thenApply(u -> u.orElse(null))
                        .get();
                if(localUser != null){
                    UserStats userStats =userStatsDAO.findByUserId(userId);
                    if(userStats == null){
                        UserStats newUserStats = new UserStats(userId, 1, 0, 0);
                        saveUserStats(AppService.getInstance().getCurrentUser(), newUserStats)
                                .thenApply(u->{
                                    return Optional.ofNullable(newUserStats);
                                });
                    }else{
                        return Optional.of(userStats);
                    }
                }
            } catch (SQLException e) {
                //throw new RuntimeException(e);
                return Optional.empty();
            } catch (Exception e){
                e.printStackTrace();
                return Optional.empty();
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Void> deleteUserStatsById(String id) {
        try {
            userStatsDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteUserStatsByUserId(String userId) {
        try {
            userStatsDAO.deleteByUserId(userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<UserStats>> getAllUserStats() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return userStatsDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
