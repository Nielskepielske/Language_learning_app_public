package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.LanguageLevel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for the language_levels table
 */
public class LanguageLevelDAO {
    /**
     * Insert a new language level into the database
     */
    public void insert(LanguageLevel languageLevel) throws SQLException {
        String sql = "INSERT INTO language_levels (id, system_id, name, \"value\") VALUES (?, ?, ?, ?)";

        if(languageLevel.getId() == null || languageLevel.getId().isEmpty()) {
            languageLevel.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, languageLevel.getId());
            pstmt.setString(2, languageLevel.getSystemId());
            pstmt.setString(3, languageLevel.getName());
            pstmt.setInt(4, languageLevel.getValue());

            pstmt.executeUpdate();
        }
    }

    /**
     * Saves (creates or updates) a LanguageLevel and returns the persisted instance.
     *
     * @param levelToSave The LanguageLevel object to save.
     * @return The final, persisted LanguageLevel object with the correct ID.
     */
    public LanguageLevel save(LanguageLevel levelToSave) throws SQLException {
        // Use the unique name to check if the entity already exists.
        Optional<LanguageLevel> existingLevelOpt = findByName(levelToSave.getName());

        if (existingLevelOpt.isPresent()) {
            // IT EXISTS: Prepare for an update.
            LanguageLevel levelInDb = existingLevelOpt.get();
            // Update fields from the incoming object onto the object we know is in the DB.
            levelInDb.setSystemId(levelToSave.getSystemId());
            levelInDb.setValue(levelToSave.getValue());
            levelToSave.setId(levelToSave.getId());

            update(levelInDb);
            return levelInDb; // Return the updated object from the database.
        } else {
            // IT'S NEW: Prepare for a create.
            // Ensure it has a primary key before inserting.
            if (levelToSave.getId() == null || levelToSave.getId().isEmpty()) {
                levelToSave.setId(UUID.randomUUID().toString());
            }
            insert(levelToSave);
            return levelToSave; // Return the newly created object.
        }
    }

    /**
     * Find a language level by ID
     */
    public Optional<LanguageLevel> findById(String id) throws SQLException {
        String sql = "SELECT * FROM language_levels WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLanguageLevel(rs));
                }
                return Optional.empty();
            }
        }
    }


    /**
     * Finds a LanguageLevel by its unique name.
     */
    public Optional<LanguageLevel> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM language_levels WHERE name = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new LanguageLevel(
                            rs.getString("id"),
                            rs.getString("system_id"),
                            rs.getString("name"),
                            rs.getInt("value")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Find a language level by value
     */
    public LanguageLevel findByValue(int value) throws SQLException {
        String sql = "SELECT * FROM language_levels WHERE \"value\" = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, value);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLanguageLevel(rs);
                }
                return null;
            }
        }
    }

    /**
     * Get all language levels from the database
     */
    public List<LanguageLevel> findAll() throws SQLException {
        String sql = "SELECT * FROM language_levels ORDER BY \"value\"";
        List<LanguageLevel> languageLevels = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                languageLevels.add(mapResultSetToLanguageLevel(rs));
            }
        }

        return languageLevels;
    }

    /**
     * Updates an existing LanguageLevel record.
     */
    private void update(LanguageLevel level) throws SQLException {
        String sql = "UPDATE language_levels SET system_id = ?, name = ?, \"value\" = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, level.getSystemId());
            pstmt.setString(2, level.getName());
            pstmt.setInt(3, level.getValue());
            pstmt.setString(4, level.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a language level by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM language_levels WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to LanguageLevel
     */
    private LanguageLevel mapResultSetToLanguageLevel(ResultSet rs) throws SQLException {
        LanguageLevel languageLevel = new LanguageLevel();
        languageLevel.setId(rs.getString("id"));
        languageLevel.setSystemId(rs.getString("system_id"));
        languageLevel.setName(rs.getString("name"));
        languageLevel.setValue(rs.getInt("value"));
        languageLevel.setLastUpdate(rs.getTimestamp("last_updated"));
        return languageLevel;
    }
}
