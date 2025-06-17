package com.final_app.models;

import java.util.Date;

public class LanguageLevel {
    private String id;
    private String systemId;
    private String name;
    private int value;
    private Date lastUpdate;

    // Default constructor
    public LanguageLevel() {
    }

    // Constructor with id
    public LanguageLevel(String id, String systemId, String name, int value) {
        this.id = id;
        this.systemId = systemId;
        this.name = name;
        this.value = value;
    }

    // Constructor without id (for insertion)
    public LanguageLevel(String systemId, String name, int value) {
        this.systemId = systemId;
        this.name = name;
        this.value = value;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSystemId(){
        return systemId;
    }
    public void setSystemId(String id){
        this.systemId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LanguageLevel{" +
                "id=" + id +
                ", systemId='" + systemId + "\''" +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getLevelThreshold(){
        final int FIRST = 1;
        final int STEP = 5;
        return FIRST + (STEP * (value - 1));
    }
}
