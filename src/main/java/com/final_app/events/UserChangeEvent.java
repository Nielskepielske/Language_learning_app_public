package com.final_app.events;

import com.final_app.models.User;
import javafx.event.Event;
import javafx.event.EventType;

/**
 * Event fired when the current user changes (login, logout, etc.)
 */
public class UserChangeEvent extends Event {
    public static final EventType<UserChangeEvent> ANY =
            new EventType<>(Event.ANY, "USER_CHANGE_EVENT");

    public static final EventType<UserChangeEvent> LOGIN =
            new EventType<>(ANY, "USER_LOGIN");

    public static final EventType<UserChangeEvent> LOGOUT =
            new EventType<>(ANY, "USER_LOGOUT");

    private final User user;
    private final boolean isLogout;

    public UserChangeEvent(User user, EventType<UserChangeEvent> eventType) {
        super(eventType);
        this.user = user;
        this.isLogout = (eventType == LOGOUT);
    }

    public User getUser() {
        return user;
    }

    public boolean isLogout() {
        return isLogout;
    }
}
