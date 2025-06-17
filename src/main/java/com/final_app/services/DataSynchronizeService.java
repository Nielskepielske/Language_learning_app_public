package com.final_app.services;

import com.final_app.factories.RepositoryFactory;
import com.final_app.models.*;
import com.final_app.repositories.firebase.FirebaseManager;
import com.google.firebase.messaging.FirebaseMessaging;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataSynchronizeService {
    private final RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();

    public enum SyncType {
        LOCAL_TO_ONLINE,
        ONLINE_TO_LOCAL
    }

    private ExecutorService writeExecutor;

    public DataSynchronizeService(){
        this.writeExecutor = Executors.newSingleThreadExecutor();
    }


    public CompletableFuture<Void> synchronizeDB(User user, SyncType syncType) {
        if(Dotenv.load().get("DB_MODE").equalsIgnoreCase("OFFLINE") || user == null) {
            return CompletableFuture.completedFuture(null);
        }
        try{
            repositoryFactory.changeToOffline();
            // Get all localRepositories
            System.out.println("Synchronizing user " + user.getUserName() + " with sync type " + syncType);
            CompletableFuture<Optional<User>> userFuture = RepositoryFactory.getUserRepository().getUserById(user.getId());
//            if(userFuture.join().isEmpty()){
//                RepositoryFactory.getUserRepository().addUser(user).join();
//            }
            CompletableFuture<Iterable<LanguageLevelSystem>> localLanguageLevelSystemsFuture = RepositoryFactory.getLanguageRepository().getAllLanguageSystems();
            CompletableFuture<Iterable<Language>> localLanguagesFuture = RepositoryFactory.getLanguageRepository().getAllLanguages();
            CompletableFuture<List<LanguageLevel>> localLanguageLevelsFuture = RepositoryFactory.getLanguageRepository().getAllLanguageLevels();
            CompletableFuture<Iterable<UserLanguage>> localUserLanguagesFuture = RepositoryFactory.getUserLanguageRepository().getAllUserLanguagesFromUser(user.getId());
            CompletableFuture<Iterable<SpeakingTestQuestion>> localSpeakingTestQuestionsFuture = RepositoryFactory.getQuestionRepository().getAllQuestions();
            CompletableFuture<Iterable<SpeakingTest>> localSpeakingTestsFuture = RepositoryFactory.getSpeakingTestRepository().getAllSpeakingTests();
            CompletableFuture<Iterable<Conversation>> localConversationsFuture = RepositoryFactory.getConversationRepository().getAllConversations();
            CompletableFuture<Iterable<Scenario>> localScenariosFuture = RepositoryFactory.getScenarioRepository().getAllScenarios();
            CompletableFuture<Iterable<UserConversation>> localUserConversationsFuture = RepositoryFactory.getUserConversationsRepository().getAllUserConversationsFromUser(user.getId());
            CompletableFuture<Optional<UserStats>> localUserStatsFuture = RepositoryFactory.getUserRepository().getUserStatsByUserId(user.getId());
            CompletableFuture<Optional<Settings>> localSettingsFuture = RepositoryFactory.getSettingsRepository().getSettingsFromUser(user.getId());

            return CompletableFuture.allOf(localLanguageLevelSystemsFuture, localLanguagesFuture, localConversationsFuture, userFuture, localLanguageLevelsFuture, localUserLanguagesFuture, localSpeakingTestsFuture, localSpeakingTestQuestionsFuture, localScenariosFuture, localUserConversationsFuture, localUserStatsFuture, localUserStatsFuture, localSettingsFuture)
                    .thenCompose(result -> {
                        Optional<User> userOptional = userFuture.join();
                        Map<String, LanguageLevelSystem> localLanguageLevelSystems = ((List<LanguageLevelSystem>) localLanguageLevelSystemsFuture.join()).stream().collect(Collectors.toMap(LanguageLevelSystem::getId, Function.identity()));
                        Map<String, Language> localLanguages = ((List<Language>) localLanguagesFuture.join()).stream().collect(Collectors.toMap(Language::getId, Function.identity()));
                        Map<String, LanguageLevel> localLanguageLevels = ((List<LanguageLevel>) localLanguageLevelsFuture.join()).stream().collect(Collectors.toMap(LanguageLevel::getId, Function.identity()));
                        Map<String, SpeakingTestQuestion> localSpeakingTestQuestions = ((List<SpeakingTestQuestion>) localSpeakingTestQuestionsFuture.join()).stream().collect(Collectors.toMap(SpeakingTestQuestion::getId, Function.identity()));
                        Map<String, SpeakingTest> localSpeakingTests = ((List<SpeakingTest>) localSpeakingTestsFuture.join()).stream().collect(Collectors.toMap(SpeakingTest::getId, Function.identity()));
                        Map<String, Conversation> localConversations = ((List<Conversation>) localConversationsFuture.join()).stream().collect(Collectors.toMap(Conversation::getId, Function.identity()));
                        Map<String, Scenario> localScenarios = ((List<Scenario>) localScenariosFuture.join()).stream().collect(Collectors.toMap(Scenario::getId, Function.identity()));
                        Map<String, UserLanguage> localUserLanguages = ((List<UserLanguage>) localUserLanguagesFuture.join()).stream().collect(Collectors.toMap(UserLanguage::getId, Function.identity()));
                        Map<String, UserConversation> localUserConversations = ((List<UserConversation>) localUserConversationsFuture.join()).stream().collect(Collectors.toMap(UserConversation::getId, Function.identity()));
                        Optional<UserStats> localUserStats = localUserStatsFuture.join();
                        Optional<Settings> localSettings = localSettingsFuture.join();
                        Map<String, Evaluation> localEvaluations = new HashMap<>();

                        localUserConversations.forEach((key, value) -> {
                            if (value.getEvaluation() != null) {
                                localEvaluations.put(value.getId(), value.getEvaluation());
                            }
                        });

                        // Getting all online repositories
                        repositoryFactory.changeToOnline();
                        CompletableFuture<Iterable<LanguageLevelSystem>> onlineLanguageLevelSystemsFuture = RepositoryFactory.getLanguageRepository().getAllLanguageSystems();
                        CompletableFuture<Iterable<Language>> onlineLanguagesFuture = RepositoryFactory.getLanguageRepository().getAllLanguages();
                        CompletableFuture<List<LanguageLevel>> onlineLanguageLevelsFuture = RepositoryFactory.getLanguageRepository().getAllLanguageLevels();
                        CompletableFuture<Iterable<SpeakingTestQuestion>> onlineSpeakingTestQuestionsFuture = RepositoryFactory.getQuestionRepository().getAllQuestions();
                        CompletableFuture<Iterable<SpeakingTest>> onlineSpeakingTestsFuture = RepositoryFactory.getSpeakingTestRepository().getAllSpeakingTests();
                        CompletableFuture<Iterable<Conversation>> onlineConversationsFuture = RepositoryFactory.getConversationRepository().getAllConversations();
                        CompletableFuture<Iterable<Scenario>> onlineScenariosFuture = RepositoryFactory.getScenarioRepository().getAllScenarios();
                        CompletableFuture<Iterable<UserLanguage>> onlineUserLanguagesFuture = RepositoryFactory.getUserLanguageRepository().getAllUserLanguagesFromUser(user.getId());
                        CompletableFuture<Iterable<UserConversation>> onlineUserConversationsFuture = RepositoryFactory.getUserConversationsRepository().getAllUserConversationsFromUser(user.getId());
                        CompletableFuture<Optional<UserStats>> onlineUserStatsFuture = RepositoryFactory.getUserRepository().getUserStatsByUserId(user.getId());
                        CompletableFuture<Optional<Settings>> onlineSettingsFuture = RepositoryFactory.getSettingsRepository().getSettingsFromUser(user.getId());




                        return CompletableFuture.allOf(onlineConversationsFuture, onlineLanguageLevelSystemsFuture, onlineLanguagesFuture, onlineLanguageLevelsFuture, onlineUserLanguagesFuture, onlineSpeakingTestsFuture, onlineSpeakingTestQuestionsFuture, onlineScenariosFuture, onlineUserConversationsFuture, onlineUserStatsFuture, onlineSettingsFuture)
                                .thenCompose(result2 -> {
                                    Map<String, LanguageLevelSystem> onlineLanguageLevelSystems = ((List<LanguageLevelSystem>) onlineLanguageLevelSystemsFuture.join()).stream().collect(Collectors.toMap(LanguageLevelSystem::getId, Function.identity()));
                                    Map<String, Language> onlineLanguages = ((List<Language>) onlineLanguagesFuture.join()).stream().collect(Collectors.toMap(Language::getId, Function.identity()));
                                    Map<String, LanguageLevel> onlineLanguageLevels = ((List<LanguageLevel>) onlineLanguageLevelsFuture.join()).stream().collect(Collectors.toMap(LanguageLevel::getId, Function.identity()));
                                    Map<String, SpeakingTestQuestion> onlineSpeakingTestQuestions = ((List<SpeakingTestQuestion>) onlineSpeakingTestQuestionsFuture.join()).stream().collect(Collectors.toMap(SpeakingTestQuestion::getId, Function.identity()));
                                    Map<String, SpeakingTest> onlineSpeakingTests = ((List<SpeakingTest>) onlineSpeakingTestsFuture.join()).stream().collect(Collectors.toMap(SpeakingTest::getId, Function.identity()));
                                    Map<String, Conversation> onlineConversations = ((List<Conversation>) onlineConversationsFuture.join()).stream().collect(Collectors.toMap(Conversation::getId, Function.identity()));
                                    Map<String, Scenario> onlineScenarios = ((List<Scenario>) onlineScenariosFuture.join()).stream().collect(Collectors.toMap(Scenario::getId, Function.identity()));
                                    Map<String, UserLanguage> onlineUserLanguages = ((List<UserLanguage>) onlineUserLanguagesFuture.join()).stream().collect(Collectors.toMap(UserLanguage::getId, Function.identity()));
                                    Map<String, UserConversation> onlineUserConversations = ((List<UserConversation>) onlineUserConversationsFuture.join()).stream().collect(Collectors.toMap(UserConversation::getId, Function.identity()));
                                    Optional<UserStats> onlineUserStats = onlineUserStatsFuture.join();
                                    Optional<Settings> onlineSettings = onlineSettingsFuture.join();
                                    Map<String, Evaluation> onlineEvaluations = new HashMap<>();

                                    onlineUserConversations.forEach((key, value) -> {
                                        if (value.getEvaluation() != null) {
                                            onlineEvaluations.put(value.getId(), value.getEvaluation());
                                        }
                                    });

                                    return CompletableFuture.runAsync(() -> {
                                        if(syncType == SyncType.ONLINE_TO_LOCAL){
                                            repositoryFactory.changeToOffline();

                                            // Synchronizing LanguageLevelSystems
                                            System.out.println("Synchronizing LanguageLevelSystems");
                                            onlineLanguageLevelSystems.forEach((key, onlineLanguageLevelSystem) -> {
                                                if (!localLanguageLevelSystems.containsKey(key)) {
                                                    RepositoryFactory.getLanguageRepository().addLanguageSystem(onlineLanguageLevelSystem);
                                                } else {
                                                    if (onlineLanguageLevelSystem.getLastUpdate() != null && onlineLanguageLevelSystem.getLastUpdate().after(localLanguageLevelSystems.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getLanguageRepository().updateLanguageSystem(onlineLanguageLevelSystem);
                                                    }
                                                }
                                            });
                                            // Synchronizing Languages
                                            System.out.println("Synchronizing Languages");
                                            onlineLanguages.forEach((key, onlineLanguage) -> {
                                                if (!localLanguages.containsKey(key)) {
                                                    RepositoryFactory.getLanguageRepository().addLanguage(onlineLanguage);
                                                } else {
                                                    if (onlineLanguage.getLastUpdate() != null && onlineLanguage.getLastUpdate().after(localLanguages.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getLanguageRepository().updateLanguage(onlineLanguage);
                                                    }
                                                }
                                            });
                                            // Synchronizing LanguageLevels
                                            System.out.println("Synchronizing LanguageLevels");
                                            onlineLanguageLevels.forEach((key, onlineLanguageLevel) -> {
                                                if (!localLanguageLevels.containsKey(key)) {
                                                    RepositoryFactory.getLanguageRepository().addLanguageLevel(onlineLanguageLevel);
                                                } else {
                                                    if (onlineLanguageLevel.getLastUpdate() != null && onlineLanguageLevel.getLastUpdate().after(localLanguageLevels.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getLanguageRepository().updateLanguageLevel(onlineLanguageLevel);
                                                    }
                                                }
                                            });
                                            System.out.println("Synchronizing User");
                                            if(userOptional.isEmpty()) {
                                                RepositoryFactory.getUserRepository().addUser(user)
                                                        .join();
                                            }else{
                                                if(user.getLastUpdate() != null && user.getLastUpdate().after(userOptional.get().getLastUpdate())) {
                                                    RepositoryFactory.getUserRepository().updateUser(user);
                                                }
                                            }
                                            // Synchronizing UserStats
                                            System.out.println("Synchronizing UserStats");
                                            if (onlineUserStats.isPresent()) {
                                                if (!localUserStats.isPresent()) {
                                                    if(onlineUserStats.isPresent()) {
                                                        RepositoryFactory.getUserRepository().saveUserStats(user, onlineUserStats.get());
                                                    }else{
                                                        System.out.println("UserStats is empty");
                                                        UserStats userStats = new UserStats();
                                                        userStats.setUserId(user.getId());
                                                        userStats.setLevel(1);
                                                        userStats.setTotalXp(0);
                                                        userStats.setStreak(0);
                                                        userStats.setLastUpdate(new Date());
                                                        RepositoryFactory.getUserRepository().saveUserStats(user, userStats);
                                                    }
                                                } else {
                                                    if (onlineUserStats.get().getLastUpdate() != null && onlineUserStats.get().getLastUpdate().after(localUserStats.get().getLastUpdate())) {
                                                        RepositoryFactory.getUserRepository().saveUserStats(user, onlineUserStats.get());
                                                    }
                                                }
                                            }
                                            // Synchronizing Settings
                                            System.out.println("Synchronizing Settings");
                                            if (onlineSettings.isPresent()) {
                                                if (!localSettings.isPresent()) {
                                                    RepositoryFactory.getSettingsRepository().saveSettings(onlineSettings.get());
                                                } else {
                                                    if (onlineSettings.get().getLastUpdate() != null && onlineSettings.get().getLastUpdate().after(localSettings.get().getLastUpdate())) {
                                                        RepositoryFactory.getSettingsRepository().saveSettings(onlineSettings.get());
                                                    }
                                                }
                                            }
                                            // Synchronizing UserLanguages
                                            System.out.println("Synchronizing UserLanguages");
                                            onlineUserLanguages.forEach((key, onlineUserLanguage) -> {
                                                if (!localUserLanguages.containsKey(key)) {
                                                    RepositoryFactory.getUserLanguageRepository().addUserLanguage(onlineUserLanguage);
                                                } else {
                                                    if (onlineUserLanguage.getLastUpdate() != null && onlineUserLanguage.getLastUpdate().after(localUserLanguages.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getUserLanguageRepository().updateUserLanguage(onlineUserLanguage);
                                                    }
                                                }
                                            });
                                            // Synchronizing SpeakingTestQuestions
                                            System.out.println("Synchronizing SpeakingTestQuestions");
                                            onlineSpeakingTestQuestions.forEach((key, onlineSpeakingTestQuestion) -> {
                                                if (!localSpeakingTestQuestions.containsKey(key)) {
                                                    RepositoryFactory.getQuestionRepository().addQuestion(onlineSpeakingTestQuestion);
                                                } else {
                                                    if (onlineSpeakingTestQuestion.getLastUpdate() != null && onlineSpeakingTestQuestion.getLastUpdate().after(localSpeakingTestQuestions.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getQuestionRepository().updateQuestion(onlineSpeakingTestQuestion);
                                                    }
                                                }
                                            });
                                            // Synchronizing SpeakingTests
                                            System.out.println("Synchronizing SpeakingTests");
                                            onlineSpeakingTests.forEach((key, onlineSpeakingTest) -> {
                                                if (!localSpeakingTests.containsKey(key)) {
                                                    RepositoryFactory.getSpeakingTestRepository().addSpeakingTest(onlineSpeakingTest);
                                                } else {
                                                    if (onlineSpeakingTest.getLastUpdate() != null && onlineSpeakingTest.getLastUpdate().after(localSpeakingTests.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getSpeakingTestRepository().updateSpeakingTest(onlineSpeakingTest);
                                                    }
                                                }
                                            });
                                            // Synchronizing Scenarios
                                            System.out.println("Synchronizing Scenarios");
                                            onlineScenarios.forEach((key, onlineScenario) -> {
                                                if (!localScenarios.containsKey(key)) {
                                                    RepositoryFactory.getScenarioRepository().addScenario(onlineScenario);
                                                } else {
                                                    if (onlineScenario.getLastUpdate() != null && onlineScenario.getLastUpdate().after(localScenarios.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getScenarioRepository().updateScenario(onlineScenario);
                                                    }
                                                }
                                            });
                                            // Synchronizing Conversations
                                            System.out.println("Synchronizing Conversations");
                                            onlineConversations.forEach((key, onlineConversation) -> {
                                                if (!localConversations.containsKey(key)) {
                                                    RepositoryFactory.getConversationRepository().addConversation(onlineConversation);
                                                } else {
                                                    if (onlineConversation.getLastUpdate() != null && onlineConversation.getLastUpdate().after(localConversations.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getConversationRepository().updateConversation(onlineConversation);
                                                    }
                                                }
                                            });



                                            // Synchronize the user itself

                                            // Synchronizing UserConversations
                                            System.out.println("Synchronizing UserConversations");
                                            onlineUserConversations.forEach((key, onlineUserConversation) -> {
                                                if (!localUserConversations.containsKey(key)) {
                                                    RepositoryFactory.getUserConversationsRepository().addUserConversation(onlineUserConversation);
                                                } else {
                                                    if (onlineUserConversation.getUpdatedAt() != null && onlineUserConversation.getUpdatedAt().after(localUserConversations.get(key).getUpdatedAt())) {
                                                        RepositoryFactory.getUserConversationsRepository().updateUserConversation(onlineUserConversation);
                                                    }
                                                }
                                            });

                                            // Synchronizing Evaluations
                                            System.out.println("Synchronizing Evaluations");
                                            onlineEvaluations.forEach((key, onlineEvaluation) -> {
                                                if (!localEvaluations.containsKey(key)) {
                                                    RepositoryFactory.getEvaluationRepository().addEvaluation(onlineEvaluation);
                                                }
                                            });

                                        }else if (syncType == SyncType.LOCAL_TO_ONLINE){
                                            repositoryFactory.changeToOnline();
                                            // Synchronizing LanguageLevelSystems
                                            System.out.println("Synchronizing LanguageLevelSystems");
                                            localLanguageLevelSystems.forEach((key, localLanguageLevelSystem) -> {
                                                if (!onlineLanguageLevelSystems.containsKey(key)) {
                                                    RepositoryFactory.getLanguageRepository().addLanguageSystem(localLanguageLevelSystem);
                                                } else {
                                                    if (onlineLanguageLevelSystems.get(key).getLastUpdate() == null || localLanguageLevelSystem.getLastUpdate().after(onlineLanguageLevelSystems.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getLanguageRepository().updateLanguageSystem(localLanguageLevelSystem);
                                                    }
                                                }
                                            });
                                            // Synchronizing Languages
                                            System.out.println("Synchronizing Languages");
                                            localLanguages.forEach((key, localLanguage) -> {
                                                if (!onlineLanguages.containsKey(key)) {
                                                    RepositoryFactory.getLanguageRepository().addLanguage(localLanguage);
                                                } else {
                                                    if (onlineLanguages.get(key).getLastUpdate() == null || localLanguage.getLastUpdate().after(onlineLanguages.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getLanguageRepository().updateLanguage(localLanguage);
                                                    }
                                                }
                                            });
                                            // Synchronizing LanguageLevels
                                            System.out.println("Synchronizing LanguageLevels");
                                            localLanguageLevels.forEach((key, localLanguageLevel) -> {
                                                if (!onlineLanguageLevels.containsKey(key)) {
                                                    RepositoryFactory.getLanguageRepository().addLanguageLevel(localLanguageLevel);
                                                } else {
                                                    if (onlineLanguageLevels.get(key).getLastUpdate() == null || localLanguageLevel.getLastUpdate().after(onlineLanguageLevels.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getLanguageRepository().updateLanguageLevel(localLanguageLevel);
                                                    }
                                                }
                                            });
                                            // Synchronizing SpeakingTestQuestions
                                            System.out.println("Synchronizing SpeakingTestQuestions");
                                            localSpeakingTestQuestions.forEach((key, localSpeakingTestQuestion) -> {
                                                if (!onlineSpeakingTestQuestions.containsKey(key)) {
                                                    RepositoryFactory.getQuestionRepository().addQuestion(localSpeakingTestQuestion);
                                                } else {
                                                    if (onlineSpeakingTestQuestions.get(key).getLastUpdate() == null || localSpeakingTestQuestion.getLastUpdate().after(onlineSpeakingTestQuestions.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getQuestionRepository().updateQuestion(localSpeakingTestQuestion);
                                                    }
                                                }
                                            });
                                            // Synchronizing SpeakingTests
                                            System.out.println("Synchronizing SpeakingTests");
                                            localSpeakingTests.forEach((key, localSpeakingTest) -> {
                                                if (!onlineSpeakingTests.containsKey(key)) {
                                                    RepositoryFactory.getSpeakingTestRepository().addSpeakingTest(localSpeakingTest);
                                                } else {
                                                    if (onlineSpeakingTests.get(key).getLastUpdate() == null || localSpeakingTest.getLastUpdate().after(onlineSpeakingTests.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getSpeakingTestRepository().updateSpeakingTest(localSpeakingTest);
                                                    }
                                                }
                                            });
                                            // Synchronizing Conversations
                                            System.out.println("Synchronizing Conversations");
                                            localConversations.forEach((key, localConversation) -> {
                                                if (!onlineConversations.containsKey(key)) {
                                                    RepositoryFactory.getConversationRepository().addConversation(localConversation);
                                                } else {
                                                    if (onlineConversations.get(key).getLastUpdate() == null || localConversation.getLastUpdate().after(onlineConversations.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getConversationRepository().updateConversation(localConversation);
                                                    }
                                                }
                                            });
                                            // Synchronizing Scenarios
                                            System.out.println("Synchronizing Scenarios");
                                            localScenarios.forEach((key, localScenario) -> {
                                                if (!onlineScenarios.containsKey(key)) {
                                                    RepositoryFactory.getScenarioRepository().addScenario(localScenario);
                                                } else {
                                                    if (onlineScenarios.get(key).getLastUpdate() == null || localScenario.getLastUpdate().after(onlineScenarios.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getScenarioRepository().updateScenario(localScenario);
                                                    }
                                                }
                                            });
                                            // Synchronizing UserLanguages
                                            System.out.println("Synchronizing UserLanguages");
                                            localUserLanguages.forEach((key, localUserLanguage) -> {
                                                if (!onlineUserLanguages.containsKey(key)) {
                                                    RepositoryFactory.getUserLanguageRepository().addUserLanguage(localUserLanguage);
                                                } else {
                                                    if (onlineUserLanguages.get(key).getLastUpdate() == null || localUserLanguage.getLastUpdate().after(onlineUserLanguages.get(key).getLastUpdate())) {
                                                        RepositoryFactory.getUserLanguageRepository().updateUserLanguage(localUserLanguage);
                                                    }
                                                }
                                            });
                                            // Synchronizing UserConversations
                                            System.out.println("Synchronizing UserConversations");
                                            localUserConversations.forEach((key, localUserConversation) -> {
                                                if (!onlineUserConversations.containsKey(key)) {
                                                    RepositoryFactory.getUserConversationsRepository().addUserConversation(localUserConversation);
                                                } else {
                                                    if (onlineUserConversations.get(key).getUpdatedAt() == null || localUserConversation.getUpdatedAt().after(onlineUserConversations.get(key).getUpdatedAt())) {
                                                        RepositoryFactory.getUserConversationsRepository().updateUserConversation(localUserConversation);
                                                    }
                                                }
                                            });
                                            // Synchronizing User
                                            System.out.println("Synchronizing User");
                                            if(userOptional.isPresent()) {
                                                if (user.getLastUpdate() == null || user.getLastUpdate().after(userOptional.get().getLastUpdate())) {
                                                    RepositoryFactory.getUserRepository().updateUser(user);
                                                }
                                            }else{
                                                RepositoryFactory.getUserRepository().addUser(user)
                                                        .join();
                                            }
                                            // Synchronizing Settings
                                            System.out.println("Synchronizing Settings");
                                            if (localSettings.isPresent()) {
                                                if (!onlineSettings.isPresent()) {
                                                    RepositoryFactory.getSettingsRepository().saveSettings(localSettings.get());
                                                } else {
                                                    if (localSettings.get().getLastUpdate() == null || localSettings.get().getLastUpdate().after(onlineSettings.get().getLastUpdate())) {
                                                        RepositoryFactory.getSettingsRepository().saveSettings(localSettings.get());
                                                    }
                                                }
                                            }
                                            // Synchronizing UserStats
                                            System.out.println("Synchronizing UserStats");
                                            if (localUserStats.isPresent()) {
                                                if (!onlineUserStats.isPresent()) {
                                                    RepositoryFactory.getUserRepository().saveUserStats(user, localUserStats.get());
                                                } else {
                                                    if (onlineUserStats.get().getLastUpdate() == null || localUserStats.get().getLastUpdate().after(onlineUserStats.get().getLastUpdate())) {
                                                        RepositoryFactory.getUserRepository().saveUserStats(user, localUserStats.get());
                                                    }
                                                }
                                            }
                                            // Synchronizing Evaluations
                                            System.out.println("Synchronizing Evaluations");
                                            localEvaluations.forEach((key, localEvaluation) -> {
                                                if (!onlineEvaluations.containsKey(key)) {
                                                    RepositoryFactory.getEvaluationRepository().addEvaluation(localEvaluation);
                                                }
                                            });
                                        }
                                    }, writeExecutor);
                                });
                    });
        }catch (Exception e){
            System.out.println("Error during synchronization: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }


    }
}
