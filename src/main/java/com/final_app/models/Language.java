package com.final_app.models;

import java.util.Date;

public class Language {
    private String id;
    private String systemId;
    private String name;
    private String iso;
    private String color;
    private long maxXp;
    private Date lastUpdate;

    private LanguageLevelSystem languageLevelSystem;

    // Default constructor
    public Language() {
    }

    // Constructor with id
    public Language(String id, String systemId, String name, String iso, String color, long maxXp) {
        this.id = id;
        this.systemId = systemId;
        this.name = name;
        this.iso = iso;
        this.color = color;
        this.maxXp = maxXp;
    }

    // Constructor without id (for insertion)
    public Language(String systemId, String name, String iso, String color, long maxXp) {
        this.systemId = systemId;
        this.name = name;
        this.iso = iso;
        this.color = color;
        this.maxXp = maxXp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSystemId(){return systemId;}
    public void setSystemId(String id){this.systemId = id;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIso(){
        return iso;
    }
    public void setIso(String iso){
        this.iso = iso;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getMaxXp() {
        return maxXp;
    }

    public void setMaxXp(long maxXp) {
        this.maxXp = maxXp;
    }

    public void setLanguageLevelSystem(LanguageLevelSystem languageLevelSystem){ this.languageLevelSystem = languageLevelSystem;}
    public LanguageLevelSystem getLanguageLevelSystem(){return languageLevelSystem;}

    @Override
    public String toString() {
        return "Language{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", maxXp=" + maxXp +
                '}';
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Language)) return false;
        Language that = (Language) o;
        return iso.equalsIgnoreCase(that.iso);
    }

    @Override
    public int hashCode() {
        return iso.toLowerCase().hashCode();
    }
}
