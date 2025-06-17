package com.final_app.viewmodels;

import com.final_app.models.ConversationChain;
import de.saxsys.mvvmfx.ViewModel;

public class ConversationChainViewModel implements ViewModel {
    private ConversationChain conversationChain = new ConversationChain();

    public void setConversationChain(ConversationChain conversationChain){this.conversationChain = conversationChain;}
    public ConversationChain getConversationChain(){return conversationChain;}
}
