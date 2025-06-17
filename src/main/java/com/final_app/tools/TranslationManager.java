package com.final_app.tools;

import com.final_app.changelisteners.LanguageChangeListener;
import com.final_app.db.dao.TranslationDAO;
import com.final_app.globals.TKey;
import com.final_app.models.Language;
import com.final_app.services.ChatGPTService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages translations in-memory with language switching, listeners, and dynamic updates.
 */
public class TranslationManager {
    private static volatile TranslationManager instance;

    private final TranslationDAO dao;
    private final Map<Language, Map<String, StringProperty>> cache = new ConcurrentHashMap<>();
    private final List<LanguageChangeListener> listeners = new CopyOnWriteArrayList<>();
    private Language currentLanguage;
    private Map<String, StringProperty> translations;
    private Map<String, String> defaultTranslations;

    private Language defaultLanguage = new Language(null, "English", "en", null, 0);

    private TranslationManager(DataSource ds, Language initialLanguage) throws Exception {
        this.dao = new TranslationDAO(ds);
        setLanguage(initialLanguage);
        defaultTranslations = new HashMap<>();
        for (TKey key : TKey.values()) {
            defaultTranslations.put(key.name(), dao.loadTranslations(initialLanguage).getOrDefault(key.name(), key.name()));
        }
    }

    /**
     * Initialize the singleton with a DataSource and starting language.
     */
    public static synchronized void init(DataSource ds, Language language) throws Exception {
        if (instance == null) {
            instance = new TranslationManager(ds, language);
        }
    }

    /**
     * Get the singleton instance.
     */
    public static TranslationManager get() {
        if (instance == null) {
            throw new IllegalStateException("TranslationManager not initialized. Call init() first.");
        }
        return instance;
    }

    public void updateDefaultTranslations(){
            try {
                Map<String, String> tempMap = dao.loadTranslations(defaultLanguage);
                for(TKey key : TKey.values()) {
                    defaultTranslations.put(key.name(), tempMap.getOrDefault(key.name(), key.name()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    /**
     * Register a listener to be notified when the language changes.
     */
    public void addLanguageChangeListener(LanguageChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister a previously added listener.
     */
    public void removeLanguageChangeListener(LanguageChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Change the current language and reload (or fetch from cache) its translations.
     * Notifies all registered listeners after loading.
     */
    public synchronized void setLanguage(Language language) throws Exception {
        if (!language.equals(this.currentLanguage)) {
            this.currentLanguage = language;

            Map<String, String> dbTranslations= dao.loadTranslations(language);


            translations = translations == null ? new HashMap<>() : translations;

            Map<String, String> tempTranslations = new HashMap<>();

            if(defaultTranslations != null){
                for(TKey key : TKey.values()){
                    if(!translations.containsKey(key.name())){
                       translations.put(key.name(), new SimpleStringProperty(defaultTranslations.get(key.name())));
                    }
                    if(dbTranslations.containsKey(key.name()) && !dbTranslations.get(key.name()).equals(key.name())){
                        Platform.runLater(()->{
                            translations.get(key.name()).set(dbTranslations.get(key.name()));
                        });
                        continue;
                    }else{
                        generateTranslation(key.name(), defaultTranslations.get(key.name()))
                                .thenAccept(result -> {
                                    result.ifPresent(translation -> {
                                        Platform.runLater(()->{
                                            if(translations.containsKey(key.name())){
                                                translations.get(key.name()).set(translation);
                                            }else{
                                                translations.put(key.name(), new SimpleStringProperty(translation));
                                            }
                                        });
                                    });

                                });
                    }
                }
            }

            // Notify listeners
            listeners.forEach(l -> l.onLanguageChanged(language));
        }
    }
    private CompletableFuture<Optional<String>> generateTranslation(String key, String text) {
        return CompletableFuture.supplyAsync(()->{
            try {
                if(text != null){
                    Optional<String> translation = ChatGPTService.translateMessage(text, "English", currentLanguage.getName());
                    translation.ifPresentOrElse(t -> {
                        try {
                            dao.addOrUpdateTranslation(currentLanguage, key, t);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, ()->{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Translation Failed");
                        alert.setContentText("Error encountered while generating translation");
                    });
                    return translation;
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Retrieve the translated string for a key, formatting any arguments.
     */
    public StringProperty t(TKey key, Object... args) {
        if(translations.get(key.name()) == null){
//            generateTranslation(key.name(), defaultTranslations.get(key.name()))
//                    .thenAccept(result -> {
//                        Platform.runLater(() -> {
//                            translations.get(key.name()).set(result);
//                        });
//                    });
            translations.put(key.name(), new SimpleStringProperty(defaultTranslations.get(key.name())));
        }

        return translations.get(key.name());
    }

    /**
     * Add or update a translation dynamically.
     */
    public void addTranslation(Language language, TKey key, String text) throws Exception {
        dao.addOrUpdateTranslation(language, key.name(), text);
        cache.remove(language);
        if (language.equals(this.currentLanguage)) {
            setLanguage(language);
        }
    }
}

