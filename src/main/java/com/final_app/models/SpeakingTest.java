package com.final_app.models;

import java.util.Date;
import java.util.List;

public class SpeakingTest {
    private String id;
    private String title;
    private String description;
    private String explanation;
    private String languageId;
    private String languageFromId; // Language of creation
    private String levelId;
    private String grammarFocus; // e.g., "Past Tense", "Modal Verbs"
    private String vocabularyTheme; // e.g., "Travel", "Workplace"
    private Date lastUpdate;

    private int maxScore;

    private Language language;
    private Language languageFrom;
    private LanguageLevel languageLevel;

    private List<SpeakingTestQuestion> questions;

    public SpeakingTest(){}

    public SpeakingTest(String title, String description, String explanation, String languageId, String levelId, String grammarFocus, String vocabularyTheme){
        this.title = title;
        this.description = description;
        this.explanation = explanation;
        this.languageId = languageId;
        this.levelId = levelId;
        this.grammarFocus = grammarFocus;
        this.vocabularyTheme = vocabularyTheme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguageId() {
        return languageId;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getGrammarFocus() {
        return grammarFocus;
    }

    public void setGrammarFocus(String grammarFocus) {
        this.grammarFocus = grammarFocus;
    }

    public String getVocabularyTheme() {
        return vocabularyTheme;
    }

    public void setVocabularyTheme(String vocabularyTheme) {
        this.vocabularyTheme = vocabularyTheme;
    }

    public List<SpeakingTestQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<SpeakingTestQuestion> questions) {
        this.questions = questions;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public LanguageLevel getLanguageLevel() {
        return languageLevel;
    }

    public void setLanguageLevel(LanguageLevel languageLevel) {
        this.languageLevel = languageLevel;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @Override
    public String toString(){
        return "Title: " + title + "\n" +
                "Description: " + description;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public String getLanguageFromId() {
        return languageFromId;
    }

    public void setLanguageFromId(String languageFromId) {
        this.languageFromId = languageFromId;
    }

    public Language getLanguageFrom() {
        return languageFrom;
    }

    public void setLanguageFrom(Language languageFrom) {
        this.languageFrom = languageFrom;
        if(languageFrom != null) this.languageFromId = languageFrom.getId();
    }
}
