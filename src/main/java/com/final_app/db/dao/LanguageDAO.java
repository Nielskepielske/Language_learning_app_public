package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevelSystem;
import com.final_app.services.AppService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for the languages table
 */
public class LanguageDAO {
    private LanguageLevelSystemDAO languageLevelSystemDAO = new LanguageLevelSystemDAO();
    /**
     * Insert a new language into the database
     */
    public void insert(Language language) throws SQLException {
        String sql = "INSERT INTO languages (id, system_id, name, iso, color, max_xp) VALUES (?, ?, ?, ?, ?, ?)";

        if(language.getId() == null || language.getId().isEmpty()) {
            language.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, language.getId());
            pstmt.setString(2, language.getSystemId());
            pstmt.setString(3, language.getName());
            pstmt.setString(4, language.getIso());
            pstmt.setString(5, language.getColor());
            pstmt.setLong(6, language.getMaxXp());

            pstmt.executeUpdate();
        }
    }
    /**
     * Saves (creates or updates) a Language and returns the persisted instance.
     *
     * @param languageToSave The Language object to save.
     * @return The final, persisted Language object with the correct ID.
     */
    public Language save(Language languageToSave) throws SQLException {
        // Use the unique ISO code to check if the entity already exists.
        Optional<Language> existingLanguageOpt = findByIso(languageToSave.getIso());

        if (existingLanguageOpt.isPresent()) {
            // IT EXISTS: Prepare for an update.
            Language languageInDb = existingLanguageOpt.get();

            // Update fields from the incoming object onto the object we know is in the DB.
            languageToSave.setId(languageInDb.getId());
            languageInDb.setSystemId(languageToSave.getSystemId());
            languageInDb.setName(languageToSave.getName());
            languageInDb.setColor(languageToSave.getColor());
            languageInDb.setMaxXp(languageToSave.getMaxXp());

            update(languageInDb);
            return languageInDb; // Return the updated object from the database.
        } else {
            // IT'S NEW: Prepare for a create.
            if (languageToSave.getId() == null || languageToSave.getId().isEmpty()) {
                languageToSave.setId(UUID.randomUUID().toString());
            }
            insert(languageToSave);
            return languageToSave; // Return the newly created object.
        }
    }

    /**
     * Find a language by ID
     */
    public Language findById(String id) throws SQLException {
        String sql = "SELECT * FROM languages WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLanguage(rs);
                }
                return null;
            }
        }
    }
    /**
     * Finds a Language by its unique ISO code.
     */
    public Optional<Language> findByIso(String iso) throws SQLException {
        String sql = "SELECT * FROM languages WHERE iso = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, iso);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLanguage(rs)); // Use a helper to map ResultSet to Object
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Find a language by name
     */
    public Language findByName(String name) throws SQLException {
        String sql = "SELECT * FROM languages WHERE name = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLanguage(rs);
                }
                return null;
            }
        }
    }

    /**
     * Get all languages from the database
     */
    public List<Language> findAll() throws SQLException {
        String sql = "SELECT * FROM languages";
        List<Language> languages = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                languages.add(mapResultSetToLanguage(rs));
            }
        }

        return languages;
    }

    /**
     * Update an existing language
     */
    public void update(Language language) throws SQLException {
        String sql = "UPDATE languages SET system_id = ?, name = ?, color = ?, max_xp = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, language.getSystemId());
            pstmt.setString(2, language.getName());
            pstmt.setString(3, language.getColor());
            pstmt.setLong(4, language.getMaxXp());
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(6, language.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a language by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM languages WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to Language
     */
    private Language mapResultSetToLanguage(ResultSet rs) throws SQLException {
        Language language = new Language();
        language.setId(rs.getString("id"));
        language.setSystemId(rs.getString("system_id"));
        language.setName(rs.getString("name"));
        language.setIso(rs.getString("iso"));
        language.setColor(rs.getString("color"));
        language.setMaxXp(rs.getLong("max_xp"));
        language.setLastUpdate(rs.getTimestamp("last_updated"));

        language.setLanguageLevelSystem(languageLevelSystemDAO.findById(language.getSystemId()));
        return language;
    }

    public LanguageLevelSystemDAO getLanguageLevelSystemDAO() {
        return languageLevelSystemDAO;
    }

    public void setLanguageLevelSystemDAO(LanguageLevelSystemDAO languageLevelSystemDAO) {
        this.languageLevelSystemDAO = languageLevelSystemDAO;
    }
}