package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.Conversation;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.Scenario;
import com.final_app.tools.PerformanceTimer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConversationDAO {
    private final LanguageDAO languageDAO = new LanguageDAO();
    private final LanguageLevelDAO languageLevelDAO = new LanguageLevelDAO();
    private final ScenarioDAO scenarioDAO = new ScenarioDAO();

    public void insert(Conversation conversation) throws SQLException {
        String sql = "INSERT INTO conversations (id, title, description, language_id, language_from_id, level_id, scenario_id, start_prompt, model) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (conversation.getId() == null || conversation.getId().isEmpty()) {
            conversation.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, conversation.getId());
            pstmt.setString(2, conversation.getTitle());
            pstmt.setString(3, conversation.getDescription());
            pstmt.setString(4, conversation.getLanguageId());
            pstmt.setString(5, conversation.getLanguageFromId());
            pstmt.setString(6, conversation.getLevelId());
            pstmt.setString(7, conversation.getScenarioId());
            pstmt.setString(8, conversation.getStartPrompt());
            pstmt.setString(9, conversation.getModel());

            pstmt.executeUpdate();
        }
    }

    public Conversation findById(String id) throws SQLException {
        String sql = "SELECT * FROM conversations WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Conversation conversation = mapResultSetToConversation(rs);
                    loadRelatedObjects(conversation);
                    return conversation;
                }
                return null;
            }
        }
    }

    public List<Conversation> findAll() throws SQLException {
        PerformanceTimer.start("Dao: ConversationDAO.findAll");
        String sql = "SELECT * FROM conversations";
        List<Conversation> conversations = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Conversation conversation = mapResultSetToConversation(rs);
                loadRelatedObjects(conversation);
                conversations.add(conversation);
            }
            PerformanceTimer.stop("Dao: ConversationDAO.findAll");
        }
        return conversations;
    }

    public List<Conversation> findAllWithoutRelations() throws SQLException {
        PerformanceTimer.start("Dao: ConversationDAO.findAllWithoutRelations");
        String sql = "SELECT * FROM conversations";
        List<Conversation> conversations = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Conversation conversation = mapResultSetToConversation(rs);
                conversations.add(conversation);
            }
            PerformanceTimer.stop("Dao: ConversationDAO.findAllWithoutRelations");
        }
        return conversations;
    }

    /**
     * Find all conversations by level ID
     */
    public List<Conversation> findByLevelId(String levelId) throws SQLException {
        String sql = "SELECT * FROM conversations WHERE level_id = ?";
        List<Conversation> conversations = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, levelId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Conversation conversation = mapResultSetToConversation(rs);
                    //loadRelatedObjects(conversation);
                    conversations.add(conversation);
                }
            }
        }

        return conversations;
    }

    /**
     * Find all conversations by language ID
     */
    public List<Conversation> findByLanguageId(String languageId) throws SQLException {
        String sql = "SELECT * FROM conversations WHERE language_id = ?";
        List<Conversation> conversations = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, languageId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Conversation conversation = mapResultSetToConversation(rs);
                    loadRelatedObjects(conversation);
                    conversations.add(conversation);
                }
            }
        }

        return conversations;
    }

    public void update(Conversation conversation) throws SQLException {
        String sql = "UPDATE conversations SET title = ?, description = ?, language_id = ?, language_from_id = ?, " +
                "level_id = ?, scenario_id = ?, start_prompt = ?, model = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, conversation.getTitle());
            pstmt.setString(2, conversation.getDescription());
            pstmt.setString(3, conversation.getLanguageId());
            pstmt.setString(4, conversation.getLanguageFromId());
            pstmt.setString(5, conversation.getLevelId());
            pstmt.setString(6, conversation.getScenarioId());
            pstmt.setString(7, conversation.getStartPrompt());
            pstmt.setString(8, conversation.getModel());
            pstmt.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(10, conversation.getId());

            pstmt.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM conversations WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        Conversation conversation = new Conversation();
        conversation.setId(rs.getString("id"));
        conversation.setTitle(rs.getString("title"));
        conversation.setDescription(rs.getString("description"));
        conversation.setLanguageId(rs.getString("language_id"));
        conversation.setLanguageFromId(rs.getString("language_from_id"));
        conversation.setLevelId(rs.getString("level_id"));
        conversation.setScenarioId(rs.getString("scenario_id"));
        conversation.setStartPrompt(rs.getString("start_prompt"));
        conversation.setModel(rs.getString("model"));
        conversation.setLastUpdate(rs.getTimestamp("last_updated"));
        return conversation;
    }

    private void loadRelatedObjects(Conversation conversation) throws SQLException {
        Language language = languageDAO.findById(conversation.getLanguageId());
        conversation.setLanguage(language);

        Language languageFrom = languageDAO.findById(conversation.getLanguageFromId());
        conversation.setLanguageFrom(languageFrom);

        Optional<LanguageLevel> level = languageLevelDAO.findById(conversation.getLevelId());
        conversation.setLanguageLevel(level.orElse(null));

        Scenario scenario = scenarioDAO.findById(conversation.getScenarioId());
        conversation.setScenario(scenario);
    }
}

