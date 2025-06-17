package com.final_app.models;

public class Evaluation {
    private String id;
    private String userConversationId;
    private int score;
    private int maxScore;
    private int vocab;
    private int grammar;
    private int correctness;
    private int duration;
    private int purpose;
    private String feedback;

    private int maxPointsPerCriteria = 10;

    public int maxPointsPerCriteria(){return maxPointsPerCriteria;}
    public void setMaxPointsPerCriteria(int maxPointsPerCriteria) { this.maxPointsPerCriteria = maxPointsPerCriteria; }

    // Default constructor
    public Evaluation() {
    }

    // Constructor with id
    public Evaluation(String id, String userConversationId, int score, int maxScore, int vocab, int grammar, String feedback) {
        this.id = id;
        this.userConversationId = userConversationId;
        this.score = score;
        this.maxScore = maxScore;
        this.vocab = vocab;
        this.grammar = grammar;
        this.feedback = feedback;
    }

    // Constructor without id (for insertion)
    public Evaluation(String userConversationId, int score, int maxScore, int vocab, int grammar, String feedback) {
        this.userConversationId = userConversationId;
        this.score = score;
        this.maxScore = maxScore;
        this.vocab = vocab;
        this.grammar = grammar;
        this.feedback = feedback;
    }

    public Evaluation(String userConversationId, int score, int maxScore, int vocab, int grammar, String feedback, int correctness, int duration, int purpose) {
        this.userConversationId = userConversationId;
        this.score = score;
        this.maxScore = maxScore;
        this.vocab = vocab;
        this.grammar = grammar;
        this.feedback = feedback;
        this.correctness = correctness;
        this.duration = duration;
        this.purpose = purpose;
    }

    // Constructor for compatibility with existing code
    public Evaluation(int score, int maxScore, int vocab, int grammar, String feedback) {
        this.score = score;
        this.maxScore = maxScore;
        this.vocab = vocab;
        this.grammar = grammar;
        this.feedback = feedback;
    }

    public Evaluation(int score, int maxScore, int vocab, int grammar, String feedback, int correctness, int duration, int purpose) {
        this.score = score;
        this.maxScore = maxScore;
        this.vocab = vocab;
        this.grammar = grammar;
        this.feedback = feedback;
        this.correctness = correctness;
        this.duration = duration;
        this.purpose = purpose;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserConversationId() {
        return userConversationId;
    }

    public void setUserConversationId(String userConversationId) {
        this.userConversationId = userConversationId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public int getVocab() {
        return vocab;
    }

    public void setVocab(int vocab) {
        this.vocab = vocab;
    }

    public int getGrammar() {
        return grammar;
    }

    public void setGrammar(int grammar) {
        this.grammar = grammar;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", userConversationId=" + userConversationId +
                ", score=" + score +
                ", maxScore=" + maxScore +
                ", vocab=" + vocab +
                ", grammar=" + grammar +
                ", feedback='" + feedback + '\'' +
                '}';
    }

    public int getCorrectness() {
        return correctness;
    }

    public void setCorrectness(int correctness) {
        this.correctness = correctness;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPurpose() {
        return purpose;
    }

    public void setPurpose(int purpose) {
        this.purpose = purpose;
    }
}
