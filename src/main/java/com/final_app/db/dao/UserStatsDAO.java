package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.UserStats;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data Access Object for the user_stats table
 */
public class UserStatsDAO {
    /**
     * Insert a new user stats record into the database
     */
    public void insert(UserStats userStats) throws SQLException {
        String sql = "INSERT INTO user_stats (id, user_id, level, total_xp, streak) VALUES (?, ?, ?, ?, ?)";

        if(userStats.getId() == null || userStats.getId().isEmpty()) {
            // Generate a new UUID if the ID is not provided
            userStats.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, userStats.getId());
            pstmt.setString(2, userStats.getUserId());
            pstmt.setInt(3, userStats.getLevel());
            pstmt.setLong(4, userStats.getTotalXp());
            pstmt.setInt(5, userStats.getStreak());

            pstmt.executeUpdate();
        }
    }

    /**
     * Find user stats by ID
     */
    public UserStats findById(String id) throws SQLException {
        String sql = "SELECT * FROM user_stats WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserStats(rs);
                }
                return null;
            }
        }
    }

    /**
     * Find user stats by user ID
     */
    public UserStats findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM user_stats WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserStats(rs);
                }
                return null;
            }
        }
    }

    /**
     * Get all user stats from the database
     */
    public List<UserStats> findAll() throws SQLException {
        String sql = "SELECT * FROM user_stats";
        List<UserStats> userStatsList = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userStatsList.add(mapResultSetToUserStats(rs));
            }
        }

        return userStatsList;
    }

    /**
     * Update an existing user stats record
     */
    public void update(UserStats userStats) throws SQLException {
        String sql = "UPDATE user_stats SET user_id = ?, level = ?, total_xp = ?, streak = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userStats.getUserId());
            pstmt.setInt(2, userStats.getLevel());
            pstmt.setLong(3, userStats.getTotalXp());
            pstmt.setInt(4, userStats.getStreak());
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(6, userStats.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a user stats record by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM user_stats WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete user stats by user ID
     */
    public void deleteByUserId(String userId) throws SQLException {
        String sql = "DELETE FROM user_stats WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        }
    }
    public int getUserStreak(String userId) throws SQLException {
        // Get today's date
        LocalDateTime today = LocalDateTime.now();

        // Create a set to hold all dates with activity
        Set<LocalDateTime> activityDays = new HashSet<>();

        // SQL to get all dates with activity
        String sql = "SELECT DISTINCT updated_at " +
                "FROM user_conversations " +
                "WHERE user_id = ? " +
                "ORDER BY updated_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Convert SQL date to LocalDate
                    LocalDateTime date = rs.getTimestamp("updated_at").toLocalDateTime();
                    activityDays.add(date);
                }
            }
        }

        // Calculate streak
        LocalDateTime checkDate = today;
        int streakCount = 0;

        // If today has no activity, check if yesterday does before ending streak
        if (!activityDays.contains(today)) {
            LocalDateTime yesterday = today.minusDays(1);
            if (!activityDays.contains(yesterday)) {
                return 0; // No recent activity
            }
            // Start checking from yesterday instead
            checkDate = yesterday;
        }

        // Count consecutive days with activity
        while (activityDays.contains(checkDate)) {
            streakCount++;
            checkDate = checkDate.minusDays(1);
        }

        return streakCount;
    }

    /**
     * Helper method to map ResultSet to UserStats
     */
    private UserStats mapResultSetToUserStats(ResultSet rs) throws SQLException {
        UserStats userStats = new UserStats();
        userStats.setId(rs.getString("id"));
        userStats.setUserId(rs.getString("user_id"));
        userStats.setLevel(rs.getInt("level"));
        userStats.setTotalXp(rs.getLong("total_xp"));
        userStats.setStreak(rs.getInt("streak"));
        userStats.setLastUpdate(rs.getTimestamp("last_updated"));
        return userStats;
    }
}
