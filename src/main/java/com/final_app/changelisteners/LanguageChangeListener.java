package com.final_app.changelisteners;

import com.final_app.models.Language;

/**
 * Listener for language change events.
 */
@FunctionalInterface
public interface LanguageChangeListener {
    void onLanguageChanged(Language newLanguage);
}
