package com.final_app.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LanguageLevelSystem {
    private String id;
    private String description;
    private String name;
    private Date lastUpdate;

    private List<LanguageLevel> levels = new ArrayList<>();

    public LanguageLevelSystem(){}
    public LanguageLevelSystem(String id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }
    public LanguageLevelSystem(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public void setLevels(List<LanguageLevel> levels){
        this.levels = levels;
    }
    public List<LanguageLevel> getLevels(){
        return this.levels;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
