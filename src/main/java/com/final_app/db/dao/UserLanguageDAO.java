package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.UserLanguage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for the user_languages table
 */
public class UserLanguageDAO {
    private final LanguageDAO languageDAO = new LanguageDAO();
    private final LanguageLevelDAO languageLevelDAO = new LanguageLevelDAO();

    /**
     * Insert a new user language into the database
     */
    public void insert(UserLanguage userLanguage) throws SQLException {
        String sql = "INSERT INTO user_languages (id, user_id, language_id, level_id, xp) VALUES (?, ?, ?, ?, ?)";

        if (userLanguage.getId() == null || userLanguage.getId().isEmpty()) {
            userLanguage.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, userLanguage.getId());
            pstmt.setString(2, userLanguage.getUserId());
            pstmt.setString(3, userLanguage.getLanguageId());
            pstmt.setString(4, userLanguage.getLevelId());
            pstmt.setLong(5, userLanguage.getXp());

            pstmt.executeUpdate();
        }
    }

    /**
     * Find a user language by ID
     */
    public UserLanguage findById(String id) throws SQLException {
        String sql = "SELECT * FROM user_languages WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserLanguage(rs);
                }
                return null;
            }
        }
    }

    /**
     * Find all user languages for a specific user
     */
    public List<UserLanguage> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM user_languages WHERE user_id = ?";
        List<UserLanguage> userLanguages = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userLanguages.add(mapResultSetToUserLanguage(rs));
                }
            }
        }

        return userLanguages;
    }

    /**
     * Find a user language by user ID and language ID
     */
    public UserLanguage findByUserIdAndLanguageId(String userId, String languageId) throws SQLException {
        String sql = "SELECT * FROM user_languages WHERE user_id = ? AND language_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, languageId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserLanguage(rs);
                }
                return null;
            }
        }
    }

    /**
     * Get all user languages from the database with related objects loaded
     */
    public List<UserLanguage> findAll() throws SQLException {
        String sql = "SELECT * FROM user_languages";
        List<UserLanguage> userLanguages = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userLanguages.add(mapResultSetToUserLanguage(rs));
            }
        }

        return userLanguages;
    }

    /**
     * Update an existing user language
     */
    public void update(UserLanguage userLanguage) throws SQLException {
        String sql = "UPDATE user_languages SET user_id = ?, language_id = ?, level_id = ?, xp = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userLanguage.getUserId());
            pstmt.setString(2, userLanguage.getLanguageId());
            pstmt.setString(3, userLanguage.getLevelId());
            pstmt.setLong(4, userLanguage.getXp());
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(6, userLanguage.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Update only the XP and level of a user language
     */
    public void updateXpAndLevel(UserLanguage userLanguage) throws SQLException {
        String sql = "UPDATE user_languages SET level_id = ?, xp = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userLanguage.getLevelId());
            pstmt.setLong(2, userLanguage.getXp());
            pstmt.setString(3, userLanguage.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a user language by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM user_languages WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete all user languages for a specific user
     */
    public void deleteByUserId(String userId) throws SQLException {
        String sql = "DELETE FROM user_languages WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to UserLanguage with related objects loaded
     */
    private UserLanguage mapResultSetToUserLanguage(ResultSet rs) throws SQLException {
        UserLanguage userLanguage = new UserLanguage();
        userLanguage.setId(rs.getString("id"));
        userLanguage.setUserId(rs.getString("user_id"));
        userLanguage.setLanguageId(rs.getString("language_id"));
        userLanguage.setLevelId(rs.getString("level_id"));
        userLanguage.setXp(rs.getLong("xp"));
        userLanguage.setLastUpdate(rs.getTimestamp("last_updated"));

        // Load related objects if needed
        try {
            Language language = languageDAO.findById(userLanguage.getLanguageId());
            userLanguage.setLanguage(language);

            Optional<LanguageLevel> level = languageLevelDAO.findById(userLanguage.getLevelId());
            userLanguage.setLevel(level.orElseGet(LanguageLevel::new));
        } catch (SQLException e) {
            // Log the error but continue
            System.err.println("Error loading related objects for UserLanguage: " + e.getMessage());
        }

        return userLanguage;
    }
}