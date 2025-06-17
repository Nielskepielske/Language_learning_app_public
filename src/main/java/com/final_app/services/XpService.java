package com.final_app.services;

import com.final_app.globals.ConversationStatus;
import com.final_app.globals.Difficulty;
import com.final_app.globals.Sender;
import com.final_app.models.*;

/**
 * Service dedicated to XP calculations and rewards
 */
public class XpService {

    // Base XP for different activities
    private static final int BASE_XP_CONVERSATION = 50;
    private static final int BASE_XP_SPEAKING_TEST = 100;
    private static final int BASE_XP_DAILY_STREAK = 20;

    // Multipliers
    private static final double LANGUAGE_LEVEL_MULTIPLIER = 0.5; // per level
    private static final double DIFFICULTY_MULTIPLIER = 0.2; // per difficulty level
    private static final double QUALITY_MULTIPLIER = 0.01; // per point in score

    // Bonuses
    private static final int PERFECT_SCORE_BONUS = 50;
    private static final int STREAK_MILESTONE_BONUS = 100; // Every 7 days
    private static final int FIRST_COMPLETION_BONUS = 30;

    // Punishment
    private static final int TRANSLATION_XP_LOSS = 20;

    /**
     * Calculate XP for completing a conversation based on evaluation
     */
    public int calculateConversationXp(UserConversation userConversation, Evaluation evaluation) {
        Conversation conversation = userConversation.getConversation();
        if (conversation == null || evaluation == null) {
            return 0;
        }

        // Base XP
        int xp = BASE_XP_CONVERSATION;

        // Language level bonus
        LanguageLevel level = conversation.getLanguageLevel();
        if (level != null) {
            xp += (level.getValue() * BASE_XP_CONVERSATION * LANGUAGE_LEVEL_MULTIPLIER);
        }

        // Score quality bonus
        double scorePercentage = (double) evaluation.getScore() / evaluation.getMaxScore();
        xp += (evaluation.getScore() * QUALITY_MULTIPLIER * BASE_XP_CONVERSATION);

        // Perfect score bonus (>= 95%)
        if (scorePercentage >= 0.95) {
            xp += PERFECT_SCORE_BONUS;
        }

        // First completion bonus
        if (userConversation.getStatusEnum() != ConversationStatus.COMPLETED) {
            xp += FIRST_COMPLETION_BONUS;
        }

        int translationsUsed = userConversation.getMessages().stream().filter(msg -> msg.getSenderEnum() == Sender.TRANSLATION).toList().size();

        xp -= (translationsUsed * TRANSLATION_XP_LOSS);

        return xp;
    }

    /**
     * Calculate XP for completing a speaking test
     */
    public int calculateSpeakingTestXp(UserConversation userConversation, Evaluation evaluation, Difficulty difficulty) {
        if (userConversation == null || evaluation == null) {
            return 0;
        }

        // Base XP
        int xp = BASE_XP_SPEAKING_TEST;

        // Difficulty bonus
        if (difficulty != null) {
            xp += (difficulty.getValue() * BASE_XP_SPEAKING_TEST * DIFFICULTY_MULTIPLIER);
        }

        // Score quality bonus
        double scorePercentage = (double) evaluation.getScore() / evaluation.getMaxScore();
        xp += (evaluation.getScore() * QUALITY_MULTIPLIER * BASE_XP_SPEAKING_TEST);

        // Perfect score bonus
        if (scorePercentage >= 0.95) {
            xp += PERFECT_SCORE_BONUS;
        }

        return xp;
    }

    /**
     * Calculate daily streak bonus
     */
    public int calculateStreakBonus(int currentStreak) {
        int bonus = BASE_XP_DAILY_STREAK;

        // Milestone bonuses (e.g., every 7 days)
        if (currentStreak > 0 && currentStreak % 7 == 0) {
            bonus += STREAK_MILESTONE_BONUS;
        }

        // Gradual increase for longer streaks
        bonus += Math.min(100, currentStreak); // Cap at +100 XP

        return bonus;
    }

    /**
     * Calculate level required for a given amount of XP
     */
    public int calculateLevelFromXp(long totalXp) {
        // Formula: level = 1 + floor(sqrt(totalXp / 100))
        // This creates a gradually increasing XP requirement for each level
        return 1 + (int) Math.floor(Math.sqrt(totalXp / 100.0));
    }

    /**
     * Calculate XP required for the next level
     */
    public long calculateXpForNextLevel(int currentLevel) {
        // Inverse of the level calculation formula
        // XP = (level-1)^2 * 100
        int nextLevel = currentLevel + 1;
        return (long) Math.pow(nextLevel - 1, 2) * 100;
    }

    /**
     * Calculate XP required to reach a specific level
     */
    public long calculateXpForLevel(int targetLevel) {
        return (long) Math.pow(targetLevel - 1, 2) * 100;
    }

    /**
     * Calculate progress percentage toward next level
     */
    public double calculateProgressToNextLevel(long currentXp, int currentLevel) {
        long currentLevelXp = calculateXpForLevel(currentLevel);
        long nextLevelXp = calculateXpForLevel(currentLevel + 1);

        return (double) (currentXp - currentLevelXp) / (nextLevelXp - currentLevelXp); // this is more for if you want the progress to reset at each level
        //return (double) currentXp / nextLevelXp;
    }
    /**
     * Given totalXp and a LanguageLevelSystem (e.g. CEFR),
     * compute the user’s numeric level and then pick the
     * highest LanguageLevel whose 'value' ≤ that numeric level.
     */
    public LanguageLevel mapXpToOfficialLevel(long totalXp,
                                              LanguageLevelSystem system) {
        // 1) compute your numeric level
        int numericLevel = calculateLevelFromXp(totalXp);

        // 2) find the highest threshold ≤ numericLevel
        //    (assumes system.getLevels() is populated and
        //     sorted ascending by value)
        LanguageLevel best = null;
        for (LanguageLevel lvl : system.getLevels()) {
            if (numericLevel >= lvl.getLevelThreshold()) {
                best = lvl;
            } else {
                break;  // since levels are sorted
            }
        }

        return best;  // null if numericLevel is below the first threshold
    }

}
