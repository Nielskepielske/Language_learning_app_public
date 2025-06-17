package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.ScenarioKeyPoint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for the scenario_key_points table
 */
public class ScenarioKeyPointDAO {
    /**
     * Insert a new scenario key point using an existing connection
     * (Used within transactions)
     */
    public void insert(Connection conn, ScenarioKeyPoint keyPoint) throws SQLException {
        String sql = "INSERT INTO scenario_key_points (id, scenario_id, key_point) VALUES (?, ?, ?)";

        if(keyPoint.getId() == null || keyPoint.getId().isEmpty())
            // Generate a new UUID if the ID is not set
            keyPoint.setId(UUID.randomUUID().toString());

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, keyPoint.getId());
            pstmt.setString(2, keyPoint.getScenarioId());
            pstmt.setString(3, keyPoint.getKeyPoint());

            pstmt.executeUpdate();
        }
    }

    /**
     * Insert a new scenario key point
     */
    public void insert(ScenarioKeyPoint keyPoint) throws SQLException {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            insert(conn, keyPoint);
        }
    }

    /**
     * Find a scenario key point by ID
     */
    public ScenarioKeyPoint findById(String id) throws SQLException {
        String sql = "SELECT * FROM scenario_key_points WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToScenarioKeyPoint(rs);
                }
                return null;
            }
        }
    }

    /**
     * Find all key points for a specific scenario
     */
    public List<ScenarioKeyPoint> findByScenarioId(String scenarioId) throws SQLException {
        String sql = "SELECT * FROM scenario_key_points WHERE scenario_id = ?";
        List<ScenarioKeyPoint> keyPoints = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, scenarioId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    keyPoints.add(mapResultSetToScenarioKeyPoint(rs));
                }
            }
        }

        return keyPoints;
    }

    /**
     * Get all scenario key points from the database
     */
    public List<ScenarioKeyPoint> findAll() throws SQLException {
        String sql = "SELECT * FROM scenario_key_points";
        List<ScenarioKeyPoint> keyPoints = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                keyPoints.add(mapResultSetToScenarioKeyPoint(rs));
            }
        }

        return keyPoints;
    }

    /**
     * Update an existing scenario key point
     */
    public void update(ScenarioKeyPoint keyPoint) throws SQLException {
        String sql = "UPDATE scenario_key_points SET scenario_id = ?, key_point = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, keyPoint.getScenarioId());
            pstmt.setString(2, keyPoint.getKeyPoint());
            pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(4, keyPoint.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a scenario key point by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM scenario_key_points WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete all key points for a specific scenario using an existing connection
     * (Used within transactions)
     */
    public void deleteByScenarioId(Connection conn, String scenarioId) throws SQLException {
        String sql = "DELETE FROM scenario_key_points WHERE scenario_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, scenarioId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete all key points for a specific scenario
     */
    public void deleteByScenarioId(String scenarioId) throws SQLException {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            deleteByScenarioId(conn, scenarioId);
        }
    }

    /**
     * Helper method to map ResultSet to ScenarioKeyPoint
     */
    private ScenarioKeyPoint mapResultSetToScenarioKeyPoint(ResultSet rs) throws SQLException {
        ScenarioKeyPoint keyPoint = new ScenarioKeyPoint();
        keyPoint.setId(rs.getString("id"));
        keyPoint.setScenarioId(rs.getString("scenario_id"));
        keyPoint.setKeyPoint(rs.getString("key_point"));
        keyPoint.setLastUpdate(rs.getTimestamp("last_updated"));
        return keyPoint;
    }
}