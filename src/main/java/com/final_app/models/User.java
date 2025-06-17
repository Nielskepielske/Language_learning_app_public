package com.final_app.models;

import java.util.Date;

public class User {
    private String id;
    private String userName;
    private String email;
    private String password;
    private String photoPath;
    private Date lastUpdate;

    // Default constructor
    public User() {
    }

    // Constructor with id
    public User(String id, String userName, String email, String password, String photoPath) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.photoPath = photoPath;
    }

    // Constructor without id (for insertion)
    public User(String userName, String email, String password, String photoPath) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.photoPath = photoPath;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", photoPath='" + photoPath + '\'' +
                '}';
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
