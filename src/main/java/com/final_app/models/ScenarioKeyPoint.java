package com.final_app.models;

import java.util.Date;

public class ScenarioKeyPoint{
    private String id;
    private String scenarioId;
    private String keyPoint;
    private Date lastUpdate;

    // Default constructor
    public ScenarioKeyPoint() {
    }

    // Constructor with id
    public ScenarioKeyPoint(String id, String scenarioId, String keyPoint) {
        this.id = id;
        this.scenarioId = scenarioId;
        this.keyPoint = keyPoint;
    }

    // Constructor without id (for insertion)
    public ScenarioKeyPoint(String scenarioId, String keyPoint) {
        this.scenarioId = scenarioId;
        this.keyPoint = keyPoint;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getKeyPoint() {
        return keyPoint;
    }

    public void setKeyPoint(String keyPoint) {
        this.keyPoint = keyPoint;
    }

    @Override
    public String toString() {
        return "ScenarioKeyPoint{" +
                "id=" + id +
                ", scenarioId=" + scenarioId +
                ", keyPoint='" + keyPoint + '\'' +
                '}';
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
