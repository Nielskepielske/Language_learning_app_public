package com.final_app.globals;

public enum ConversationStatus {
    NOTSTARTED("Not started"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed")
    ;

    private String text;

    public String getText(){
        return text;
    }
    ConversationStatus(String text){
        this.text = text;
    }
}
