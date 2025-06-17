package com.final_app.interfaces;

import com.final_app.models.User;
import com.final_app.models.UserStats;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IUserRepository {
    // User
    CompletableFuture<Void> addUser(User user);
    CompletableFuture<Void> updateUser(User user);

    CompletableFuture<Optional<User>> getUserById(String id);
    CompletableFuture<Optional<User>> getUserByUsername(String username);
    CompletableFuture<Optional<User>> getUserByEmail(String email);

    CompletableFuture<Void> deleteUserById(String id);

    CompletableFuture<List<User>> getAllUsers();

    CompletableFuture<Boolean> emailExists(String email);
    CompletableFuture<Boolean> usernameExists(String username);

    // UserStats
    CompletableFuture<Void> saveUserStats(User user, UserStats userStats);
    CompletableFuture<Optional<UserStats>> getUserStatsById(String id);
    CompletableFuture<Optional<UserStats>> getUserStatsByUserId(String userId);
    CompletableFuture<Void> deleteUserStatsById(String id);
    CompletableFuture<Void> deleteUserStatsByUserId(String userId);
    CompletableFuture<List<UserStats>> getAllUserStats();
}
