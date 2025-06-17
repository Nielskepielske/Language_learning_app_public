package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for the messages table
 */
public class MessageDAO {
    /**
     * Insert a new message into the database
     */
    public void insert(Message message) throws SQLException {
        String sql = "INSERT INTO messages (id, message_index, user_conversation_id, text, sender, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        if(message.getId() == null || message.getId().isEmpty()) {
            message.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, message.getId());
            pstmt.setInt(2, message.getIndex());
            pstmt.setString(3, message.getUserConversationId());
            pstmt.setString(4, message.getText());
            pstmt.setString(5, message.getSender());

            // Convert Java LocalDateTime to SQL Timestamp
            if (message.getTimestamp() != null) {
                pstmt.setTimestamp(6, new Timestamp(message.getTimestamp().getTime()));
            } else {
                pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            }

            pstmt.executeUpdate();
        }
    }

    /**
     * Find a message by ID
     */
    public Message findById(String id) throws SQLException {
        String sql = "SELECT * FROM messages WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMessage(rs);
                }
                return null;
            }
        }
    }

    /**
     * Find all messages for a specific user conversation
     */
    public List<Message> findByUserConversationId(String userConversationId) throws SQLException {
        String sql = "SELECT * FROM messages WHERE user_conversation_id = ? ORDER BY timestamp";
        List<Message> messages = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userConversationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }
        }

        return messages;
    }

    /**
     * Get all messages from the database
     */
    public List<Message> findAll() throws SQLException {
        String sql = "SELECT * FROM messages";
        List<Message> messages = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        }

        return messages;
    }

    /**
     * Update an existing message
     */
    public void update(Message message) throws SQLException {
        String sql = "UPDATE messages SET message_index = ?, user_conversation_id = ?, text = ?, sender = ?, timestamp = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, message.getIndex());
            pstmt.setString(2, message.getUserConversationId());
            pstmt.setString(3, message.getText());
            pstmt.setString(4, message.getSender());
            pstmt.setTimestamp(5, new Timestamp(message.getTimestamp().getTime()));
            pstmt.setString(6, message.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a message by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM messages WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete all messages for a specific user conversation
     */
    public void deleteByUserConversationId(String userConversationId) throws SQLException {
        String sql = "DELETE FROM messages WHERE user_conversation_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userConversationId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to Message
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getString("id"));
        message.setIndex(rs.getInt("message_index"));
        message.setUserConversationId(rs.getString("user_conversation_id"));
        message.setText(rs.getString("text"));
        message.setSender(rs.getString("sender"));

        // Convert SQL Timestamp to Java LocalDateTime
        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            message.setTimestamp(timestamp);
        }

        return message;
    }
}
