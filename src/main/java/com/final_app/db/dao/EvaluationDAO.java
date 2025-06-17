package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.Evaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for the evaluations table
 */
public class EvaluationDAO {
    /**
     * Insert a new evaluation into the database
     */
    public void insert(Evaluation evaluation) throws SQLException {
        String sql = "INSERT INTO evaluations (id, user_conversation_id, score, max_score, vocab, grammar, feedback, correctness, duration, purpose) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if(evaluation.getId() == null) {
            evaluation.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, evaluation.getId());
            pstmt.setString(2, evaluation.getUserConversationId());
            pstmt.setInt(3, evaluation.getScore());
            pstmt.setInt(4, evaluation.getMaxScore());
            pstmt.setInt(5, evaluation.getVocab());
            pstmt.setInt(6, evaluation.getGrammar());
            pstmt.setString(7, evaluation.getFeedback());
            pstmt.setInt(8, evaluation.getCorrectness());
            pstmt.setInt(9, evaluation.getDuration());
            pstmt.setInt(10, evaluation.getPurpose());

            pstmt.executeUpdate();
        }
    }

    /**
     * Find an evaluation by ID
     */
    public Evaluation findById(String id) throws SQLException {
        String sql = "SELECT * FROM evaluations WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvaluation(rs);
                }
                return null;
            }
        }
    }

    /**
     * Find an evaluation by user conversation ID
     */
    public Evaluation findByUserConversationId(String userConversationId) throws SQLException {
        String sql = "SELECT * FROM evaluations WHERE user_conversation_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userConversationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvaluation(rs);
                }
                return null;
            }
        }
    }

    /**
     * Get all evaluations from the database
     */
    public List<Evaluation> findAll() throws SQLException {
        String sql = "SELECT * FROM evaluations";
        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                evaluations.add(mapResultSetToEvaluation(rs));
            }
        }

        return evaluations;
    }

    /**
     * Update an existing evaluation
     */
    public void update(Evaluation evaluation) throws SQLException {
        String sql = "UPDATE evaluations SET user_conversation_id = ?, score = ?, max_score = ?, " +
                "vocab = ?, grammar = ?, feedback = ?, correctness = ?, duration = ?, purpose = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, evaluation.getUserConversationId());
            pstmt.setInt(2, evaluation.getScore());
            pstmt.setInt(3, evaluation.getMaxScore());
            pstmt.setInt(4, evaluation.getVocab());
            pstmt.setInt(5, evaluation.getGrammar());
            pstmt.setString(6, evaluation.getFeedback());
            pstmt.setString(7, evaluation.getId());
            pstmt.setInt(8, evaluation.getCorrectness());
            pstmt.setInt(9, evaluation.getDuration());
            pstmt.setInt(10, evaluation.getPurpose());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete an evaluation by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM evaluations WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete an evaluation by user conversation ID
     */
    public void deleteByUserConversationId(String userConversationId) throws SQLException {
        String sql = "DELETE FROM evaluations WHERE user_conversation_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userConversationId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to Evaluation
     */
    private Evaluation mapResultSetToEvaluation(ResultSet rs) throws SQLException {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(rs.getString("id"));
        evaluation.setUserConversationId(rs.getString("user_conversation_id"));
        evaluation.setScore(rs.getInt("score"));
        evaluation.setMaxScore(rs.getInt("max_score"));
        evaluation.setVocab(rs.getInt("vocab"));
        evaluation.setGrammar(rs.getInt("grammar"));
        evaluation.setFeedback(rs.getString("feedback"));
        evaluation.setCorrectness(rs.getInt("correctness"));
        evaluation.setDuration(rs.getInt("duration"));
        evaluation.setPurpose(rs.getInt("purpose"));
        return evaluation;
    }
}
