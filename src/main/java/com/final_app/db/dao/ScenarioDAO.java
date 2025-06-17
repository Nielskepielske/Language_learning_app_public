package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.Scenario;
import com.final_app.models.ScenarioKeyPoint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for the scenarios table
 */
public class ScenarioDAO {
    private final ScenarioKeyPointDAO keyPointDAO = new ScenarioKeyPointDAO();

    /**
     * Insert a new scenario into the database
     */
    public void insert(Scenario scenario) throws SQLException {
        Connection conn = null;

        try {
            //System.out.println("Start function");
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            //System.out.println("Database conn: " + conn);

            // Insert scenario
            String sql = "INSERT INTO scenarios (id, description, role) VALUES (?, ?, ?)";

            if(scenario.getId() == null || scenario.getId().isEmpty()) {
                // Generate a new UUID if the ID is not provided
                scenario.setId(UUID.randomUUID().toString());
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, scenario.getId());
                pstmt.setString(2, scenario.getDescription());
                pstmt.setString(3, scenario.getRole());

                pstmt.executeUpdate();

            }

            // Insert key points
            if (scenario.getKeyPoints() != null && !scenario.getKeyPoints().isEmpty()) {
                for (String keyPoint : scenario.getKeyPoints()) {
                    ScenarioKeyPoint keyPointObj = new ScenarioKeyPoint(scenario.getId(), keyPoint);
                    keyPointDAO.insert(conn, keyPointObj);
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Find a scenario by ID with all its key points
     */
    public Scenario findById(String id) throws SQLException {
        String sql = "SELECT * FROM scenarios WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Scenario scenario = mapResultSetToScenario(rs);

                    // Load key points
                    List<ScenarioKeyPoint> keyPoints = keyPointDAO.findByScenarioId(scenario.getId());
                    for (ScenarioKeyPoint keyPoint : keyPoints) {
                        scenario.addKeyPoint(keyPoint.getKeyPoint());
                    }

                    return scenario;
                }
                return null;
            }
        }
    }

    /**
     * Get all scenarios from the database
     */
    public List<Scenario> findAll() throws SQLException {
        String sql = "SELECT * FROM scenarios";
        List<Scenario> scenarios = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Scenario scenario = mapResultSetToScenario(rs);

                // Load key points
                List<ScenarioKeyPoint> keyPoints = keyPointDAO.findByScenarioId(scenario.getId());
                for (ScenarioKeyPoint keyPoint : keyPoints) {
                    scenario.addKeyPoint(keyPoint.getKeyPoint());
                }

                scenarios.add(scenario);
            }
        }

        return scenarios;
    }

    /**
     * Update an existing scenario and its key points
     */
    public void update(Scenario scenario) throws SQLException {
        Connection conn = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Update scenario
            String sql = "UPDATE scenarios SET description = ?, role = ?, last_updated = ? WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, scenario.getDescription());
                pstmt.setString(2, scenario.getRole());
                pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                pstmt.setString(4, scenario.getId());

                pstmt.executeUpdate();
            }

            // Delete existing key points
            keyPointDAO.deleteByScenarioId(conn, scenario.getId());

            // Insert new key points
            if (scenario.getKeyPoints() != null && !scenario.getKeyPoints().isEmpty()) {
                for (String keyPoint : scenario.getKeyPoints()) {
                    ScenarioKeyPoint keyPointObj = new ScenarioKeyPoint(scenario.getId(), keyPoint);
                    keyPointDAO.insert(conn, keyPointObj);
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete a scenario by ID
     * This will also delete all associated key points due to the ON DELETE CASCADE constraint
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM scenarios WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to Scenario
     */
    private Scenario mapResultSetToScenario(ResultSet rs) throws SQLException {
        Scenario scenario = new Scenario();
        scenario.setId(rs.getString("id"));
        scenario.setDescription(rs.getString("description"));
        scenario.setRole(rs.getString("role"));
        scenario.setLastUpdate(rs.getTimestamp("last_updated"));
        return scenario;
    }
}
