package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for the conversation_chains table
 */
public class ConversationChainDAO {
    private final LanguageDAO languageDAO = new LanguageDAO();
    private final LanguageLevelDAO languageLevelDAO = new LanguageLevelDAO();
    private final ScenarioDAO scenarioDAO = new ScenarioDAO();
    private final ConversationChainItemDAO conversationChainItemDAO = new ConversationChainItemDAO();

    /**
     * Insert a new conversation into the database
     */
    public void insert(ConversationChain conversationChain) throws SQLException {
        String sql = "INSERT INTO conversation_chains (id, title, description, language_id, language_from_id, level_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        if(conversationChain.getId() == null || conversationChain.getId().isEmpty()) {
            conversationChain.setId(UUID.randomUUID().toString());
        }
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, conversationChain.getId());
            pstmt.setString(2, conversationChain.getTitle());
            pstmt.setString(3, conversationChain.getDescription());
            pstmt.setString(4, conversationChain.getLanguageId());
            pstmt.setString(5, conversationChain.getLanguageFromId());
            pstmt.setString(6, conversationChain.getLevelId());

            pstmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find a conversation by ID with all related objects loaded
     */
    public ConversationChain findById(String id) throws SQLException {
        String sql = "SELECT * FROM conversation_chains WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ConversationChain conversationChain = mapResultSetToConversationChain(rs);
                    loadRelatedObjects(conversationChain);
                    return conversationChain;
                }
                return null;
            }
        }
    }

    /**
     * Find all conversation_chains by language ID
     */
    public List<ConversationChain> findByLanguageId(String languageId) throws SQLException {
        String sql = "SELECT * FROM conversation_chains WHERE language_id = ?";
        List<ConversationChain> conversationChains = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, languageId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ConversationChain conversationChain = mapResultSetToConversationChain(rs);
                    loadRelatedObjects(conversationChain);
                    conversationChains.add(conversationChain);
                }
            }
        }

        return conversationChains;
    }

    /**
     * Find all conversation_chains by level ID
     */
    public List<ConversationChain> findByLevelId(String levelId) throws SQLException {
        String sql = "SELECT * FROM conversation_chains WHERE level_id = ?";
        List<ConversationChain> conversationChains = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, levelId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ConversationChain conversationChain = mapResultSetToConversationChain(rs);
                    loadRelatedObjects(conversationChain);
                    conversationChains.add(conversationChain);
                }
            }
        }

        return conversationChains;
    }

    /**
     * Get all conversation_chains from the database with related objects loaded
     */
    public List<ConversationChain> findAll() throws SQLException {
        String sql = "SELECT * FROM conversation_chains";
        List<ConversationChain> conversationChains = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ConversationChain conversationChain = mapResultSetToConversationChain(rs);
                loadRelatedObjects(conversationChain);
                conversationChains.add(conversationChain);
            }
        }

        return conversationChains;
    }

    /**
     * Update an existing conversation
     */
    public void update(ConversationChain conversationChain) throws SQLException {
        String sql = "UPDATE conversation_chains SET title = ?, description = ?, language_id = ?, language_from_id = ?, " +
                "level_id = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, conversationChain.getTitle());
            pstmt.setString(2, conversationChain.getDescription());
            pstmt.setString(3, conversationChain.getLanguageId());
            pstmt.setString(4, conversationChain.getLanguageFromId());
            pstmt.setString(5, conversationChain.getLevelId());
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(7, conversationChain.getId());


            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a conversation by ID
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM conversation_chains WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to Conversation chain
     */
    private ConversationChain mapResultSetToConversationChain(ResultSet rs) throws SQLException {
        ConversationChain conversationChain = new ConversationChain();
        conversationChain.setId(rs.getString("id"));
        conversationChain.setTitle(rs.getString("title"));
        conversationChain.setDescription(rs.getString("description"));
        conversationChain.setLanguageId(rs.getString("language_id"));
        conversationChain.setLanguageFromId(rs.getString("language_from_id"));
        conversationChain.setLevelId(rs.getString("level_id"));
        conversationChain.setLastUpdate(rs.getTimestamp("last_updated"));
        return conversationChain;
    }

    /**
     * Load related objects for a conversationChain
     */
    private void loadRelatedObjects(ConversationChain conversationChain) throws SQLException {
        // Load language
        Language language = languageDAO.findById(conversationChain.getLanguageId());
        conversationChain.setLanguage(language);
        Language languageFrom = languageDAO.findById(conversationChain.getLanguageFromId());
        conversationChain.setLanguageFrom(languageFrom);
        Optional<LanguageLevel> level = languageLevelDAO.findById(conversationChain.getLevelId());
        conversationChain.setLanguageLevel(level.orElse(null));

        // Load associated conversationItems
        List<ConversationChainItem> conversationChainItems = conversationChainItemDAO.findByConversationChainId(conversationChain.getId());
        conversationChain.setConversations(conversationChainItems);

    }
}
