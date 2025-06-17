package com.final_app.models;

import com.final_app.globals.ConversationStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserConversation {
    private String id;
    private String userId;
    private String conversationId;
    private String status; // Stored as string in DB

    // Referenced objects
    private User user;
    private Conversation conversation;
    private List<Message> messages;
    private Evaluation evaluation;

    // Timestamps
    private Date createdAt;
    private Date updatedAt;
    private Date completedAt;

    // Default constructor
    public UserConversation() {
        this.messages = new ArrayList<>();
        this.createdAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
        this.updatedAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    // Constructor with id
    public UserConversation(String id, String userId, String conversationId, String status) {
        this.id = id;
        this.userId = userId;
        this.conversationId = conversationId;
        this.status = status;
        this.messages = new ArrayList<>();
        this.createdAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
        this.updatedAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    // Constructor without id (for insertion)
    public UserConversation(String userId, String conversationId, String status) {
        this.userId = userId;
        this.conversationId = conversationId;
        this.status = status;
        this.messages = new ArrayList<>();
        this.createdAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
        this.updatedAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    // Constructor with ConversationStatus enum
    public UserConversation(String id, String userId, String conversationId, ConversationStatus status) {
        this.id = id;
        this.userId = userId;
        this.conversationId = conversationId;
        this.status = status.name();
        this.messages = new ArrayList<>();
        this.createdAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
        this.updatedAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    // Constructor with objects (for use in code)
    public UserConversation(String id, User user, Conversation conversation, ConversationStatus status) {
        this.id = id;
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
        this.conversation = conversation;
        if (conversation != null) {
            this.conversationId = conversation.getId();
        }
        this.status = status.name();
        this.messages = new ArrayList<>();
        this.createdAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
        this.updatedAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
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

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void updateStatus(ConversationStatus newStatus) {
        this.status = newStatus.name();
        this.updatedAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());

        if (newStatus == ConversationStatus.COMPLETED) {
            this.completedAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
    }

    // Helper methods for enum
    public ConversationStatus getStatusEnum() {
        try {
            return ConversationStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setStatusEnum(ConversationStatus status) {
        if (status != null) {
            this.status = status.name();
        }
    }

    // Object reference getters and setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
        if (conversation != null) {
            this.conversationId = conversation.getId();
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

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public Date getCreatedAt(){
        return createdAt;
    }
    public Date getUpdatedAt(){
        return updatedAt;
    }
    public Date getCompletedAt(){
        return completedAt;
    }
    public void setCreatedAt(Date createdAt){
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(Date updatedAt){
        this.updatedAt = updatedAt;
    }
    public void setCompletedAt(Date completedAt){
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "UserConversation{" +
                "id=" + id +
                ", userId=" + userId +
                ", conversationId=" + conversationId +
                ", status='" + status + '\'' +
                '}';
    }
}
