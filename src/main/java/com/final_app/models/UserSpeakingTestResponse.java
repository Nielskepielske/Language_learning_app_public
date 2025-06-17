package com.final_app.models;

import com.final_app.globals.Sender;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Stores a user's response to a specific question in a speaking test,
 * along with evaluation metrics and feedback.
 */
public class UserSpeakingTestResponse {
    private String id;
    private String userSpeakingTestId;
    private String questionId;
    private int questionIndex;
    private String transcribedText;
    private Date respondedAt;
    private Date lastUpdate;

    // Evaluation metrics
    private int grammarScore; // 1-5
    private int vocabularyScore; // 1-5
    private int pronunciationScore; // 1-5
    private int fluencyScore; // 1-5
    private int overallScore; // 1-5

    public int maxScore = 5;

    // Detailed analysis
    private String feedback;
    private Map<String, Boolean> grammarRulesCorrect; // Maps rule name to correct/incorrect
    private Map<String, Boolean> requiredVocabularyUsed; // Maps vocabulary word to used/not used

    // Reference to the question for convenience
    private SpeakingTestQuestion question;

    // Default constructor
    public UserSpeakingTestResponse() {
        this.respondedAt = Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant());
        this.grammarRulesCorrect = new HashMap<>();
        this.requiredVocabularyUsed = new HashMap<>();
    }

    // Constructor with essential fields
    public UserSpeakingTestResponse(String userSpeakingTestId, String questionId,
                                    int questionIndex, String transcribedText) {
        this();
        this.userSpeakingTestId = userSpeakingTestId;
        this.questionId = questionId;
        this.questionIndex = questionIndex;
        this.transcribedText = transcribedText;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getGrammarScore() {
        return grammarScore;
    }

    public void setGrammarScore(int grammarScore) {
        this.grammarScore = grammarScore;
    }

    public String getUserSpeakingTestId() {
        return userSpeakingTestId;
    }

    public void setUserSpeakingTestId(String userSpeakingTestId) {
        this.userSpeakingTestId = userSpeakingTestId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public String getTranscribedText() {
        return transcribedText;
    }

    public void setTranscribedText(String transcribedText) {
        this.transcribedText = transcribedText;
    }

    public Date getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Date respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public SpeakingTestQuestion getQuestion() {
        return question;
    }

    public void setQuestion(SpeakingTestQuestion question) {
        this.question = question;
        if(question != null){
            this.questionId = question.getId();
        }
    }

    public Map<String, Boolean> getGrammarRulesCorrect(){return this.grammarRulesCorrect;}
    public void setGrammarRulesCorrect(Map<String, Boolean> grammarRulesCorrect){this.grammarRulesCorrect = grammarRulesCorrect;}

    public Map<String, Boolean> getRequiredVocabularyUsed(){return requiredVocabularyUsed;}
    public void setRequiredVocabularyUsed(Map<String, Boolean> requiredVocabularyUsed){this.requiredVocabularyUsed = requiredVocabularyUsed;}

    public int getVocabularyScore(){return vocabularyScore;}
    public void setVocabularyScore(int vocabularyScore){this.vocabularyScore = vocabularyScore;}

    public int getOverallScore(){return overallScore;}
    public void setOverallScore(int overallScore){this.overallScore = overallScore;}

    /**
     * Calculate percentage of required vocabulary used correctly
     */
    public double getVocabularyUsagePercentage() {
        if (requiredVocabularyUsed.isEmpty()) return 0.0;

        long usedCount = requiredVocabularyUsed.values().stream()
                .filter(used -> used)
                .count();

        return (double) usedCount / requiredVocabularyUsed.size() * 100.0;
    }

    /**
     * Calculate percentage of grammar rules applied correctly
     */
    public double getGrammarCorrectPercentage() {
        if (grammarRulesCorrect.isEmpty()) return 0.0;

        long correctCount = grammarRulesCorrect.values().stream()
                .filter(correct -> correct)
                .count();

        return (double) correctCount / grammarRulesCorrect.size() * 100.0;
    }

    /**
     * Checks if a specific vocabulary word was used
     */
    public boolean wasVocabularyUsed(String word) {
        return requiredVocabularyUsed.getOrDefault(word.toLowerCase(), false);
    }

    /**
     * Checks if a specific grammar rule was correctly applied
     */
    public boolean wasGrammarRuleCorrect(String rule) {
        return grammarRulesCorrect.getOrDefault(rule, false);
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }


    // Additional methods as needed
}
