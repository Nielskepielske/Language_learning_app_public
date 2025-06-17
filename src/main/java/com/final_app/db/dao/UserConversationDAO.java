package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for the user_conversations table
 */
public class UserConversationDAO {
    private final UserDAO userDAO = new UserDAO();
    private final ConversationDAO conversationDAO = new ConversationDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final EvaluationDAO evaluationDAO = new EvaluationDAO();

    /**
     * Insert a new user conversation into the database
     */
    public void insert(UserConversation userConversation) throws SQLException {
        String sql = "INSERT INTO user_conversations (id, user_id, conversation_id, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";

        if(userConversation.getId() == null || userConversation.getId().isEmpty()) {
            // Generate a new UUID if the ID is not provided
            userConversation.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, userConversation.getId());
            pstmt.setString(2, userConversation.getUserId());
            pstmt.setString(3, userConversation.getConversationId());
            pstmt.setString(4, userConversation.getStatus());
            pstmt.setTimestamp(5, new Timestamp(userConversation.getCreatedAt().getTime()));
            pstmt.setTimestamp(6, new Timestamp(userConversation.getUpdatedAt().getTime()));

            pstmt.executeUpdate();
        }
    }

    /**
     * Find a user conversation by ID with all related objects loaded
     */
    public UserConversation findById(String id) throws SQLException {
        String sql = "SELECT * FROM user_conversations WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserConversation userConversation = mapResultSetToUserConversation(rs);
                    loadRelatedObjects(userConversation);
                    return userConversation;
                }
                return null;
            }
        }
    }

    /**
     * Find all user conversations for a specific user
     */
    public List<UserConversation> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM user_conversations WHERE user_id = ?";
        List<UserConversation> userConversations = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserConversation userConversation = mapResultSetToUserConversation(rs);
                    loadRelatedObjects(userConversation);
                    userConversations.add(userConversation);
                }
            }
        }

        return userConversations;
    }

    /**
     * Find all user conversations for a specific conversation
     */
    public List<UserConversation> findByConversationId(String conversationId) throws SQLException {
        String sql = "SELECT * FROM user_conversations WHERE conversation_id = ?";
        List<UserConversation> userConversations = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, conversationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserConversation userConversation = mapResultSetToUserConversation(rs);
                    loadRelatedObjects(userConversation);
                    userConversations.add(userConversation);
                }
            }
        }

        return userConversations;
    }

    /**
     * Find a user conversation by user ID and conversation ID
     */
    public List<UserConversation> findByUserIdAndConversationId(String userId, String conversationId) throws SQLException {
        String sql = "SELECT * FROM user_conversations WHERE user_id = ? AND conversation_id = ?";

        List<UserConversation> userConversations = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, conversationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserConversation userConversation = mapResultSetToUserConversation(rs);
                    loadRelatedObjects(userConversation);
                    userConversations.add(userConversation);
                }
            }
        }
        return userConversations;
    }

    /**
     * Get all user conversations from the database with related objects loaded
     */
    public List<UserConversation> findAll() throws SQLException {
        String sql = "SELECT * FROM user_conversations";
        List<UserConversation> userConversations = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserConversation userConversation = mapResultSetToUserConversation(rs);
                loadRelatedObjects(userConversation);
                userConversations.add(userConversation);
            }
        }

        return userConversations;
    }

    /**
     * Update an existing user conversation
     */
    public void update(UserConversation userConversation) throws SQLException {
        String sql = "UPDATE user_conversations SET user_id = ?, conversation_id = ?, status = ?, updated_at = ?, completed_at = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userConversation.getUserId());
            pstmt.setString(2, userConversation.getConversationId());
            pstmt.setString(3, userConversation.getStatus());
            pstmt.setTimestamp(4, new Timestamp(userConversation.getUpdatedAt().getTime()));

            // Handle null completedAt
            if (userConversation.getCompletedAt() != null) {
                pstmt.setTimestamp(5, new Timestamp(userConversation.getCompletedAt().getTime()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }

            pstmt.setString(6, userConversation.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Update only the status of a user conversation
     */
    public void updateStatus(String id, String status) throws SQLException {
        String sql = "UPDATE user_conversations SET status = ?, updated_at = ?";

        // If status is COMPLETED, also update completed_at
        if (status.equals("COMPLETED")) {
            sql += ", completed_at = ? WHERE id = ?";
        } else {
            sql += " WHERE id = ?";
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));

            if (status.equals("COMPLETED")) {
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(4, id);
            } else {
                pstmt.setString(3, id);
            }

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a user conversation by ID
     * This will also delete all associated messages and evaluations due to the ON DELETE CASCADE constraint
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM user_conversations WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete all user conversations for a specific user
     */
    public void deleteByUserId(String userId) throws SQLException {
        String sql = "DELETE FROM user_conversations WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to UserConversation
     */
    private UserConversation mapResultSetToUserConversation(ResultSet rs) throws SQLException {
        UserConversation userConversation = new UserConversation();
        userConversation.setId(rs.getString("id"));
        userConversation.setUserId(rs.getString("user_id"));
        userConversation.setConversationId(rs.getString("conversation_id"));
        userConversation.setStatus(rs.getString("status"));

        // Map timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            userConversation.setCreatedAt(createdAt);
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            userConversation.setUpdatedAt(updatedAt);
        }

        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            userConversation.setCompletedAt(completedAt);
        }

        return userConversation;
    }

    /**
     * Load related objects for a user conversation
     */
    private void loadRelatedObjects(UserConversation userConversation) throws SQLException {
        // Load user
        User user = userDAO.findById(userConversation.getUserId());
        userConversation.setUser(user);

        // Load conversation
        Conversation conversation = conversationDAO.findById(userConversation.getConversationId());
        userConversation.setConversation(conversation);

        // Load messages
        List<Message> messages = messageDAO.findByUserConversationId(userConversation.getId());
        userConversation.setMessages(messages);

        // Load evaluation (if any)
        try {
            Evaluation evaluation = evaluationDAO.findByUserConversationId(userConversation.getId());
            userConversation.setEvaluation(evaluation);
        } catch (SQLException e) {
            // It's okay if there's no evaluation, just continue
            System.out.println("No evaluation found for user conversation " + userConversation.getId());
        }
    }
}