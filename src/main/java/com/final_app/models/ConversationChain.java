package com.final_app.models;

import java.util.Date;
import java.util.List;

public class ConversationChain {
    private String id;
    private String languageId;
    private String languageFromId;
    private String levelId;
    private String title;
    private String description;
    private Date lastUpdate;

    private Language language;
    private Language languageFrom;
    private LanguageLevel languageLevel;
    private List<ConversationChainItem> conversations;

    public ConversationChain(){}
    public ConversationChain(String languageId, String levelId, String title, String description){
        this.languageId = languageId;
        this.levelId = levelId;
        this.title = title;
        this.description = description;
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

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getLevelId() {
        return levelId;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public Language getLanguage(){return language;}
    public void setLanguage(Language language){
        if(language != null) this.languageId = language.getId();
        this.language = language;
    }

    public LanguageLevel getLanguageLevel(){return languageLevel;}
    public void setLanguageLevel(LanguageLevel languageLevel){
        if(languageLevel != null) this.levelId = languageLevel.getId();
        this.languageLevel = languageLevel;
    }

    public List<ConversationChainItem> getConversations(){
        return conversations;
    }
    public void setConversations(List<ConversationChainItem> conversations){
        this.conversations = conversations;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLanguageFromId() {
        return languageFromId;
    }

    public void setLanguageFromId(String languageFromId) {
        this.languageFromId = languageFromId;
    }

    public Language getLanguageFrom() {
        return languageFrom;
    }

    public void setLanguageFrom(Language languageFrom) {
        this.languageFrom = languageFrom;
        if(languageFrom != null) this.languageFromId = languageFrom.getId();
    }
}
