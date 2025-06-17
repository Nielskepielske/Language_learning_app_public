package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.ConversationChain;
import com.final_app.models.ConversationChainItem;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for the conversation_chains table
 */
public class ConversationChainItemDAO {
    private final LanguageDAO languageDAO = new LanguageDAO();
    private final LanguageLevelDAO languageLevelDAO = new LanguageLevelDAO();
    private final ScenarioDAO scenarioDAO = new ScenarioDAO();
    private final ConversationDAO conversationDAO = new ConversationDAO();

    /**
     * Insert a new conversation_chain_item into the database
     */
    public void insert(ConversationChainItem conversationChainItem) throws SQLException {
        String sql = "INSERT INTO conversation_chain_items (id, conversation_chain_id, conversation_id, conversation_index) " +
                "VALUES (?, ?, ?, ?)";

        if(conversationChainItem.getId() == null || conversationChainItem.getId().isEmpty()) {
            conversationChainItem.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, conversationChainItem.getId());
            pstmt.setString(2, conversationChainItem.getConversationChainId());
            pstmt.setString(3, conversationChainItem.getConversationId());
            pstmt.setInt(4, conversationChainItem.getConversationIndex());
            pstmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find a conversation_chain_item by ID with all related objects loaded
     */
    public ConversationChainItem findById(String id) throws SQLException {
        String sql = "SELECT * FROM conversation_chain_items WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ConversationChainItem conversationChainItem = mapResultSetToConversationChain(rs);
                    loadRelatedObjects(conversationChainItem);
                    return conversationChainItem;
                }
                return null;
            }
        }
    }

    /**
     * Find all conversation_chains_items by conversationId ID
     */
    public List<ConversationChainItem> findByConversationId(String conversationId) throws SQLException {
        String sql = "SELECT * FROM conversation_chain_items WHERE conversation_id = ?";
        List<ConversationChainItem> conversationChainItems = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, conversationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ConversationChainItem conversationChainItem = mapResultSetToConversationChain(rs);
                    loadRelatedObjects(conversationChainItem);
                    conversationChainItems.add(conversationChainItem);
                }
            }
        }

        return conversationChainItems;
    }

    /**
     * Find all conversation_chains_item by level ID
     */
    public List<ConversationChainItem> findByConversationChainId(String conversationChainId) throws SQLException {
        String sql = "SELECT * FROM conversation_chain_items WHERE conversation_chain_id = ?";
        List<ConversationChainItem> conversationChainItems = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, conversationChainId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ConversationChainItem conversationChainItem = mapResultSetToConversationChain(rs);
                    loadRelatedObjects(conversationChainItem);
                    conversationChainItems.add(conversationChainItem);
                }
            }
        }

        return conversationChainItems;
    }

    /**
     * Get all conversation_chains_items from the database with related objects loaded
     */
    public List<ConversationChainItem> findAll() throws SQLException {
        String sql = "SELECT * FROM conversation_chain_items";
        List<ConversationChainItem> conversationChainItems = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ConversationChainItem conversationChainItem = mapResultSetToConversationChain(rs);
                loadRelatedObjects(conversationChainItem);
                conversationChainItems.add(conversationChainItem);
            }
        }

        return conversationChainItems;
    }

    /**
     * Update an existing conversation_chain_item
     */
    public void update(ConversationChainItem conversationChainItem) throws SQLException {
        String sql = "UPDATE conversation_chain_items SET conversation_chain_id = ?, conversation_id = ?, conversation_index = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, conversationChainItem.getConversationChainId());
            pstmt.setString(2, conversationChainItem.getConversationId());
            pstmt.setInt(3, conversationChainItem.getConversationIndex());
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(5, conversationChainItem.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a conversation by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM conversation_chain_items WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to Conversation chain item
     */
    private ConversationChainItem mapResultSetToConversationChain(ResultSet rs) throws SQLException {
        ConversationChainItem conversationChainItem = new ConversationChainItem();
        conversationChainItem.setId(rs.getString("id"));
        conversationChainItem.setConversationId(rs.getString("conversation_id"));
        conversationChainItem.setConversationChainId(rs.getString("conversation_chain_id"));
        conversationChainItem.setConversationIndex(rs.getInt("conversation_index"));
        conversationChainItem.setLastUpdate(rs.getTimestamp("last_updated"));

        return conversationChainItem;
    }

    /**
     * Load related objects for a conversationChainItem
     */
    private void loadRelatedObjects(ConversationChainItem conversationChainItem) throws SQLException {
        conversationChainItem.setConversation(conversationDAO.findById(conversationChainItem.getConversationId()));
    }
}
