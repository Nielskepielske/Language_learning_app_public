package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.LanguageLevel;
import com.final_app.models.LanguageLevelSystem;
import com.final_app.services.AppService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class LanguageLevelSystemDAO {
    /**
     * Insert a new language system into the database
     */
    public void insert(LanguageLevelSystem languageLevelSystem) throws SQLException {
        String sql = "INSERT INTO language_systems (id, name, description) VALUES (?, ?, ?)";

        if(languageLevelSystem.getId() == null || languageLevelSystem.getId().isEmpty()) {
            languageLevelSystem.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, languageLevelSystem.getId());
            pstmt.setString(2, languageLevelSystem.getName());
            pstmt.setString(3, languageLevelSystem.getDescription());

            pstmt.executeUpdate();
        }
    }

// Inside your LanguageLevelSystemDAO or LocalLanguageRepository

    /**
     * Saves (creates or updates) a LanguageLevelSystem and returns the persisted instance.
     * This method ensures data consistency by first checking if the record exists.
     *
     * @param systemToSave The LanguageLevelSystem object to save.
     * @return The final, persisted LanguageLevelSystem object with the correct ID from the database.
     */
    public LanguageLevelSystem save(LanguageLevelSystem systemToSave) throws SQLException {
        // Use the unique 'name' to check if the entity already exists in the database.
        Optional<LanguageLevelSystem> existingSystemOpt = Optional.ofNullable(findByName(systemToSave.getName()));

        if (existingSystemOpt.isPresent()) {
            // IT EXISTS: Prepare for an update.
            LanguageLevelSystem systemInDb = existingSystemOpt.get();
            systemToSave.setId(systemInDb.getId());

            // Update the fields of the object retrieved from the database
            // with the new values from the object that was passed in.
            systemInDb.setDescription(systemToSave.getDescription());

            // Persist the changes using the private update method.
            update(systemInDb);

            // CRITICAL: Return the object that reflects the state of the database,
            // which now has the updated description and the original, correct ID.
            return systemInDb;

        } else {
            // IT'S NEW: Prepare for a create operation.
            // Ensure the object has a primary key before inserting.
            if (systemToSave.getId() == null || systemToSave.getId().isEmpty()) {
                systemToSave.setId(UUID.randomUUID().toString());
            }

            // Persist the new object using the private create method.
            insert(systemToSave);

            // Return the newly created object, which now has its ID set.
            return systemToSave;
        }
    }

    /**
     * Find a language system by ID
     */
    public LanguageLevelSystem findById(String id) throws SQLException {
        String sql = "SELECT * FROM language_systems WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLanguageLevelSystem(rs);
                }
                return null;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Find a language system by name
     */
    public LanguageLevelSystem findByName(String name) throws SQLException {
        String sql = "SELECT * FROM language_systems WHERE name = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLanguageLevelSystem(rs);
                }
                return null;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Get all language systems from the database
     */
    public List<LanguageLevelSystem> findAll() throws SQLException {
        String sql = "SELECT * FROM language_systems ORDER BY name";
        List<LanguageLevelSystem> languageLevelSystems = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                languageLevelSystems.add(mapResultSetToLanguageLevelSystem(rs));
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return languageLevelSystems;
    }

    /**
     * Update an existing language system
     */
    public void update(LanguageLevelSystem languageLevelSystem) throws SQLException {
        String sql = "UPDATE language_systems SET name = ?, description = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, languageLevelSystem.getName());
            pstmt.setString(2, languageLevelSystem.getDescription());
            pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(4, languageLevelSystem.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a language system by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM language_systems WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to languageLevelSystem
     */
    private LanguageLevelSystem mapResultSetToLanguageLevelSystem(ResultSet rs) throws SQLException, ExecutionException, InterruptedException {
        LanguageLevelSystem languageLevelSystem = new LanguageLevelSystem();
        languageLevelSystem.setId(rs.getString("id"));
        languageLevelSystem.setName(rs.getString("name"));
        languageLevelSystem.setDescription(rs.getString("description"));
        languageLevelSystem.setLastUpdate(rs.getTimestamp("last_updated"));

        languageLevelSystem.setLevels(AppService.getInstance().getLanguageService().getAllLanguageLevels().stream().filter(lnglvl -> lnglvl.getSystemId().equals(languageLevelSystem.getId())).toList());
        return languageLevelSystem;
    }
}
