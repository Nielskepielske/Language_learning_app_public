package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.SpeakingTestQuestion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Data Access Object for the speaking_test_questions table
 */
public class SpeakingTestQuestionDAO {
    /**
     * Insert a new speaking test question into the database
     */
    public void insert(SpeakingTestQuestion question) throws SQLException {
        String sql = "INSERT INTO speaking_test_questions (id, test_id, question_text, expected_response_pattern, " +
                "expected_response_language_iso, required_vocabulary, difficulty_level, order_index) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        if(question.getId() == null || question.getId().isEmpty()) {
            // Generate a new UUID if the ID is not provided
            question.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, question.getId());
            pstmt.setString(2, question.getTestId());
            pstmt.setString(3, question.getQuestionText());
            pstmt.setString(4, question.getExpectedResponsePattern());
            pstmt.setString(5, question.getExpectedResponseLanguageIso());

            // Convert list of required vocabulary to comma-separated string
            String vocabularyStr = null;
            if (question.getRequiredVocabulary() != null && !question.getRequiredVocabulary().isEmpty()) {
                vocabularyStr = String.join(",", question.getRequiredVocabulary());
            }
            pstmt.setString(6, vocabularyStr);

            pstmt.setInt(7, question.getDifficultyLevel());
            pstmt.setInt(8, question.getOrderIndex());

            pstmt.executeUpdate();
        }
    }

    /**
     * Find a speaking test question by ID
     */
    public SpeakingTestQuestion findById(String id) throws SQLException {
        String sql = "SELECT * FROM speaking_test_questions WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSpeakingTestQuestion(rs);
                }
                return null;
            }
        }
    }

    /**
     * Find all questions for a specific test
     */
    public List<SpeakingTestQuestion> findByTestId(String testId) throws SQLException {
        String sql = "SELECT * FROM speaking_test_questions WHERE test_id = ? ORDER BY order_index";
        List<SpeakingTestQuestion> questions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, testId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapResultSetToSpeakingTestQuestion(rs));
                }
            }
        }

        return questions;
    }

    /**
     * Find questions by difficulty level
     */
    public List<SpeakingTestQuestion> findByDifficultyLevel(int difficultyLevel) throws SQLException {
        String sql = "SELECT * FROM speaking_test_questions WHERE difficulty_level = ?";
        List<SpeakingTestQuestion> questions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, difficultyLevel);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapResultSetToSpeakingTestQuestion(rs));
                }
            }
        }

        return questions;
    }

    /**
     * Get all speaking test questions from the database
     */
    public List<SpeakingTestQuestion> findAll() throws SQLException {
        String sql = "SELECT * FROM speaking_test_questions";
        List<SpeakingTestQuestion> questions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                questions.add(mapResultSetToSpeakingTestQuestion(rs));
            }
        }

        return questions;
    }

    /**
     * Update an existing speaking test question
     */
    public void update(SpeakingTestQuestion question) throws SQLException {
        String sql = "UPDATE speaking_test_questions SET test_id = ?, question_text = ?, " +
                "expected_response_pattern = ?, expected_response_language_iso = ?, required_vocabulary = ?, difficulty_level = ?, " +
                "order_index = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, question.getTestId());
            pstmt.setString(2, question.getQuestionText());
            pstmt.setString(3, question.getExpectedResponsePattern());
            pstmt.setString(4, question.getExpectedResponseLanguageIso());

            // Convert list of required vocabulary to comma-separated string
            String vocabularyStr = null;
            if (question.getRequiredVocabulary() != null && !question.getRequiredVocabulary().isEmpty()) {
                vocabularyStr = String.join(",", question.getRequiredVocabulary());
            }
            pstmt.setString(5, vocabularyStr);

            pstmt.setInt(6, question.getDifficultyLevel());
            pstmt.setInt(7, question.getOrderIndex());
            pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(9, question.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a speaking test question by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM speaking_test_questions WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete all questions for a specific test
     */
    public void deleteByTestId(String testId) throws SQLException {
        String sql = "DELETE FROM speaking_test_questions WHERE test_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, testId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to SpeakingTestQuestion
     */
    private SpeakingTestQuestion mapResultSetToSpeakingTestQuestion(ResultSet rs) throws SQLException {
        SpeakingTestQuestion question = new SpeakingTestQuestion();
        question.setId(rs.getString("id"));
        question.setTestId(rs.getString("test_id"));
        question.setQuestionText(rs.getString("question_text"));
        question.setExpectedResponsePattern(rs.getString("expected_response_pattern"));
        question.setExpectedResponseLanguageIso(rs.getString("expected_response_language_iso"));

        // Convert comma-separated string to list of required vocabulary
        String vocabularyStr = rs.getString("required_vocabulary");
        if (vocabularyStr != null && !vocabularyStr.isEmpty()) {
            List<String> vocabularyList = Arrays.asList(vocabularyStr.split(","));
            question.setRequiredVocabulary(vocabularyList);
        } else {
            question.setRequiredVocabulary(new ArrayList<>());
        }

        question.setDifficultyLevel(rs.getInt("difficulty_level"));
        question.setOrderIndex(rs.getInt("order_index"));
        question.setLastUpdate(rs.getTimestamp("last_updated"));

        return question;
    }
}
