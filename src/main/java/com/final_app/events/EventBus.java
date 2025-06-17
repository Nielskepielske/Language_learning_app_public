package com.final_app.events;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Node;

// Simple event bus implementation
public class EventBus {
    private static final EventBus instance = new EventBus();

    private final Node eventNode = new Group(); // Hidden node to fire events on

    private EventBus() {
        // Private constructor for singleton
    }

    public static EventBus getInstance() {
        return instance;
    }

    public <T extends Event> void subscribe(EventType<T> eventType, EventHandler<T> handler) {
        eventNode.addEventHandler(eventType, handler);
    }

    public void post(Event event) {
        eventNode.fireEvent(event);
    }
}
