package com.final_app.models;

import java.util.Date;

public class UserStats {
    private String id;
    private String userId;
    private int level;
    private long totalXp;
    private int streak;
    private Date lastUpdate;

    // Default constructor
    public UserStats() {
    }

    // Constructor with id
    public UserStats(String id, String userId, int level, long totalXp, int streak) {
        this.id = id;
        this.userId = userId;
        this.level = level;
        this.totalXp = totalXp;
        this.streak = streak;
    }

    // Constructor without id (for insertion)
    public UserStats(String userId, int level, long totalXp, int streak) {
        this.userId = userId;
        this.level = level;
        this.totalXp = totalXp;
        this.streak = streak;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(long totalXp) {
        this.totalXp = totalXp;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    @Override
    public String toString() {
        return "UserStats{" +
                "id=" + id +
                ", userId=" + userId +
                ", level=" + level +
                ", totalXp=" + totalXp +
                ", streak=" + streak +
                '}';
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}