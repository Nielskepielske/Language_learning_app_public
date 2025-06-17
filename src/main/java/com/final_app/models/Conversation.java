package com.final_app.models;

import com.final_app.globals.AIModels;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Conversation {
    private String id;
    private String title;
    private String description;
    private String languageId;
    private String languageFromId;
    private String levelId;
    private String scenarioId;
    private String startPrompt;
    private String model;
    private Date lastUpdate;

    // Object references - these aren't directly stored in the db
    private Language language;
    private Language languageFrom;
    private LanguageLevel languageLevel;
    private Scenario scenario;
    private List<Message> messages;

    // Default constructor
    public Conversation() {
        this.messages = new ArrayList<>();
    }

    // Constructor with id
    public Conversation(String id, String title, String description, String languageId,
                        String levelId, String scenarioId, String startPrompt, String model) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.languageId = languageId;
        this.levelId = levelId;
        this.scenarioId = scenarioId;
        this.startPrompt = startPrompt;
        this.model = model;
        this.messages = new ArrayList<>();
    }

    // Constructor for insertion
    public Conversation(String title, String description, String languageId,
                        String levelId, String scenarioId, String startPrompt, String model) {
        this.title = title;
        this.description = description;
        this.languageId = languageId;
        this.levelId = levelId;
        this.scenarioId = scenarioId;
        this.startPrompt = startPrompt;
        this.model = model;
        this.messages = new ArrayList<>();
    }

    // Compatibility constructor with existing code
    public Conversation(String id, String title, String description, Language language,
                        LanguageLevel conversationLevel, Scenario scenario,
                        String startPrompt, AIModels model) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.language = language;
        if (language != null) {
            this.languageId = language.getId();
        }
        this.languageLevel = conversationLevel;
        this.scenario = scenario;
        if (scenario != null) {
            this.scenarioId = scenario.getId();
        }
        this.startPrompt = startPrompt;
        if (model != null) {
            this.model = model.getModel();
        }
        this.messages = new ArrayList<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getStartPrompt() {
        return startPrompt;
    }

    public void setStartPrompt(String startPrompt) {
        this.startPrompt = startPrompt;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    // Object reference getters and setters
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
        if (language != null) {
            this.languageId = language.getId();
        }
    }

    public LanguageLevel getLanguageLevel() {
        return languageLevel;
    }

    public void setLanguageLevel(LanguageLevel languageLevel) {
        if(languageLevel != null){
            setLevelId(languageLevel.getId());
        }
        this.languageLevel = languageLevel;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
        if (scenario != null) {
            this.scenarioId = scenario.getId();
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
    }

    // Helper methods
    public AIModels getAIModel() {
        try {
            for (AIModels aiModel : AIModels.values()) {
                if (aiModel.getModel().equals(this.model)) {
                    return aiModel;
                }
            }
        } catch (Exception e) {
            // Handle any conversion errors
        }
        return null;
    }

    public void setAIModel(AIModels aiModel) {
        if (aiModel != null) {
            this.model = aiModel.getModel();
        }
    }

    @Override
    public String toString() {
        return "Conversation: \n"+
                "Title: " + this.title +
                "\nDescription: " + this.description;
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