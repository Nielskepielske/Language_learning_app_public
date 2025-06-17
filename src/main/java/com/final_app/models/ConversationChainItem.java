package com.final_app.models;

import java.util.Date;

public class ConversationChainItem {
    private String id;
    private String conversationChainId;
    private String conversationId;
    private int conversationIndex; // order
    private Date lastUpdate;

    private Conversation conversation;

    public ConversationChainItem(){}
    public ConversationChainItem(String conversationChainId, String conversationId, int conversationIndex){
        this.conversationId = conversationId;
        this.conversationChainId = conversationChainId;
        this.conversationIndex = conversationIndex;
    }

    public String getId(){return id;}
    public void setId(String id){this.id = id;}

    public String getConversationChainId(){return conversationChainId;}
    public void setConversationChainId(String conversationChainId){this.conversationChainId = conversationChainId;}

    public String getConversationId(){return conversationId;}
    public void setConversationId(String conversationId){this.conversationId = conversationId;}

    public int getConversationIndex(){return conversationIndex;}
    public void setConversationIndex(int conversationIndex){this.conversationIndex = conversationIndex;}

    public Conversation getConversation(){return conversation;}
    public void setConversation(Conversation conversation){
        this.conversationId = conversation.getId();
        this.conversation = conversation;
    }


    @Override
    public String toString(){
        return "Conversation: "
                + "\n Title: " + conversation.getTitle()
                + "\n Description: " + conversation.getDescription();
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
