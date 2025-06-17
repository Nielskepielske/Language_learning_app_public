package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.factories.RepositoryFactory;
import com.final_app.models.Language;
import com.final_app.models.Message;
import com.final_app.models.Settings;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for the messages table
 */
public class SettingsDAO {
    /**
     * Insert a new message into the database
     */
    public void insert(Settings settings) throws SQLException {
        String sql = "INSERT INTO settings (id, language_id, selected_languages, user_id) VALUES (?, ?, ?, ?)";

        if(settings.getId() == null || settings.getId().isEmpty()){
            settings.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, settings.getId());
            pstmt.setString(2, settings.getLanguageId());
            pstmt.setString(3, String.join(",", settings.getSelectedLanguages()));
            pstmt.setString(4, settings.getUserId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Find a message by ID
     */
    public Settings findById(String id) throws SQLException {
        String sql = "SELECT * FROM settings WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSettings(rs);
                }
                return null;
            }
        }
    }

    /**
     * Find Settings by user id
     */
    public Settings findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM settings WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSettings(rs);
                }
            }
        }
        return null;
    }

    /**
     * Update an existing message
     */
    public void update(Settings settings) throws SQLException {
        String sql = "UPDATE settings SET language_id = ?, selected_languages = ?, user_id = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getLanguageId());
            pstmt.setString(2, String.join(",", settings.getSelectedLanguages()));
            pstmt.setString(3, settings.getUserId());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(5, settings.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a message by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM settings WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete all messages for a specific user conversation
     */
    public void deleteByUserId(String userId) throws SQLException {
        String sql = "DELETE FROM settings WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to Message
     */
    private Settings mapResultSetToSettings(ResultSet rs) throws SQLException {
        Settings settings = new Settings();
        settings.setId(rs.getString("id"));
        settings.setLanguageId(rs.getString("language_id"));
        settings.setSelectedLanguages(rs.getString("selected_languages"));
        settings.setUserId(rs.getString("user_id"));
        settings.setLastUpdate(rs.getTimestamp("last_updated"));

        Language language = RepositoryFactory.getLanguageRepository().getLanguageById(settings.getLanguageId()).join().get();
        settings.setLanguage(language);

        return settings;
    }
}
