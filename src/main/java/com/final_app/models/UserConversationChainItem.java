package com.final_app.models;

import java.util.Date;
import java.util.List;

public class UserConversationChainItem {
    private String id;
    private String userConversationId;
    private String conversationChainId;
    private Date lastUpdate;

    private UserConversation userConversation;
    private ConversationChain conversationChain;

    public UserConversationChainItem(){}
    public UserConversationChainItem(String userConversationId, String conversationChainId){
        this.userConversationId = userConversationId;
        this.conversationChainId = conversationChainId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getConversationChainId() {
        return conversationChainId;
    }
    public void setConversationChainId(String conversationChainId) {
        this.conversationChainId = conversationChainId;
    }

    public String getUserConversationId() {
        return userConversationId;
    }
    public void setUserConversationId(String userConversationId) {
        this.userConversationId = userConversationId;
    }

    public ConversationChain getConversationChain() {
        return conversationChain;
    }
    public void setConversationChain(ConversationChain conversationChain) {
        this.conversationChain = conversationChain;
    }

    public UserConversation getUserConversation() {
        return userConversation;
    }

    public void setUserConversation(UserConversation userConversation) {
        this.userConversation = userConversation;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
