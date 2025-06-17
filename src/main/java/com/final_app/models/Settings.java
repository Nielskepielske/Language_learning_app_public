package com.final_app.models;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Settings {
    private String id;
    private String languageId;
    private String selectedLanguages;
    private String userId;
    private Date lastUpdate;

    private Language language;

    public Settings() {}
    public Settings(String id, String languageId, String userId) {
        this.id = id;
        this.languageId = languageId;
        this.userId = userId;
    }
    public Settings(String id, String languageId, String userId, Language language) {
        this.id = id;
        this.languageId = languageId;
        this.userId = userId;
        this.language = language;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getLanguageId() {
        return languageId;
    }
    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Language getLanguage() {
        return language;
    }
    public void setLanguage(Language language) {
        if(language != null) {
            this.languageId = language.getId();
        }
        this.language = language;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<String> getSelectedLanguages() {
        return Arrays.stream(selectedLanguages.split(",")).toList();
    }

    public void setSelectedLanguages(List<String> selectedLanguages) {
        this.selectedLanguages = String.join(",", selectedLanguages);
    }
    public void setSelectedLanguages(String selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }
}
