package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.SpeakingTestQuestion;
import com.final_app.models.UserSpeakingTestResponse;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data Access Object for the user_speaking_test_responses table
 */
public class UserSpeakingTestResponseDAO {
    private final SpeakingTestQuestionDAO questionDAO = new SpeakingTestQuestionDAO();

    /**
     * Insert a new user speaking test response into the database
     */
    public void insert(UserSpeakingTestResponse response) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Insert the main response record
            String sql = "INSERT INTO user_speaking_test_responses (id, user_speaking_test_id, question_id, " +
                    "question_index, transcribed_text, responded_at, grammar_score, vocabulary_score, " +
                    "overall_score, feedback) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            if(response.getId() == null || response.getId().isEmpty()) {
                // Generate a new UUID if the ID is not provided
                response.setId(UUID.randomUUID().toString());
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, response.getId());
                pstmt.setString(2, response.getUserSpeakingTestId());
                pstmt.setString(3, response.getQuestionId());
                pstmt.setInt(4, response.getQuestionIndex());
                pstmt.setString(5, response.getTranscribedText());

                // Set timestamp
                if (response.getRespondedAt() != null) {
                    pstmt.setTimestamp(6, new Timestamp(response.getRespondedAt().getTime()));
                } else {
                    pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                }

                pstmt.setInt(7, response.getGrammarScore());
                pstmt.setInt(8, response.getVocabularyScore());
                pstmt.setInt(9, response.getOverallScore());
                pstmt.setString(10, response.getFeedback());

                pstmt.executeUpdate();

                loadRelatedObjects(response, conn);
            }

            // Insert grammar rule evaluations
            if (response.getGrammarRulesCorrect() != null && !response.getGrammarRulesCorrect().isEmpty()) {
                insertGrammarRules(conn, response.getId(), response.getGrammarRulesCorrect());
            }

            // Insert vocabulary usage data
            if (response.getRequiredVocabularyUsed() != null && !response.getRequiredVocabularyUsed().isEmpty()) {
                insertVocabularyUsage(conn, response.getId(), response.getRequiredVocabularyUsed());
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
     * Helper method to insert grammar rule evaluations
     */
    private void insertGrammarRules(Connection conn, String responseId, Map<String, Boolean> grammarRules) throws SQLException {
        String sql = "INSERT INTO response_grammar_evaluations (response_id, grammar_rule, is_correct) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Boolean> entry : grammarRules.entrySet()) {
                pstmt.setString(1, responseId);
                pstmt.setString(2, entry.getKey());
                pstmt.setBoolean(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * Helper method to insert vocabulary usage data
     */
    private void insertVocabularyUsage(Connection conn, String responseId, Map<String, Boolean> vocabularyUsage) throws SQLException {
        String sql = "INSERT INTO response_vocabulary_usage (response_id, vocabulary_word, was_used) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Boolean> entry : vocabularyUsage.entrySet()) {
                pstmt.setString(1, responseId);
                pstmt.setString(2, entry.getKey());
                pstmt.setBoolean(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * Find a user speaking test response by ID with all related objects loaded
     */
    public UserSpeakingTestResponse findById(String id) throws SQLException {
        String sql = "SELECT * FROM user_speaking_test_responses WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserSpeakingTestResponse response = mapResultSetToUserSpeakingTestResponse(rs);
                    loadRelatedObjects(response, conn);
                    return response;
                }
                return null;
            }
        }
    }

    /**
     * Find all responses for a specific user speaking test
     */
    public List<UserSpeakingTestResponse> findByUserSpeakingTestId(String userSpeakingTestId) throws SQLException {
        String sql = "SELECT * FROM user_speaking_test_responses WHERE user_speaking_test_id = ? ORDER BY question_index";
        List<UserSpeakingTestResponse> responses = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userSpeakingTestId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserSpeakingTestResponse response = mapResultSetToUserSpeakingTestResponse(rs);
                    loadRelatedObjects(response, conn);
                    responses.add(response);
                }
            }
        }

        return responses;
    }

    /**
     * Find all responses for a specific question
     */
    public List<UserSpeakingTestResponse> findByQuestionId(String questionId) throws SQLException {
        String sql = "SELECT * FROM user_speaking_test_responses WHERE question_id = ?";
        List<UserSpeakingTestResponse> responses = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, questionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserSpeakingTestResponse response = mapResultSetToUserSpeakingTestResponse(rs);
                    loadRelatedObjects(response, conn);
                    responses.add(response);
                }
            }
        }

        return responses;
    }

    /**
     * Get all user speaking test responses from the database with related objects loaded
     */
    public List<UserSpeakingTestResponse> findAll() throws SQLException {
        String sql = "SELECT * FROM user_speaking_test_responses";
        List<UserSpeakingTestResponse> responses = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserSpeakingTestResponse response = mapResultSetToUserSpeakingTestResponse(rs);
                loadRelatedObjects(response, conn);
                responses.add(response);
            }
        }

        return responses;
    }

    /**
     * Update an existing user speaking test response
     */
    public void update(UserSpeakingTestResponse response) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Update the main response record
            String sql = "UPDATE user_speaking_test_responses SET user_speaking_test_id = ?, " +
                    "question_id = ?, question_index = ?, transcribed_text = ?, responded_at = ?, " +
                    "grammar_score = ?, vocabulary_score = ?, overall_score = ?, feedback = ?, last_updated = ? " +
                    "WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, response.getUserSpeakingTestId());
                pstmt.setString(2, response.getQuestionId());
                pstmt.setInt(3, response.getQuestionIndex());
                pstmt.setString(4, response.getTranscribedText());
                pstmt.setTimestamp(5, new Timestamp(response.getRespondedAt().getTime()));
                pstmt.setInt(6, response.getGrammarScore());
                pstmt.setInt(7, response.getVocabularyScore());
                pstmt.setInt(8, response.getOverallScore());
                pstmt.setString(9, response.getFeedback());
                pstmt.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
                pstmt.setString(11, response.getId());

                pstmt.executeUpdate();
            }

            // Delete existing grammar rule evaluations and vocabulary usage data
            deleteGrammarRules(conn, response.getId());
            deleteVocabularyUsage(conn, response.getId());

            // Insert updated grammar rule evaluations
            if (response.getGrammarRulesCorrect() != null && !response.getGrammarRulesCorrect().isEmpty()) {
                insertGrammarRules(conn, response.getId(), response.getGrammarRulesCorrect());
            }

            // Insert updated vocabulary usage data
            if (response.getRequiredVocabularyUsed() != null && !response.getRequiredVocabularyUsed().isEmpty()) {
                insertVocabularyUsage(conn, response.getId(), response.getRequiredVocabularyUsed());
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
     * Helper method to delete grammar rule evaluations
     */
    private void deleteGrammarRules(Connection conn, String responseId) throws SQLException {
        String sql = "DELETE FROM response_grammar_evaluations WHERE response_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, responseId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to delete vocabulary usage data
     */
    private void deleteVocabularyUsage(Connection conn, String responseId) throws SQLException {
        String sql = "DELETE FROM response_vocabulary_usage WHERE response_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, responseId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a user speaking test response by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM user_speaking_test_responses WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to UserSpeakingTestResponse
     */
    private UserSpeakingTestResponse mapResultSetToUserSpeakingTestResponse(ResultSet rs) throws SQLException {
        UserSpeakingTestResponse response = new UserSpeakingTestResponse();
        response.setId(rs.getString("id"));
        response.setUserSpeakingTestId(rs.getString("user_speaking_test_id"));
        response.setQuestionId(rs.getString("question_id"));
        response.setQuestionIndex(rs.getInt("question_index"));
        response.setTranscribedText(rs.getString("transcribed_text"));

        // Convert SQL timestamp to LocalDateTime
        Timestamp respondedAt = rs.getTimestamp("responded_at");
        if (respondedAt != null) {
            response.setRespondedAt(respondedAt);
        }

        response.setGrammarScore(rs.getInt("grammar_score"));
        response.setVocabularyScore(rs.getInt("vocabulary_score"));
        response.setOverallScore(rs.getInt("overall_score"));
        response.setFeedback(rs.getString("feedback"));
        response.setLastUpdate(rs.getTimestamp("last_updated"));

        return response;
    }

    /**
     * Load related objects for a user speaking test response
     */
    private void loadRelatedObjects(UserSpeakingTestResponse response, Connection conn) throws SQLException {
        // Load the question
        SpeakingTestQuestion question = questionDAO.findById(response.getQuestionId());
        response.setQuestion(question);

        // Load grammar rule evaluations
        response.setGrammarRulesCorrect(loadGrammarRules(conn, response.getId()));

        // Load vocabulary usage data
        response.setRequiredVocabularyUsed(loadVocabularyUsage(conn, response.getId()));
    }

    /**
     * Helper method to load grammar rule evaluations
     */
    private Map<String, Boolean> loadGrammarRules(Connection conn, String responseId) throws SQLException {
        Map<String, Boolean> grammarRules = new HashMap<>();
        String sql = "SELECT grammar_rule, is_correct FROM response_grammar_evaluations WHERE response_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, responseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    grammarRules.put(rs.getString("grammar_rule"), rs.getBoolean("is_correct"));
                }
            }
        }

        return grammarRules;
    }

    /**
     * Helper method to load vocabulary usage data
     */
    private Map<String, Boolean> loadVocabularyUsage(Connection conn, String responseId) throws SQLException {
        Map<String, Boolean> vocabularyUsage = new HashMap<>();
        String sql = "SELECT vocabulary_word, was_used FROM response_vocabulary_usage WHERE response_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, responseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vocabularyUsage.put(rs.getString("vocabulary_word"), rs.getBoolean("was_used"));
                }
            }
        }

        return vocabularyUsage;
    }
}