package com.final_app.models;

import com.final_app.globals.Roles;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Scenario {
    private String id;
    private String description;
    private String role; // Stored as string in DB
    private List<String> keyPoints;
    private Date lastUpdate;

    // Default constructor
    public Scenario() {
        this.keyPoints = new ArrayList<>();
    }

    // Constructor with id
    public Scenario(String id, String description, String role) {
        this.id = id;
        this.description = description;
        this.role = role;
        this.keyPoints = new ArrayList<>();
    }

    // Constructor without id (for insertion)
    public Scenario(String description, String role) {
        this.description = description;
        this.role = role;
        this.keyPoints = new ArrayList<>();
    }

    // Constructor with all fields
    public Scenario(String id, String description, String role, List<String> keyPoints) {
        this.id = id;
        this.description = description;
        this.role = role;
        this.keyPoints = keyPoints;
    }

    // Additional constructor for existing code compatibility
    public Scenario(String description, ArrayList<String> keyPoints, Roles role) {
        this.description = description;
        this.keyPoints = keyPoints;
        this.role = role.name();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Helper methods for working with Roles enum
    public Roles getRoleEnum() {
        try {
            return Roles.valueOf(role);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setRoleEnum(Roles role) {
        if (role != null) {
            this.role = role.name();
        }
    }

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> keyPoints) {
        this.keyPoints = keyPoints;
    }

    public void addKeyPoint(String keyPoint) {
        if (this.keyPoints == null) {
            this.keyPoints = new ArrayList<>();
        }
        this.keyPoints.add(keyPoint);
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", role='" + role + '\'' +
                ", keyPoints=" + keyPoints +
                '}';
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
