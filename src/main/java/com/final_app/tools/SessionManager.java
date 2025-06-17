package com.final_app.tools;

import com.final_app.services.AppService;

import java.util.prefs.Preferences;

/**
 * Manages user session information for persistence between application restarts
 */
public class SessionManager {
    private static final String PREF_EMAIL = "user_email";
    private static final String PREF_REMEMBER_ME = "remember_me";
    private static final String PREF_SESSION_TOKEN = "session_token";

    private static final SessionManager instance = new SessionManager();
    private final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);

    private SessionManager() {
        // Private constructor for singleton
    }

    public static SessionManager getInstance() {
        return instance;
    }

    /**
     * Save user session information
     */
    public void saveSession(String email, boolean rememberMe) {
        if (rememberMe) {
            prefs.put(PREF_EMAIL, email);
            prefs.putBoolean(PREF_REMEMBER_ME, true);

            // In a real app, you'd generate and store a secure token
            // This is a simple placeholder
            String sessionToken = generateSessionToken(email);
            prefs.put(PREF_SESSION_TOKEN, sessionToken);
        } else {
            clearSession();
        }
    }

    /**
     * Check if a saved session exists
     */
    public boolean hasSavedSession() {
        return prefs.getBoolean(PREF_REMEMBER_ME, false) &&
                !prefs.get(PREF_EMAIL, "").isEmpty() &&
                !prefs.get(PREF_SESSION_TOKEN, "").isEmpty();
    }

    /**
     * Try to restore a saved session
     * Returns true if successful
     */
    public boolean restoreSession() {
        if (!hasSavedSession()) {
            return false;
        }

        try {
            String email = prefs.get(PREF_EMAIL, "");
            // In a real app, you'd verify the token server-side
            // Here we just try to log in with stored credentials

            // This is just a placeholder. In a real app, you would use the token
            // to authenticate rather than asking for the password again.
            // For simplicity, we return false here to force manual login.
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clear the saved session
     */
    public void clearSession() {
        prefs.remove(PREF_EMAIL);
        prefs.remove(PREF_REMEMBER_ME);
        prefs.remove(PREF_SESSION_TOKEN);
    }

    /**
     * Get the saved email if any
     */
    public String getSavedEmail() {
        return prefs.get(PREF_EMAIL, "");
    }

    /**
     * Check if "remember me" is enabled
     */
    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(PREF_REMEMBER_ME, false);
    }

    /**
     * Generate a simple session token (not secure, just for demonstration)
     */
    private String generateSessionToken(String email) {
        // In a real app, you'd use a secure token generator
        return email.hashCode() + "-" + System.currentTimeMillis();
    }
}
