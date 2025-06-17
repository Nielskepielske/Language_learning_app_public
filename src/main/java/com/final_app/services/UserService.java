package com.final_app.services;

import com.final_app.db.DatabaseManager;
import com.final_app.db.dao.UserDAO;
import com.final_app.db.dao.UserStatsDAO;
import com.final_app.events.EventBus;
import com.final_app.events.XpEarnedEvent;
import com.final_app.factories.RepositoryFactory;
import com.final_app.interfaces.ISettingsRepository;
import com.final_app.interfaces.IUserRepository;
import com.final_app.models.*;
import com.final_app.tools.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Enhanced UserService that preserves original functionality while adding improved authentication features
 */
public class UserService {
    private final XpService xpService = new XpService();

    // Cache recently earned XP for analytics and UI feedback
    private final Map<String, XpTransaction> recentXpTransactions = new HashMap<>();

    /**
     * Register a new user with initial stats - enhanced with better password handling
     */
    public User registerUser(String userName, String email, String password) throws SQLException {
        try {
            // Check if email already exists
            if (RepositoryFactory.getUserRepository().emailExists(email).get()) {
                throw new SQLException("Email already registered");
            }

            // Check if username already exists
            if (RepositoryFactory.getUserRepository().usernameExists(userName).get()) {
                throw new SQLException("Username already taken");
            }

            // Hash password for security
            String hashedPassword = PasswordUtil.simpleHash(password);

            // Create user
            User user = new User(userName, email, hashedPassword, null);
            RepositoryFactory.getUserRepository().addUser(user);

            // Create initial user stats
            UserStats stats = new UserStats(user.getId(), 1, 0, 0);
            RepositoryFactory.getUserRepository().saveUserStats(user, stats);

            return user;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a user by their ID
     */
    public User getUserById(String id) throws SQLException {
        try{
            return RepositoryFactory.getUserRepository().getUserById(id).get().orElseThrow();
        }catch (Exception e){
            System.out.println("User not found");
            return null;
        }
    }

    /**
     * Get a user by their email
     */
    public User getUserByEmail(String email) throws SQLException {
        try{
            return RepositoryFactory.getUserRepository().getUserByEmail(email).get().orElseThrow();
        }catch (Exception e){
            System.out.println("User not found");
            return null;
        }
    }

    /**
     * Authenticate a user with email and password
     * Returns the user if authentication is successful, null otherwise
     */
    public User authenticateUser(String email, String password) throws SQLException {
        User user = getUserByEmail(email);

        if (user == null) {
            return null; // User not found
        }

        // Check if password matches
        // Using simple comparison for now, but should be replaced with proper hashing
        // in a production environment
        String hashedPassword = PasswordUtil.simpleHash(password);

        if (hashedPassword.equals(user.getPassword())) {
            return user;
        }

        return null; // Password doesn't match
    }

    /**
     * Update user profile information
     */
    public void updateUserProfile(User user) throws SQLException {
        RepositoryFactory.getUserRepository().updateUser(user);
    }

    /**
     * Update user password
     */
    public void updateUserPassword(String userId, String currentPassword, String newPassword)
            throws SQLException, IllegalArgumentException {

        User user = getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Verify current password
        String hashedCurrentPassword = PasswordUtil.simpleHash(currentPassword);
        if (!hashedCurrentPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update to new password
        String hashedNewPassword = PasswordUtil.simpleHash(newPassword);
        user.setPassword(hashedNewPassword);
        RepositoryFactory.getUserRepository().updateUser(user);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() throws SQLException {
        return (List<User>) RepositoryFactory.getUserRepository().getAllUsers();
    }

    /**
     * Delete a user and all associated data
     */
    public void deleteUser(String userId) throws SQLException {
        RepositoryFactory.getUserRepository().deleteUserById(userId);
        // Foreign key cascade will handle related records
    }

    /**
     * Get user statistics
     */
    public UserStats getUserStats(String userId) throws SQLException {
        try{
            return RepositoryFactory.getUserRepository().getUserStatsByUserId(userId).get().orElseThrow();
        }catch (Exception e){
            System.out.println("User stats not found");
            return null;
        }
    }

    /**
     * Update user statistics
     */
    public void updateUserStats(UserStats stats) throws SQLException {
        RepositoryFactory.getUserRepository().saveUserStats(getUserById(stats.getUserId()), stats);
    }

    /**
     *
     * @param settings
     * @throws SQLException
     */
    public void saveUserSettings(Settings settings) throws SQLException {
        assert RepositoryFactory.getSettingsRepository() != null;
        RepositoryFactory.getSettingsRepository().saveSettings(settings);
    }

    /**
     *
     * @param userId
     * @return
     * @throws SQLException
     */
    public CompletableFuture<Settings> getUserSettings(String userId) throws SQLException {
        return RepositoryFactory.getSettingsRepository().getSettingsFromUser(userId)
                .thenApply(s -> {
                    if(s.isPresent()){
                        return s.get();
                    }
                    return null;
                })
                .whenComplete((settings, throwable) -> {});
    }

    /**
     * Add XP to a user's account
     * Simplified version for backward compatibility
     */
    public void addXp(String userId, long xpAmount) throws SQLException {
        addXp(userId, xpAmount, "GENERAL", "XP earned");
    }

    /**
     * Add XP to a user's account with detailed source information
     * Enhanced version with XP transaction tracking
     */
    public XpTransaction addXp(String userId, long xpAmount, String source, String description) throws SQLException {
        try{

            UserStats stats = RepositoryFactory.getUserRepository().getUserStatsByUserId(userId).get().orElse(null);
            if (stats == null) {
                return null;
            }

            long oldXp = stats.getTotalXp();
            int oldLevel = stats.getLevel();

            // Add XP
            stats.setTotalXp(oldXp + xpAmount);

            // Calculate new level based on total XP
            int newLevel = xpService.calculateLevelFromXp(stats.getTotalXp());
            stats.setLevel(newLevel);

            // Update in database
            RepositoryFactory.getUserRepository().saveUserStats(getUserById(userId), stats);

            // Create XP transaction record
            XpTransaction transaction = new XpTransaction(
                    userId,
                    xpAmount,
                    source,
                    description,
                    LocalDateTime.now(),
                    oldLevel,
                    newLevel,
                    oldLevel < newLevel
            );

            // Store in recent transactions cache
            recentXpTransactions.put(userId, transaction);

            // Fire XP earned event for listeners (UI updates, etc.)
            fireXpEarnedEvent(transaction);

            return transaction;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get most recent XP transaction for a user
     */
    public XpTransaction getRecentXpTransaction(String userId) {
        return recentXpTransactions.get(userId);
    }

    public int getUserStreak(String userId) {
        try{
            return RepositoryFactory.getUserRepository().getUserStatsByUserId(userId).get().orElseThrow(()-> new RuntimeException("no userStatsfound")).getStreak();
        }catch (IllegalArgumentException e){
            System.out.println("User not found");
            return 0;
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * Increment user streak by 1
     */
    public void incrementStreak(String userId) throws SQLException {
        try{
            UserStats stats = RepositoryFactory.getUserRepository().getUserStatsByUserId(userId).get().orElse(null);
            if (stats != null) {
                int oldStreak = stats.getStreak();
                stats.setStreak(oldStreak + 1);
                RepositoryFactory.getUserRepository().saveUserStats(getUserById(userId), stats);

                // Award streak bonus XP (new functionality)
                if (xpService != null) {
                    int streakBonus = xpService.calculateStreakBonus(stats.getStreak());
                    if (streakBonus > 0) {
                        addXp(userId, streakBonus, "STREAK",
                                "Day streak: " + stats.getStreak() + " days");
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Reset user streak to 0
     */
    public void resetStreak(String userId) throws SQLException {
        try{
            UserStats stats = RepositoryFactory.getUserRepository().getUserStatsByUserId(userId).get().orElse(null);
            if (stats != null) {
                stats.setStreak(0);
                RepositoryFactory.getUserRepository().saveUserStats(getUserById(userId), stats);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }


    /**
     * Check how many days in a row a user has completed a conversation or speaking test
     */
    public boolean checkAndUpdateDailyStreak(String userId) throws SQLException {
        try {
            List<Date> dates = new ArrayList<>();
            List<UserConversation> userConversations = AppService.getInstance().getConversationService().getUserConversations(userId);
            List<UserSpeakingTest> userSpeakingTests = AppService.getInstance().getSpeakingTestService().getUserSpeakingTests(userId);

            for (UserSpeakingTest test : userSpeakingTests) {
                if (test.getCompletedAt() != null && dates.stream().noneMatch(d -> isSameDay(d, test.getCompletedAt()))) {
                    dates.add(test.getCompletedAt());
                }
            }
            for (UserConversation convo : userConversations) {
                if (convo.getCompletedAt() != null && dates.stream().noneMatch(d -> isSameDay(d, convo.getCompletedAt()))) {
                    dates.add(convo.getCompletedAt());
                }
            }

            List<Date> sortedDates = dates.stream()
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toCollection(LinkedList::new));

            Date currentDate = new Date();
            int streak = 0;
            while (!sortedDates.isEmpty()) {
                if (isSameDay(sortedDates.getFirst(), currentDate)) {
                    sortedDates.removeFirst();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(currentDate);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    currentDate = cal.getTime();
                    streak++;
                } else {
                    break;
                }
            }

            CompletableFuture<Optional<UserStats>> userStatsFuture = RepositoryFactory.getUserRepository().getUserStatsByUserId(userId);

            int finalStreak = streak;
            userStatsFuture.thenAccept(userStats -> {
               userStats.ifPresent(stats -> {
                   stats.setStreak(finalStreak);
                   RepositoryFactory.getUserRepository().saveUserStats(AppService.getInstance().getCurrentUser(), stats);
               });
            });

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        LocalDate today = LocalDate.now();


        // Already logged in today, no streak change
        return true;
    }

    /**
     * Get estimated XP required for next level
     */
    public long getXpRequiredForNextLevel(String userId) throws SQLException {
        try{
            UserStats stats = RepositoryFactory.getUserRepository().getUserStatsByUserId(userId).get().orElse(null);
            if (stats == null || xpService == null) {
                return 0;
            }

            return xpService.calculateXpForNextLevel(stats.getLevel());
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * Get progress percentage to next level
     */
    public double getProgressToNextLevel(String userId) throws SQLException {
        try{
            UserStats stats = RepositoryFactory.getUserRepository().getUserStatsByUserId(userId).get().orElse(null);
            if (stats == null || xpService == null) {
                return 0;
            }

            return xpService.calculateProgressToNextLevel(
                    stats.getTotalXp(), stats.getLevel());
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * Get estimated days to next level at current XP earning rate
     */
    public int getEstimatedDaysToNextLevel(String userId) throws SQLException {
        try{

            // This would analyze user's XP history to calculate average daily XP
            // For demonstration, we'll use a simplified approach
            UserStats stats = RepositoryFactory.getUserRepository().getUserStatsByUserId(userId).get().orElse(null);
            if (stats == null || xpService == null) {
                return 0;
            }

            long xpNeeded = xpService.calculateXpForNextLevel(stats.getLevel()) - stats.getTotalXp();

            // Assume average 100 XP per day (would be calculated from history)
            int averageDailyXp = 100;

            return (int) Math.ceil((double) xpNeeded / averageDailyXp);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    // Fire event for UI components to update
    private void fireXpEarnedEvent(XpTransaction transaction) {
        // In a full implementation, this would use JavaFX event system
        // Example placeholder:
        XpEarnedEvent event = new XpEarnedEvent(transaction);

        EventBus.getInstance().post(event);
    }


    /**
     * Calculate level based on total XP (original method for backward compatibility)
     */
    private int calculateLevel(long totalXp) {
        // Example formula: level = 1 + floor(sqrt(totalXp / 100))
        // This creates a gradually increasing XP requirement for each level
        return 1 + (int) Math.floor(Math.sqrt(totalXp / 100.0));
    }

    // Inner class to represent XP transactions for tracking and UI
    public static class XpTransaction {
        private final String userId;
        private final long amount;
        private final String source;
        private final String description;
        private final LocalDateTime timestamp;
        private final int oldLevel;
        private final int newLevel;
        private final boolean leveledUp;

        public XpTransaction(String userId, long amount, String source,
                             String description, LocalDateTime timestamp,
                             int oldLevel, int newLevel, boolean leveledUp) {
            this.userId = userId;
            this.amount = amount;
            this.source = source;
            this.description = description;
            this.timestamp = timestamp;
            this.oldLevel = oldLevel;
            this.newLevel = newLevel;
            this.leveledUp = leveledUp;
        }

        // Getters
        public String getUserId() { return userId; }
        public long getAmount() { return amount; }
        public String getSource() { return source; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getOldLevel() { return oldLevel; }
        public int getNewLevel() { return newLevel; }
        public boolean isLeveledUp() { return leveledUp; }
    }
}