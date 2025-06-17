package com.final_app.services;

import com.final_app.db.DatabaseManager;
import com.final_app.factories.RepositoryFactory;
import com.final_app.globals.AIModels;
import com.final_app.globals.ConversationStatus;
import com.final_app.globals.Sender;
import com.final_app.interfaces.*;
import com.final_app.models.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service class for conversation-related operations
 */
public class ConversationService {
    /**
     * Create a new conversation template
     */
    public Conversation createConversation(String title, String description, String languageId, String languageFromId,
                                           String levelId, String scenarioId,
                                           String startPrompt, String model) throws SQLException, ExecutionException, InterruptedException {
        Conversation conversation = new Conversation(title, description, languageId,
                levelId, scenarioId,
                startPrompt, model);
        conversation.setLanguageFromId(languageFromId);
        RepositoryFactory.getConversationRepository().addConversation(conversation).get();
        return conversation;
    }

    public Conversation createConversation(Conversation conversation) throws SQLException, ExecutionException, InterruptedException {
        return createConversation(conversation.getTitle(), conversation.getDescription(), conversation.getLanguageId(), conversation.getLanguageFromId(), conversation.getLevelId(), conversation.getScenarioId(), conversation.getStartPrompt(), AIModels.CONVERSATION.getModel());
    }

    /**
     * Create a conversationChain
     */
    public void createConversationChain(ConversationChain conversationChain) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getConversationRepository().addConversationChain(conversationChain);
    }

    /**
     * Get a conversation by ID
     */
    public Conversation getConversationById(String id) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getConversationRepository().getConversationById(id).get().orElseThrow();
    }

    /**
     * Get a conversationchain by ID
     */
    public ConversationChain getConversationChainById(String id) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getConversationRepository().getConversationChainById(id).get().orElseThrow();
    }

    /**
     * Get all conversations
     */
    public List<Conversation> getAllConversations() throws SQLException, ExecutionException, InterruptedException {
        Iterable<Conversation> iterableConversations = RepositoryFactory.getConversationRepository().getAllConversations().get();
        return StreamSupport.stream(iterableConversations.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get all conversationchains
     */
    public List<ConversationChain> getAllConversationChains() throws SQLException, ExecutionException, InterruptedException {
        Iterable<ConversationChain> iterableConversationChains = RepositoryFactory.getConversationRepository().getAllConversationChains().get();
        return StreamSupport.stream(iterableConversationChains.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get conversations by language
     */
    public List<Conversation> getConversationsByLanguage(String languageId) throws SQLException, ExecutionException, InterruptedException {
        Iterable<Conversation> iterableConversations = RepositoryFactory.getConversationRepository().getAllConversationsByLanguage(languageId).get();
        return StreamSupport.stream(iterableConversations.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get conversations by level
     */
    public List<Conversation> getConversationsByLevel(String levelId) throws SQLException, ExecutionException, InterruptedException {
        Iterable<Conversation> iterableConversations = RepositoryFactory.getConversationRepository().getAllConversationsByLevel(levelId).get();
        return StreamSupport.stream(iterableConversations.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Update an existing conversation
     */
    public void updateConversation(Conversation conversation) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getConversationRepository().updateConversation(conversation).get();
    }

    /**
     * Delete a conversation
     */
    public void deleteConversation(String id) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getConversationRepository().deleteConversationById(id).get();
    }

    /**
     * Create a new scenario with key points
     */
    public Scenario createScenario(String description, String role, List<String> keyPoints) throws SQLException, ExecutionException, InterruptedException {
        Scenario scenario = new Scenario(description, role);
        if (keyPoints != null) {
            scenario.setKeyPoints(keyPoints);
        }
        RepositoryFactory.getScenarioRepository().addScenario(scenario).get();
        return scenario;
    }

    /**
     * Get a scenario by ID
     */
    public Scenario getScenarioById(String id) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getScenarioRepository().getScenarioById(id).get().orElseThrow();
    }

    /**
     * Start a conversation for a user
     */
    public UserConversation startConversation(String userId, String conversationId) throws SQLException, ExecutionException, InterruptedException {
        UserConversation userConversation = new UserConversation(userId, conversationId, ConversationStatus.NOTSTARTED.name());
        RepositoryFactory.getUserConversationsRepository().addUserConversation(userConversation).get();
        return RepositoryFactory.getUserConversationsRepository().getUserConversationById(userConversation.getId()).get().orElse(null);
    }

    /**
     * Starts a user conversation when activated from a conversationchain
     *
     * @param userId
     * @param conversationChainId
     * @param conversationId
     * @return
     * @throws SQLException
     */
    public UserConversation startConversationInConversationChain(String userId, String conversationChainId, String conversationId) throws SQLException, ExecutionException, InterruptedException {
        UserConversation userConversation = startConversation(userId, conversationId);
        UserConversationChainItem userConversationChainItem = new UserConversationChainItem(userConversation.getId(), conversationChainId);
        RepositoryFactory.getUserConversationsRepository().addUserConversationChainItem(userConversationChainItem).get();
        return userConversation;
    }

    /**
     * Get a user conversation by ID
     */
    public UserConversation getUserConversationById(String id) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getUserConversationsRepository().getUserConversationById(id).get().orElse(null);
    }

    /**
     * Checks if the userconversation is part of a conversationchain or not
     *
     * @param userConversation
     * @return true | false
     */
    public boolean checkIfUserConversationInChain(UserConversation userConversation) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getUserConversationsRepository().getUserConversationChainItemByUserConversationId(userConversation.getId()).get().isPresent();
    }

    /**
     * @param conversationChainId
     * @param conversationId
     * @param userId
     * @return UserConversationChainItem | null
     * @throws SQLException
     */
    public UserConversationChainItem getUserConversationChainItemByConversationId(String conversationChainId, String conversationId, String userId) throws SQLException, ExecutionException, InterruptedException {
        List<UserConversation> userConversations = StreamSupport.stream(RepositoryFactory.getUserConversationsRepository().getUserConversationsByUserAndConversationId(userId, conversationId).get().spliterator(), false).collect(Collectors.toList());
        List<UserConversationChainItem> userConversationChainItems = StreamSupport.stream(RepositoryFactory.getUserConversationsRepository().getAllUserConversationChainItemsFromConversationChainId(conversationChainId).get().spliterator(), false).collect(Collectors.toList());

        for (UserConversation userConversation : userConversations) {
            for (UserConversationChainItem userConversationChainItem : userConversationChainItems) {
                if (userConversation.getId().equals(userConversationChainItem.getUserConversationId())) return userConversationChainItem;
            }
        }
        return null;
    }

    /**
     * Get all the user conversation connected to a conversation chain
     *
     * @param id
     * @return
     * @throws SQLException
     */
    public List<UserConversationChainItem> getUserConversationFromConversationChain(String id) throws SQLException, ExecutionException, InterruptedException {
        ConversationChain conversationChain = RepositoryFactory.getConversationRepository().getConversationChainById(id).get().orElseThrow();
        List<UserConversationChainItem> userConversationChainItems = new ArrayList<>();
        for (ConversationChainItem conversationChainItem : conversationChain.getConversations()) {
            List<UserConversation> userConversations2 = StreamSupport.stream(RepositoryFactory.getUserConversationsRepository().getUserConversationsByUserAndConversationId(AppService.getInstance().getCurrentUser().getId(), conversationChainItem.getConversationId()).get().spliterator(), false).collect(Collectors.toList());
            for (UserConversation userConversation : userConversations2) {
                UserConversationChainItem userConversationChainItem = RepositoryFactory.getUserConversationsRepository().getUserConversationChainItemByUserConversationId(userConversation.getId()).get().orElse(null);
                if (userConversationChainItem == null) continue;
                if (userConversationChainItem.getConversationChainId().equals(id)) userConversationChainItems.add(userConversationChainItem);
            }
        }
        return userConversationChainItems;
    }

    /**
     * Get all conversations for a user
     */
    public List<UserConversation> getUserConversations(String userId) throws SQLException, ExecutionException, InterruptedException {
        Iterable<UserConversation> iterableUserConversations = RepositoryFactory.getUserConversationsRepository().getAllUserConversationsFromUser(userId).join();
        return StreamSupport.stream(iterableUserConversations.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Update the status of a user conversation
     */
    public void updateConversationStatus(String userConversationId, ConversationStatus status) throws SQLException, ExecutionException, InterruptedException {
        UserConversation userConversation = RepositoryFactory.getUserConversationsRepository().getUserConversationById(userConversationId).get().orElseThrow();
        userConversation.updateStatus(status);
        RepositoryFactory.getUserConversationsRepository().updateUserConversation(userConversation).get();
    }

    public void updateConversationMessages(UserConversation userConversation) throws SQLException, ExecutionException, InterruptedException {
        for(Message msg : userConversation.getMessages()){
            RepositoryFactory.getMessageRepository().updateMessage(msg).get();
        }
    }

    /**
     * Add a message to a conversation
     *
     * @param index
     * @param userConversationId
     * @param text
     * @param sender
     */
    public Message addMessage(int index, String userConversationId, String text, Sender sender) throws SQLException, ExecutionException, InterruptedException {
        Message message = new Message(index, userConversationId, text, sender.name());
        message.setTimestamp(Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        RepositoryFactory.getMessageRepository().addMessage(message);
        return message;
    }

    /**
     * Get all messages for a conversation
     */
    public List<Message> getConversationMessages(String userConversationId) throws SQLException, ExecutionException, InterruptedException {
        Iterable<Message> iterableMessages = RepositoryFactory.getMessageRepository().getAllMessagesFromUserConversation(userConversationId).get();
        return StreamSupport.stream(iterableMessages.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Delete a user conversation and all its messages
     */
    public void deleteUserConversation(String userConversationId) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getUserConversationsRepository().deleteUserConversationById(userConversationId).get();
    }

    /**
     * Add an evaluation for a user conversation and award XP
     */
    public Evaluation evaluateConversation(String userConversationId, int score, int maxScore, int vocab, int grammar, String feedback) throws SQLException, ExecutionException, InterruptedException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Get user conversation
            UserConversation userConversation = RepositoryFactory.getUserConversationsRepository().getUserConversationById(userConversationId).get().orElseThrow();
            if (userConversation == null) {
                throw new SQLException("User conversation not found");
            }

            // Create evaluation
            Evaluation evaluation = new Evaluation(userConversationId, score, maxScore, vocab, grammar, feedback);

            // Check if evaluation already exists
            Evaluation existing = getEvaluationByUserConversationId(userConversationId);
            if (existing != null) {
                // Update existing evaluation
                evaluation.setId(existing.getId());
                RepositoryFactory.getEvaluationRepository().updateEvaluation(evaluation).get();
            } else {
                // Insert new evaluation
                RepositoryFactory.getEvaluationRepository().addEvaluation(evaluation).get();
            }

            // Update conversation status
            userConversation.updateStatus(ConversationStatus.COMPLETED);
            RepositoryFactory.getUserConversationsRepository().updateUserConversation(userConversation).get();

            // Award XP based on evaluation
            awardXpForEvaluation(userConversation, evaluation, conn);

            conn.commit();
            return evaluation;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Award XP based on evaluation results
     */
    private void awardXpForEvaluation(UserConversation userConversation, Evaluation evaluation, Connection conn) throws SQLException, ExecutionException, InterruptedException {
        // Get services
        XpService xpService = new XpService();
        UserService userService = AppService.getInstance().getUserService();
        LanguageService languageService = AppService.getInstance().getLanguageService();

        // Calculate XP to award
        int xpAmount = xpService.calculateConversationXp(userConversation, evaluation);

        // Add XP to user's general stats
        userService.addXp(userConversation.getUserId(), xpAmount, "CONVERSATION",
                "Completed: " + userConversation.getConversation().getTitle());

        // Add XP to the specific language
        Conversation conversation = userConversation.getConversation();
        if (conversation != null && conversation.getLanguage() != null) {
            UserLanguage userLanguage = languageService.getUserLanguage(
                    userConversation.getUserId(), conversation.getLanguageId());
            if (userLanguage != null) {
                languageService.addLanguageXp(userLanguage.getId(), xpAmount);
            }
        }
    }

    /**
     * Get the evaluation for a user conversation
     */
    public Evaluation getEvaluationByUserConversationId(String userConversationId) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getEvaluationRepository().getEvaluationByUserConversationId(userConversationId).get().orElse(null);
    }
}