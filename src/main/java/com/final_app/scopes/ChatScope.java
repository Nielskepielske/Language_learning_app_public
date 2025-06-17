package com.final_app.scopes;

import com.final_app.models.Message;
import de.saxsys.mvvmfx.Scope;
import de.saxsys.mvvmfx.ScopeProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@ScopeProvider(com.final_app.scopes.ChatScope.class)
public class ChatScope implements Scope {
    private final ObservableList<Message> sharedList = FXCollections.observableArrayList();

    public ObservableList<Message> getSharedList(){
        return sharedList;
    }
}
