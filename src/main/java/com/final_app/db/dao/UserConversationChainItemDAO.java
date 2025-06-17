package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for the conversation_chains table
 */
public class UserConversationChainItemDAO {
    private final ConversationChainItemDAO conversationChainItemDAO = new ConversationChainItemDAO();
    private final UserConversationDAO userConversationDAO = new UserConversationDAO();
    private final ConversationChainDAO conversationChainDAO = new ConversationChainDAO();

    /**
     * Insert a new conversation into the database
     */
    public void insert(UserConversationChainItem userConversationChainItem) throws SQLException {
        String sql = "INSERT INTO user_conversation_chains (id, user_conversation_id, conversation_chain_id) " +
                "VALUES (?, ?, ?)";

        if(userConversationChainItem.getId() == null || userConversationChainItem.getId().isEmpty()) {
            // Generate a new UUID if the ID is not set
            userConversationChainItem.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, userConversationChainItem.getId());
            pstmt.setString(2, userConversationChainItem.getUserConversationId());
            pstmt.setString(3, userConversationChainItem.getConversationChainId());

            pstmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find a conversation chain item by ID with all related objects loaded
     */
    public UserConversationChainItem findById(String id) throws SQLException {
        String sql = "SELECT * FROM user_conversation_chains WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserConversationChainItem userConversationChainItem = mapResultSetToConversationChain(rs);
                    loadRelatedObjects(userConversationChainItem);
                    return userConversationChainItem;
                }
                return null;
            }
        }
    }

    /**
     * Gets the item by the userconversationid
     * @param userConversationId
     * @return
     * @throws SQLException
     */
    public UserConversationChainItem findByUserConversationId(String userConversationId) throws SQLException {
        String sql = "SELECT * FROM user_conversation_chains WHERE user_conversation_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userConversationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserConversationChainItem userConversationChainItem = mapResultSetToConversationChain(rs);
                    loadRelatedObjects(userConversationChainItem);
                    return userConversationChainItem;
                }
                return null;
            }
        }
    }

    /**
     * Find all conversation_chain items by conversation_chain ID
     */
    public List<UserConversationChainItem> findByConversationChainId(String conversationChainId) throws SQLException {
        String sql = "SELECT * FROM user_conversation_chains WHERE conversation_chain_id = ?";
        List<UserConversationChainItem> userConversationChainItems = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, conversationChainId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserConversationChainItem userConversationChainItem = mapResultSetToConversationChain(rs);
                    loadRelatedObjects(userConversationChainItem);
                    userConversationChainItems.add(userConversationChainItem);
                }
            }
        }

        return userConversationChainItems;
    }

    /**
     * Get all conversation_chains from the database with related objects loaded
     */
    public List<UserConversationChainItem> findAll() throws SQLException {
        String sql = "SELECT * FROM user_conversation_chains";
        List<UserConversationChainItem> userConversationChainItems = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserConversationChainItem userConversationChainItem = mapResultSetToConversationChain(rs);
                loadRelatedObjects(userConversationChainItem);
                userConversationChainItems.add(userConversationChainItem);
            }
        }

        return userConversationChainItems;
    }

    /**
     * Delete a conversation by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM user_conversation_chains WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to Conversation chain
     */
    private UserConversationChainItem mapResultSetToConversationChain(ResultSet rs) throws SQLException {
        UserConversationChainItem userConversationChainItem = new UserConversationChainItem();
        userConversationChainItem.setId(rs.getString("id"));
        userConversationChainItem.setConversationChainId(rs.getString("conversation_chain_id"));
        userConversationChainItem.setUserConversationId(rs.getString("user_conversation_id"));
        userConversationChainItem.setLastUpdate(rs.getTimestamp("last_updated"));
        return userConversationChainItem;
    }

    /**
     * Load related objects for a conversationChain
     */
    private void loadRelatedObjects(UserConversationChainItem userConversationChainItem) throws SQLException {
        ConversationChain conversationChain = conversationChainDAO.findById(userConversationChainItem.getConversationChainId());
        userConversationChainItem.setConversationChain(conversationChain);
        UserConversation userConversation = userConversationDAO.findById(userConversationChainItem.getUserConversationId());
        userConversationChainItem.setUserConversation(userConversation);
    }
}
