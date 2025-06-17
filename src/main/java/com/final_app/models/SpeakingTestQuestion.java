package com.final_app.models;

import java.util.Date;
import java.util.List;

public class SpeakingTestQuestion {
    private String id;
    private String testId;
    private String questionText;
    private String expectedResponsePattern; // Grammar pattern to check for
    private String expectedResponseLanguageIso;
    private List<String> requiredVocabulary; // Words that should be used
    private int difficultyLevel;
    private int orderIndex;
    private Date lastUpdate;

    public SpeakingTestQuestion(){

    }

    public SpeakingTestQuestion(String testId, String questionText, String expectedResponsePattern, String expectedResponseLanguageIso, List<String> requiredVocabulary, int difficultyLevel, int orderIndex){
        this.testId = testId;
        this.questionText = questionText;
        this.expectedResponsePattern = expectedResponsePattern;
        this.expectedResponseLanguageIso = expectedResponseLanguageIso;
        this.requiredVocabulary = requiredVocabulary;
        this.difficultyLevel = difficultyLevel;
        this.orderIndex = orderIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getExpectedResponsePattern() {
        return expectedResponsePattern;
    }

    public void setExpectedResponsePattern(String expectedResponsePattern) {
        this.expectedResponsePattern = expectedResponsePattern;
    }

    public List<String> getRequiredVocabulary() {
        return requiredVocabulary;
    }

    public void setRequiredVocabulary(List<String> requiredVocabulary) {
        this.requiredVocabulary = requiredVocabulary;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public String toString(){
        return "Question: " + questionText + "\n"+
                "Expected response: " + expectedResponsePattern;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getExpectedResponseLanguageIso() {
        return expectedResponseLanguageIso;
    }

    public void setExpectedResponseLanguageIso(String expectedResponseLanguageIso) {
        this.expectedResponseLanguageIso = expectedResponseLanguageIso;
    }
}