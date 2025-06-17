package com.final_app.models;

import java.util.Date;

public class UserLanguage {
    private String id;
    private String userId;
    private String languageId;
    private String levelId;
    private long xp;
    private Date lastUpdate;

    // For convenience, these fields won't be stored in the db but can be loaded
    private Language language;
    private LanguageLevel level;

    // Default constructor
    public UserLanguage() {
    }

    // Constructor with id
    public UserLanguage(String id, String userId, String languageId, String levelId, long xp) {
        this.id = id;
        this.userId = userId;
        this.languageId = languageId;
        this.levelId = levelId;
        this.xp = xp;
    }

    // Constructor without id (for insertion)
    public UserLanguage(String userId, String languageId, String levelId, long xp) {
        this.userId = userId;
        this.languageId = languageId;
        this.levelId = levelId;
        this.xp = xp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLanguageId() {
        return languageId;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
        if (language != null) {
            this.languageId = language.getId();
        }
    }

    public LanguageLevel getLevel() {
        return level;
    }

    public void setLevel(LanguageLevel level) {
        this.level = level;
        if (level != null) {
            this.levelId = level.getId();
        }
    }

    @Override
    public String toString() {
        return "UserLanguage{" +
                "id=" + id +
                ", userId=" + userId +
                ", languageId=" + languageId +
                ", levelId=" + levelId +
                ", xp=" + xp +
                '}';
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
