package com.final_app.factories;

import com.final_app.interfaces.*;
import com.final_app.repositories.firebase.*;
import com.final_app.repositories.local.*;
import io.github.cdimascio.dotenv.Dotenv;

public class RepositoryFactory {
    private State state = Dotenv.load().get("DB_MODE").equals("OFFLINE") ? State.OFFLINE : State.ONLINE;

    private static RepositoryFactory instance;
    public static RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    public enum State {
        OFFLINE,
        ONLINE
    }

    public State getState() {
        return state;
    }

    public void changeToOnline(){
        state = State.ONLINE;
    }
    public void changeToOffline(){
        state = State.OFFLINE;
    }

    public static IConversationRepository getConversationRepository() {
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalConversationRepository.getInstance();
            }
            case ONLINE -> {
                return  FBConversationRepository.getInstance();
            }
            default -> {
                return  LocalConversationRepository.getInstance();
            }
        }
    }
    public static ISpeakingTestRepository getSpeakingTestRepository() {
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalSpeakingTestRepository.getInstance();
            }
            case ONLINE -> {
                return  FBSpeakingTestRepository.getInstance();
            }
            default -> {
                return  LocalSpeakingTestRepository.getInstance();
            }
        }
    }
    public static ILanguageRepository getLanguageRepository() {
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalLanguageRepository.getInstance();
            }
            case ONLINE -> {
                return  FBLanguageRepository.getInstance();
            }
            default -> {
                return  LocalLanguageRepository.getInstance();
            }
        }
    }
    public static IUserRepository getUserRepository() {
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalUserRepository.getInstance();
            }
            case ONLINE -> {
                return  FBUserRepository.getInstance();
            }
            default -> {
                return  LocalUserRepository.getInstance();
            }
        }
    }
    public static IUserConversationsRepository getUserConversationsRepository(){
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalUserConversationRepository.getInstance();
            }
            case ONLINE -> {
                return  FBUserConversationsRepository.getInstance();
            }
            default -> {
                return  LocalUserConversationRepository.getInstance();
            }
        }
    }
    public static IUserSpeakingTestRepository getUserSpeakingTestRepository(){
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalUserSpeakingTestRepository.getInstance();
            }
            case ONLINE -> {
                return  FBUserSpeakingTestRepository.getInstance();
            }
            default -> {
                return  LocalUserSpeakingTestRepository.getInstance();
            }
        }
    }
    public static IScenarioRepository getScenarioRepository(){
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalScenarioRepository.getInstance();
            }
            case ONLINE -> {
                return  FBScenarioRepository.getInstance();
            }
            default -> {
                return  LocalScenarioRepository.getInstance();
            }
        }
    }
    public static IMessageRepository getMessageRepository(){
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalMessageRepository.getInstance();
            }
            case ONLINE -> {
                return  FBMessageRepository.getInstance();
            }
            default -> {
                return  LocalMessageRepository.getInstance();
            }
        }
    }
    public static IUserLanguageRepository getUserLanguageRepository(){
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalUserLanguageRepository.getInstance();
            }
            case ONLINE -> {
                return  FBUserLanguageRepository.getInstance();
            }
            default -> {
                return  LocalUserLanguageRepository.getInstance();
            }
        }
    }
    public static IQuestionRepository getQuestionRepository(){
        switch (getInstance().state) {
            case OFFLINE -> {
                return  LocalQuestionRepository.getInstance();
            }
            case ONLINE -> {
                return  FBQuestionRepository.getInstance();
            }
            default -> {
                return LocalQuestionRepository.getInstance();
            }
        }
    }
    public static IResponseRepository getResponseRepository(){
        switch (getInstance().state) {
            case OFFLINE -> {
                return LocalResponseRepository.getInstance();
            }
            case ONLINE -> {
                return FBResponseRepository.getInstance();
            }
            default -> {
                return LocalResponseRepository.getInstance();
            }
        }
    }
    public static IEvaluationRepository getEvaluationRepository(){
        switch (getInstance().state) {
            case OFFLINE -> {
                return LocalEvaluationRepository.getInstance();
            }
            case ONLINE -> {
                return FBEvaluationRepository.getInstance();
            }
            default -> {
                return LocalEvaluationRepository.getInstance();
            }
        }
    }
    public static ISettingsRepository getSettingsRepository(){
        switch (getInstance().state) {
            case OFFLINE -> {
                return LocalSettingsRepository.getInstance();
            }
            case ONLINE -> {
                return FBSettingsRepository.getInstance();
            }
            default -> {
                return LocalSettingsRepository.getInstance();
            }
        }
    }
}
