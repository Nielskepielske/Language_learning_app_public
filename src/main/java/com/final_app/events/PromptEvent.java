package com.final_app.events;

import javafx.event.Event;
import javafx.event.EventType;

public class PromptEvent extends Event {
    public static final EventType<PromptEvent> ANY =
            new EventType<>(Event.ANY, "PROMPT_EVENT");

    private String message;
    public String getMessage(){
        return this.message;
    }
    public PromptEvent(String message) {
        super(ANY);
        this.message = message;
    }
}
