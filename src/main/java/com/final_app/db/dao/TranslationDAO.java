package com.final_app.db.dao;

import com.final_app.models.Language;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO for loading and updating translations in an H2 database.
 */
public class TranslationDAO {
    private final DataSource dataSource;
    private static final String SQL_LOAD =
            "SELECT translation_key, text FROM translations WHERE locale = ?";
    private static final String SQL_UPSERT =
            "MERGE INTO translations (translation_key, locale, text) KEY(translation_key, locale) VALUES (?, ?, ?);";

    public TranslationDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Load all translations for the given language.
     */
    public Map<String, String> loadTranslations(Language language) throws Exception {
        Map<String, String> map = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_LOAD)) {
            ps.setString(1, language.getIso());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("translation_key"), rs.getString("text"));
                }
            }
        }
        return map;
    }

    /**
     * Add or update a single translation.
     */
    public void addOrUpdateTranslation(Language language, String key, String text) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPSERT)) {
            ps.setString(1, key);
            ps.setString(2, language.getIso());
            ps.setString(3, text);
            ps.executeUpdate();
        }
    }
}
