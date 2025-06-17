package com.final_app.events;

import com.final_app.services.UserService.XpTransaction;
import javafx.event.Event;
import javafx.event.EventType;

/**
 * Event fired when XP is earned
 */
public class XpEarnedEvent extends Event {
    public static final EventType<XpEarnedEvent> ANY =
            new EventType<>(Event.ANY, "XP_EARNED_EVENT");

    private final XpTransaction transaction;

    public XpEarnedEvent(XpTransaction transaction) {
        super(ANY);
        this.transaction = transaction;
    }

    public XpTransaction getTransaction() {
        return transaction;
    }
}
