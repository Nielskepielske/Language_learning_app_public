package com.final_app.models;

import com.final_app.globals.Sender;
import java.time.LocalDateTime;
import java.util.Date;

public class Message {
    private String id;
    private int index;
    private String userConversationId;
    private String text;
    private String sender; // Stored as string in DB
    private Date timestamp;

    // Default constructor
    public Message() {
        this.timestamp = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    // Constructor with id
    public Message(String id, int index, String userConversationId, String text, String sender, Date timestamp) {
        this.id = id;
        this.index = index;
        this.userConversationId = userConversationId;
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    // Constructor without id (for insertion)
    public Message(int index, String userConversationId, String text, String sender) {
        this.index = index;
        this.userConversationId = userConversationId;
        this.text = text;
        this.sender = sender;
        this.timestamp = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    // Constructor with Sender enum
    public Message(String userConversationId, String text, Sender sender) {
        this.userConversationId = userConversationId;
        this.text = text;
        this.sender = sender.name();
        this.timestamp = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    // Constructor for compatibility with existing code
    public Message(String text, Sender sender) {
        this.text = text;
        this.sender = sender.name();
        this.timestamp = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIndex(){return index;}
    public void setIndex(int index){this.index = index;}

    public String getUserConversationId() {
        return userConversationId;
    }

    public void setUserConversationId(String userConversationId) {
        this.userConversationId = userConversationId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Helper methods for enum
    public Sender getSenderEnum() {
        try {
            return Sender.valueOf(sender);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setSenderEnum(Sender sender) {
        if (sender != null) {
            this.sender = sender.name();
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", userConversationId=" + userConversationId +
                ", text='" + text + '\'' +
                ", sender='" + sender + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
