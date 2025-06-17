package com.final_app.db;

import com.final_app.models.Language;
import com.final_app.tools.TranslationManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class responsible for managing database connections
 * and initializing the database schema.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:h2:./data/lingualeap;AUTO_SERVER=TRUE;";
    //private static final String DB_URL = "jdbc:sqlite:lingualeap.db";
    //private static final String DB_URL = "jdbc:sqlite:lingualeap.db?cache=shared&busy_timeout=5000";
    private static DatabaseManager instance;

    private static HikariDataSource dataSource;

    private static final int THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors());

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL); // same URL you were using
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(THREAD_COUNT + 2); // for embedded, 4-8 is optimal
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("Hikari-H2-Pool");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private DatabaseManager() {
        // Initialize database if needed
        try (Connection conn = getConnection()) {
            // Enable foreign keys

            // Create all tables
            initializeDatabase(conn);

            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the singleton instance of DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }



    /**
     * Initialize the database by creating all necessary tables
     */
    private void initializeDatabase(Connection conn) throws Exception {

        // user tables
        createUsersTable(conn);
        createUserStatsTable(conn);
        // language tables
        createLanguageSystemTable(conn);
        createLanguagesTable(conn);
        createLanguageLevelsTable(conn);
        createUserLanguagesTable(conn);
        // conversation tables
        createScenariosTable(conn);
        createScenarioKeyPointsTable(conn);
        createConversationsTable(conn);
        createUserConversationsTable(conn);
        createMessagesTable(conn);
        createEvaluationsTable(conn);
        createConversationChainsTable(conn);
        createConversationChainItemsTable(conn);
        createUserConversationChainTable(conn);
        // speaking test tables
        createSpeakingTestTable(conn);
        createSpeakingTestQuestionTable(conn);
        createUserSpeakingTestTable(conn);
        createUserSpeakingTestResponseTable(conn);

        createGrammarRulesTable(conn);
        createUsedVocabularyTable(conn);

        // Settings table
        createSettingsTable(conn);

        // Translations table
        createSystemComponentsTable(conn);

        // Insert initial data if needed
        insertInitialData(conn);
        // Translation manager init
        TranslationManager.init(dataSource, new Language(null, "English", "en", "#000000", 1000));
    }

    private void createUsersTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "user_name VARCHAR(255) NOT NULL," +
                "email VARCHAR(255) UNIQUE NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "photo_path VARCHAR(255)," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP"+
                ");";
        conn.createStatement().execute(sql);
    }

    private void createUserStatsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS user_stats (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "user_id VARCHAR(255) NOT NULL," +
                "level INTEGER NOT NULL," +
                "total_xp INTEGER NOT NULL," +
                "streak INTEGER NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);

        // Create index for faster lookups
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_user_stats_user_id ON user_stats(user_id);");
    }

    private void createLanguagesTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS languages (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "system_id VARCHAR(255) NOT NULL," +
                "name VARCHAR(255) NOT NULL," +
                "iso TEXT NOT NULL," +
                "color TEXT NOT NULL," +
                "max_xp INTEGER NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY (system_id) REFERENCES language_systems(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);
    }

    private void createLanguageLevelsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS language_levels (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "system_id VARCHAR(255) NOT NULL," +
                "name VARCHAR(255) NOT NULL," +
                "\"value\" INTEGER NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY(system_id) REFERENCES language_systems(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);
    }

    private void createUserLanguagesTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS user_languages (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "user_id VARCHAR(255) NOT NULL," +
                "language_id VARCHAR(255) NOT NULL," +
                "level_id VARCHAR(255) NOT NULL," +
                "xp INTEGER NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE RESTRICT," +
                "FOREIGN KEY (level_id) REFERENCES language_levels(id) ON DELETE RESTRICT," +
                "UNIQUE(user_id, language_id)" +
                ");";
        conn.createStatement().execute(sql);

        // Create indexes
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_user_languages_user_id ON user_languages(user_id);");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_user_languages_language_id ON user_languages(language_id);");
    }

    private void createScenariosTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS scenarios (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "description TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP"+
                ");";
        conn.createStatement().execute(sql);
    }

    private void createScenarioKeyPointsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS scenario_key_points (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "scenario_id VARCHAR(255) NOT NULL," +
                "key_point TEXT NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY (scenario_id) REFERENCES scenarios(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);

        // Create index
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_scenario_key_points_scenario_id ON scenario_key_points(scenario_id);");
    }

    private void createConversationsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS conversations (" +
                "id VARCHAR(255) PRIMARY KEY," +
                "title VARCHAR(255) NOT NULL," +
                "description TEXT NOT NULL," +
                "language_id VARCHAR(255) NOT NULL," +
                "language_from_id VARCHAR(255) NOT NULL," +
                "level_id VARCHAR(255) NOT NULL," +
                "scenario_id VARCHAR(255) NOT NULL," +
                "start_prompt TEXT NOT NULL," +
                "model VARCHAR(255) NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE RESTRICT," +
                "FOREIGN KEY (level_id) REFERENCES language_levels(id) ON DELETE RESTRICT," +
                "FOREIGN KEY (scenario_id) REFERENCES scenarios(id) ON DELETE RESTRICT" +
                ");";
        conn.createStatement().execute(sql);

        // Create indexes
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_conversations_language_id ON conversations(language_id);");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_conversations_level_id ON conversations(level_id);");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_conversations_scenario_id ON conversations(scenario_id);");
    }

    private void createUserConversationsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS user_conversations (" +
                "id VARCHAR(255) PRIMARY KEY ," +
                "user_id VARCHAR(255) NOT NULL," +
                "conversation_id VARCHAR(255) NOT NULL," +
                "status TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "completed_at TIMESTAMP NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE RESTRICT" +
                ");";
        conn.createStatement().execute(sql);

        // Create indexes
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_user_conversations_user_id ON user_conversations(user_id);");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_user_conversations_conversation_id ON user_conversations(conversation_id);");
    }

    private void createMessagesTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS messages (" +
                "id VARCHAR(255) PRIMARY KEY ," +
                "user_conversation_id VARCHAR(255) NOT NULL," +
                "message_index INTEGER NOT NULL," +
                "text TEXT NOT NULL," +
                "sender TEXT NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_conversation_id) REFERENCES user_conversations(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);

        // Create index
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_messages_user_conversation_id ON messages(user_conversation_id);");
    }

    private void createEvaluationsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS evaluations (" +
                "id VARCHAR(255) PRIMARY KEY ," +
                "user_conversation_id VARCHAR(255) NOT NULL," +
                "score INTEGER NOT NULL," +
                "max_score INTEGER NOT NULL," +
                "vocab INTEGER NOT NULL," +
                "grammar INTEGER NOT NULL," +
                "feedback TEXT NOT NULL," +
                "correctness INTEGER DEFAULT 0," +
                "duration INTEGER DEFAULT 0," +
                "purpose INTEGER DEFAULT 0," +
                "FOREIGN KEY (user_conversation_id) REFERENCES user_conversations(id) ON DELETE CASCADE," +
                "UNIQUE(user_conversation_id)" +
                ");";
        conn.createStatement().execute(sql);

        // Create index
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_evaluations_user_conversation_id ON evaluations(user_conversation_id);");
    }
    /**
     * Create LanguageSystem table
     */
    private void createLanguageSystemTable(Connection conn) throws SQLException {
       String sql = "CREATE TABLE IF NOT EXISTS language_systems (" +
       "id VARCHAR(255) PRIMARY KEY ," +
       "name TEXT NOT NULL," +
       "description TEXT NOT NULL," +
       "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
       ");";
       conn.createStatement().execute(sql);
    }
    /**
     * Create Conversation Chains
     */
    private void createConversationChainsTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS conversation_chains ("+
                "id VARCHAR(255) PRIMARY KEY ," +
                "level_id VARCHAR(255) NOT NULL," +
                "language_id VARCHAR(255) NOT NULL," +
                "language_from_id VARCHAR(255) NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (level_id) REFERENCES language_levels(id) ON DELETE CASCADE," +
                "FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);
    }

    /**
     * Create Conversation chain Items
     */
    private void createConversationChainItemsTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS conversation_chain_items (" +
                "id VARCHAR(255) PRIMARY KEY ," +
                "conversation_id VARCHAR(255) NOT NULL," +
                "conversation_chain_id VARCHAR(255) NOT NULL," +
                "conversation_index INTEGER NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE," +
                "FOREIGN KEY (conversation_chain_id) REFERENCES conversation_chains(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);
    }

    /**
     * Create User Conversation Chain
     */
    private void createUserConversationChainTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS user_conversation_chains (" +
                "id VARCHAR(255) PRIMARY KEY ," +
                "user_conversation_id VARCHAR(255) NOT NULL," +
                "conversation_chain_id VARCHAR(255) NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_conversation_id) REFERENCES user_conversations(id) ON DELETE CASCADE," +
                "FOREIGN KEY (conversation_chain_id) REFERENCES conversation_chains(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);
    }

    // Part for speaking tests

    private void createSpeakingTestTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS speaking_tests(" +
                "id VARCHAR(255) PRIMARY KEY ," +
                "title TEXT NOT NULL," +
                "description TEXT NOT NULL," +
                "explanation TEXT NOT NULL," +
                "language_id VARCHAR(255) NOT NULL," +
                "language_from_id VARCHAR(255) NOT NULL," +
                "level_id VARCHAR(255) NOT NULL," +
                "grammar_focus TEXT NOT NULL," +
                "vocabulary_theme TEXT NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE," +
                "FOREIGN KEY (language_from_id) REFERENCES languages(id) ON DELETE CASCADE," +
                "FOREIGN KEY (level_id) REFERENCES language_levels(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);
    }

    private void createSpeakingTestQuestionTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS speaking_test_questions(" +
                "id VARCHAR(255) PRIMARY KEY ," +
                "test_id VARCHAR(255) NOT NULL," +
                "question_text TEXT NOT NULL," +
                "expected_response_pattern TEXT NOT NULL," +
                "expected_response_language_iso TEXT NOT NULL," +
                "required_vocabulary TEXT," +
                "difficulty_level INTEGER," +
                "order_index INTEGER NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (test_id) REFERENCES speaking_tests(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);
    }

    private void createUserSpeakingTestTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS user_speaking_tests(" +
                "id VARCHAR(255) PRIMARY KEY ," +
                "user_id VARCHAR(255) NOT NULL," +
                "test_id VARCHAR(255) NOT NULL," +
                "started_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "completed_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "status TEXT NOT NULL," +
                "score INTEGER," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (test_id) REFERENCES speaking_tests(id) ON DELETE CASCADE" +
                ");";
        conn.createStatement().execute(sql);
    }

    /**
     * Function to initialize the UserSpeakingTestResponseTable
     * @param conn
     * @throws SQLException
     */
    private void createUserSpeakingTestResponseTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS user_speaking_test_responses("+
                "id VARCHAR(255) PRIMARY KEY ,"+
                "user_speaking_test_id VARCHAR(255) NOT NULL,"+
                "question_id VARCHAR(255) NOT NULL,"+
                "question_index INTEGER NOT NULL,"+
                "transcribed_text TEXT," +
                "responded_at DATETIME DEFAULT CURRENT_TIMESTAMP,"+
                "grammar_score INTEGER,"+
                "vocabulary_score INTEGER,"+
                "overall_score INTEGER,"+
                "feedback TEXT NOT NULL,"+
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(user_speaking_test_id) REFERENCES user_speaking_tests(id) ON DELETE CASCADE,"+
                "FOREIGN KEY(question_id) REFERENCES speaking_test_questions(id) ON DELETE CASCADE"+
                ");";
        conn.createStatement().execute(sql);
    }

    private void createGrammarRulesTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS response_grammar_evaluations("+
                "id VARCHAR(255) PRIMARY KEY ,"+
                "response_id VARCHAR(255) NOT NULL,"+
                "grammar_rule TEXT NOT NULL,"+
                "is_correct BIT DEFAULT 0,"+
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY(response_id) REFERENCES user_speaking_test_responses(id) ON DELETE CASCADE"+
                ");";
        conn.createStatement().execute(sql);
    }

    private void createUsedVocabularyTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS response_vocabulary_usage("+
                "id VARCHAR(255) PRIMARY KEY ,"+
                "response_id VARCHAR(255) NOT NULL,"+
                "vocabulary_word TEXT NOT NULL,"+
                "was_used BIT DEFAULT 0,"+
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY(response_id) REFERENCES user_speaking_test_responses(id) ON DELETE CASCADE"+
                ");";
        conn.createStatement().execute(sql);
    }

    private void createSettingsTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS settings("+
                "id VARCHAR(255) PRIMARY KEY,"+
                "language_id VARCHAR(255) NOT NULL,"+
                "selected_languages TEXT,"+
                "user_id VARCHAR(255) NOT NULL,"+
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"+
                "FOREIGN KEY(language_id) REFERENCES languages(id)"+
                ");";
        conn.createStatement().execute(sql);
    }

    private void createSystemComponentsTable(Connection conn) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS translations (" +
        "translation_key VARCHAR(100) NOT NULL,"+
        "locale          VARCHAR(10)  NOT NULL,"+
        "text            TEXT         NOT NULL,"+
        "PRIMARY KEY (translation_key, locale));";

        conn.createStatement().execute(sql);
    }

    /**
     * Insert initial data into the database
     */
    private void insertInitialData(Connection conn) throws SQLException {

    }

}
