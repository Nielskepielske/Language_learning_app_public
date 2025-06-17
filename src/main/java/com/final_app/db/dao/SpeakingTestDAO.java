package com.final_app.db.dao;

import com.final_app.db.DatabaseManager;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.SpeakingTest;
import com.final_app.models.SpeakingTestQuestion;

import javax.swing.text.html.Option;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for the speaking_tests table
 */
public class SpeakingTestDAO {
    private final LanguageDAO languageDAO = new LanguageDAO();
    private final LanguageLevelDAO languageLevelDAO = new LanguageLevelDAO();
    private final SpeakingTestQuestionDAO questionDAO = new SpeakingTestQuestionDAO();

    /**
     * Insert a new speaking test into the database
     */
    public void insert(SpeakingTest speakingTest) throws SQLException {
        String sql = "INSERT INTO speaking_tests (id, title, description, explanation, language_id, language_from_id, level_id, " +
                "grammar_focus, vocabulary_theme) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if(speakingTest.getId() == null || speakingTest.getId().isEmpty()) {
            // Generate a new UUID for the speaking test
            speakingTest.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, speakingTest.getId());
            pstmt.setString(2, speakingTest.getTitle());
            pstmt.setString(3, speakingTest.getDescription());
            pstmt.setString(4, speakingTest.getExplanation());
            pstmt.setString(5, speakingTest.getLanguageId());
            pstmt.setString(6, speakingTest.getLanguageFromId());
            pstmt.setString(7, speakingTest.getLevelId());
            pstmt.setString(8, speakingTest.getGrammarFocus());
            pstmt.setString(9, speakingTest.getVocabularyTheme());

            pstmt.executeUpdate();

            // Insert any associated questions
            if (speakingTest.getQuestions() != null && !speakingTest.getQuestions().isEmpty()) {
                for (SpeakingTestQuestion question : speakingTest.getQuestions()) {
                    question.setTestId(speakingTest.getId());
                    questionDAO.insert(question);
                }
            }
        }
    }

    /**
     * Find a speaking test by ID with all related objects loaded
     */
    public SpeakingTest findById(String id) throws SQLException {
        String sql = "SELECT * FROM speaking_tests WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    SpeakingTest speakingTest = mapResultSetToSpeakingTest(rs);
                    loadRelatedObjects(speakingTest);
                    return speakingTest;
                }
                return null;
            }
        }
    }

    /**
     * Find all speaking tests for a specific language
     */
    public List<SpeakingTest> findByLanguageId(String languageId) throws SQLException {
        String sql = "SELECT * FROM speaking_tests WHERE language_id = ?";
        List<SpeakingTest> speakingTests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, languageId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SpeakingTest speakingTest = mapResultSetToSpeakingTest(rs);
                    loadRelatedObjects(speakingTest);
                    speakingTests.add(speakingTest);
                }
            }
        }

        return speakingTests;
    }

    /**
     * Find all speaking tests for a specific level
     */
    public List<SpeakingTest> findByLevelId(String levelId) throws SQLException {
        String sql = "SELECT * FROM speaking_tests WHERE level_id = ?";
        List<SpeakingTest> speakingTests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, levelId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SpeakingTest speakingTest = mapResultSetToSpeakingTest(rs);
                    loadRelatedObjects(speakingTest);
                    speakingTests.add(speakingTest);
                }
            }
        }

        return speakingTests;
    }

    /**
     * Find speaking tests by grammar focus
     */
    public List<SpeakingTest> findByGrammarFocus(String grammarFocus) throws SQLException {
        String sql = "SELECT * FROM speaking_tests WHERE grammar_focus LIKE ?";
        List<SpeakingTest> speakingTests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + grammarFocus + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SpeakingTest speakingTest = mapResultSetToSpeakingTest(rs);
                    loadRelatedObjects(speakingTest);
                    speakingTests.add(speakingTest);
                }
            }
        }

        return speakingTests;
    }

    /**
     * Find speaking tests by vocabulary theme
     */
    public List<SpeakingTest> findByVocabularyTheme(String vocabularyTheme) throws SQLException {
        String sql = "SELECT * FROM speaking_tests WHERE vocabulary_theme LIKE ?";
        List<SpeakingTest> speakingTests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + vocabularyTheme + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SpeakingTest speakingTest = mapResultSetToSpeakingTest(rs);
                    loadRelatedObjects(speakingTest);
                    speakingTests.add(speakingTest);
                }
            }
        }

        return speakingTests;
    }

    /**
     * Get all speaking tests from the database with related objects loaded
     */
    public List<SpeakingTest> findAll() throws SQLException {
        String sql = "SELECT * FROM speaking_tests";
        List<SpeakingTest> speakingTests = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SpeakingTest speakingTest = mapResultSetToSpeakingTest(rs);
                loadRelatedObjects(speakingTest);
                speakingTests.add(speakingTest);
            }
        }

        return speakingTests;
    }

    /**
     * Update an existing speaking test
     */
    public void update(SpeakingTest speakingTest) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Update the speaking test
            String sql = "UPDATE speaking_tests SET title = ?, description = ?, language_id = ?, " +
                    "level_id = ?, grammar_focus = ?, vocabulary_theme = ?, explanation = ?, last_updated = ? WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, speakingTest.getTitle());
                pstmt.setString(2, speakingTest.getDescription());
                pstmt.setString(3, speakingTest.getLanguageId());
                pstmt.setString(4, speakingTest.getLevelId());
                pstmt.setString(5, speakingTest.getGrammarFocus());
                pstmt.setString(6, speakingTest.getVocabularyTheme());
                pstmt.setString(7, speakingTest.getExplanation());
                pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
                pstmt.setString(9, speakingTest.getId());

                pstmt.executeUpdate();
            }

            // Update associated questions
            if (speakingTest.getQuestions() != null) {
                // Delete questions not in the updated list
                List<String> currentQuestionIds = new ArrayList<>();
                for (SpeakingTestQuestion question : speakingTest.getQuestions()) {
                    if (question.getId() != null) {
                        currentQuestionIds.add(question.getId());
                    }
                }

                // Update or insert questions
                for (SpeakingTestQuestion question : speakingTest.getQuestions()) {
                    question.setTestId(speakingTest.getId());
                    if (question.getId() != null) {
                        questionDAO.update(question);
                    } else {
                        questionDAO.insert(question);
                    }
                }
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
     * Delete a speaking test by ID
     * This will also delete all associated questions due to the ON DELETE CASCADE constraint
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM speaking_tests WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper method to map ResultSet to SpeakingTest
     */
    private SpeakingTest mapResultSetToSpeakingTest(ResultSet rs) throws SQLException {
        SpeakingTest speakingTest = new SpeakingTest();
        speakingTest.setId(rs.getString("id"));
        speakingTest.setTitle(rs.getString("title"));
        speakingTest.setDescription(rs.getString("description"));
        speakingTest.setExplanation(rs.getString("explanation"));
        speakingTest.setLanguageId(rs.getString("language_id"));
        speakingTest.setLanguageFromId(rs.getString("language_from_id"));
        speakingTest.setLevelId(rs.getString("level_id"));
        speakingTest.setGrammarFocus(rs.getString("grammar_focus"));
        speakingTest.setVocabularyTheme(rs.getString("vocabulary_theme"));
        speakingTest.setLastUpdate(rs.getTimestamp("last_updated"));
        return speakingTest;
    }

    /**
     * Load related objects for a speaking test
     */
    private void loadRelatedObjects(SpeakingTest speakingTest) throws SQLException {
        // Load language
        Language language = languageDAO.findById(speakingTest.getLanguageId());
        speakingTest.setLanguage(language);

        // Load language from
        if (speakingTest.getLanguageFromId() != null) {
            Language languageFrom = languageDAO.findById(speakingTest.getLanguageFromId());
            speakingTest.setLanguageFrom(languageFrom);
        }

        // Load language level
        Optional<LanguageLevel> level = languageLevelDAO.findById(speakingTest.getLevelId());
        speakingTest.setLanguageLevel(level.orElse(null));

        // Load questions
        List<SpeakingTestQuestion> questions = questionDAO.findByTestId(speakingTest.getId());
        speakingTest.setQuestions(questions);
    }
}
