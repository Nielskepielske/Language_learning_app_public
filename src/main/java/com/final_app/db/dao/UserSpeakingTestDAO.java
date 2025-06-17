package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.SpeakingTest;
import com.final_app.models.UserSpeakingTest;
import com.final_app.models.UserSpeakingTestResponse;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for the user_speaking_tests table
 */
public class UserSpeakingTestDAO {
    private final SpeakingTestDAO speakingTestDAO = new SpeakingTestDAO();
    private final UserSpeakingTestResponseDAO responseDAO = new UserSpeakingTestResponseDAO();

    /**
     * Insert a new user speaking test into the database
     */
    public void insert(UserSpeakingTest userSpeakingTest) throws SQLException {
        String sql = "INSERT INTO user_speaking_tests (id, user_id, test_id, started_at, " +
                "status, score) VALUES (?, ?, ?, ?, ?, ?)";

        if(userSpeakingTest.getId() == null || userSpeakingTest.getId().isEmpty()) {
            // Generate a new UUID if the ID is not set
            userSpeakingTest.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, userSpeakingTest.getId());
            pstmt.setString(2, userSpeakingTest.getUserId());
            pstmt.setString(3, userSpeakingTest.getTestId());

            // Convert LocalDateTime to SQL timestamp
            if (userSpeakingTest.getStartedAt() != null) {
                pstmt.setTimestamp(4, new Timestamp(userSpeakingTest.getStartedAt().getTime()));
            } else {
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            }

            pstmt.setString(5, userSpeakingTest.getStatus());
            pstmt.setInt(6, userSpeakingTest.getScore());

            pstmt.executeUpdate();

            // Insert any associated responses
            if (userSpeakingTest.getResponses() != null && !userSpeakingTest.getResponses().isEmpty()) {
                for (UserSpeakingTestResponse response : userSpeakingTest.getResponses()) {
                    response.setUserSpeakingTestId(userSpeakingTest.getId());
                    responseDAO.insert(response);
                }
            }
        }
    }

    /**
     * Find a user speaking test by ID with all related objects loaded
     */
    public UserSpeakingTest findById(String id) throws SQLException {
        String sql = "SELECT * FROM user_speaking_tests WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserSpeakingTest userSpeakingTest = mapResultSetToUserSpeakingTest(rs);
                    loadRelatedObjects(userSpeakingTest);
                    return userSpeakingTest;
                }
                return null;
            }
        }
    }

    /**
     * Find all user speaking tests for a specific user
     */
    public List<UserSpeakingTest> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM user_speaking_tests WHERE user_id = ?";
        List<UserSpeakingTest> tests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserSpeakingTest test = mapResultSetToUserSpeakingTest(rs);
                    loadRelatedObjects(test);
                    tests.add(test);
                }
            }
        }

        return tests;
    }

    /**
     * Find all user speaking tests for a specific test
     */
    public List<UserSpeakingTest> findByTestId(String testId) throws SQLException {
        String sql = "SELECT * FROM user_speaking_tests WHERE test_id = ?";
        List<UserSpeakingTest> tests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, testId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserSpeakingTest test = mapResultSetToUserSpeakingTest(rs);
                    loadRelatedObjects(test);
                    tests.add(test);
                }
            }
        }

        return tests;
    }

    /**
     * Find user speaking tests by status
     */
    public List<UserSpeakingTest> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM user_speaking_tests WHERE status = ?";
        List<UserSpeakingTest> tests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserSpeakingTest test = mapResultSetToUserSpeakingTest(rs);
                    loadRelatedObjects(test);
                    tests.add(test);
                }
            }
        }

        return tests;
    }

    /**
     * Find user speaking tests by score range
     */
    public List<UserSpeakingTest> findByScoreRange(int minScore, int maxScore) throws SQLException {
        String sql = "SELECT * FROM user_speaking_tests WHERE score >= ? AND score <= ?";
        List<UserSpeakingTest> tests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, minScore);
            pstmt.setInt(2, maxScore);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserSpeakingTest test = mapResultSetToUserSpeakingTest(rs);
                    loadRelatedObjects(test);
                    tests.add(test);
                }
            }
        }

        return tests;
    }

    /**
     * Find a user speaking test by user ID and test ID
     */
    public UserSpeakingTest findByUserIdAndTestId(String userId, String testId) throws SQLException {
        String sql = "SELECT * FROM user_speaking_tests WHERE user_id = ? AND test_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, testId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserSpeakingTest test = mapResultSetToUserSpeakingTest(rs);
                    loadRelatedObjects(test);
                    return test;
                }
                return null;
            }
        }
    }

    /**
     * Get all user speaking tests from the database with related objects loaded
     */
    public List<UserSpeakingTest> findAll() throws SQLException {
        String sql = "SELECT * FROM user_speaking_tests";
        List<UserSpeakingTest> tests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserSpeakingTest test = mapResultSetToUserSpeakingTest(rs);
                loadRelatedObjects(test);
                tests.add(test);
            }
        }

        return tests;
    }

    /**
     * Update an existing user speaking test
     */
    public void update(UserSpeakingTest userSpeakingTest) throws SQLException {
        final int MAX_RETRIES = 3; // Aantal retries voor SQLite-bewerkingen
        final long RETRY_DELAY_MS = 200; // Wachttijd tussen retries (in milliseconden)
        int attempt = 0;

        while (true) {
            try (Connection conn = DatabaseManager.getInstance().getConnection()) {
                conn.setAutoCommit(false); // Start de transactie

                // SQL update-query voorbereiden
                String sql = "UPDATE user_speaking_tests SET user_id = ?, test_id = ?, started_at = ?, " +
                        "completed_at = ?, status = ?, score = ?, last_updated = ? WHERE id = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, userSpeakingTest.getUserId());
                    pstmt.setString(2, userSpeakingTest.getTestId());

                    // Zet begonnen tijd (moet altijd aanwezig zijn)
                    pstmt.setTimestamp(3, new Timestamp(userSpeakingTest.getStartedAt().getTime()));

                    // Zet voltooiingstijd (indien aanwezig)
                    if (userSpeakingTest.getCompletedAt() != null) {
                        pstmt.setTimestamp(4, new Timestamp(userSpeakingTest.getCompletedAt().getTime()));
                    } else {
                        pstmt.setNull(4, Types.TIMESTAMP);
                    }

                    pstmt.setString(5, userSpeakingTest.getStatus());
                    pstmt.setInt(6, userSpeakingTest.getScore());
                    pstmt.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // Laatste bijgewerkt tijd
                    pstmt.setString(8, userSpeakingTest.getId());

                    pstmt.executeUpdate(); // Voer de update-query uit
                }

                // Update gekoppelde reacties
//                if (userSpeakingTest.getResponses() != null) {
//                    for (UserSpeakingTestResponse response : userSpeakingTest.getResponses()) {
//                        if (response.getId() > 0) {
//                            responseDAO.update(response);
//                        } else {
//                            response.setUserSpeakingTestId(userSpeakingTest.getId());
//                            responseDAO.insert(response);
//                        }
//                    }
//                }

                conn.commit(); // Commit de transactie
                return; // Als alles goed gaat, breek de retry-loop
            } catch (SQLException e) {
                // Controleer of de fout veroorzaakt wordt door een vergrendeling
                if (e.getMessage().contains("database is locked") && attempt < MAX_RETRIES) {
                    attempt++;
                    System.err.println("Attempt " + attempt + " failed due to database lock. Retrying...");
                    try {
                        Thread.sleep(RETRY_DELAY_MS); // Wacht een korte tijd voordat je het opnieuw probeert
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Retry interrupted", ie);
                    }
                } else {
                    // Hergooi de fout als het geen vergrendelingsprobleem of teveel retries zijn
                    throw e;
                }
            }
        }
    }

    /**
     * Update only the status and completion time of a user speaking test
     */
    public void updateStatus(String id, String status) throws SQLException {
        String sql;
        if ("COMPLETED".equals(status)) {
            sql = "UPDATE user_speaking_tests SET status = ?, completed_at = ? WHERE id = ?";
        } else {
            sql = "UPDATE user_speaking_tests SET status = ? WHERE id = ?";
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);

            if ("COMPLETED".equals(status)) {
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(3, id);
            } else {
                pstmt.setString(2, id);
            }

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a user speaking test by ID
     * This will also delete all associated responses due to the ON DELETE CASCADE constraint
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM user_speaking_tests WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to UserSpeakingTest
     */
    private UserSpeakingTest mapResultSetToUserSpeakingTest(ResultSet rs) throws SQLException {
        UserSpeakingTest userSpeakingTest = new UserSpeakingTest();
        userSpeakingTest.setId(rs.getString("id"));
        userSpeakingTest.setUserId(rs.getString("user_id"));
        userSpeakingTest.setTestId(rs.getString("test_id"));

        // Convert SQL timestamp to LocalDateTime
        Timestamp startedAt = rs.getTimestamp("started_at");
        if (startedAt != null) {
            userSpeakingTest.setStartedAt(startedAt);
        }

        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            userSpeakingTest.setCompletedAt(completedAt);
        }

        userSpeakingTest.setStatus(rs.getString("status"));
        userSpeakingTest.setScore(rs.getInt("score"));
        userSpeakingTest.setLastUpdate(rs.getTimestamp("last_updated"));

        return userSpeakingTest;
    }

    /**
     * Load related objects for a user speaking test
     */
    private void loadRelatedObjects(UserSpeakingTest userSpeakingTest) throws SQLException {
        // Load the test details
        SpeakingTest test = speakingTestDAO.findById(userSpeakingTest.getTestId());
        userSpeakingTest.setTest(test);

        // Load the responses
        List<UserSpeakingTestResponse> responses = responseDAO.findByUserSpeakingTestId(userSpeakingTest.getId());
        userSpeakingTest.setResponses(responses);
    }
}
