package com.final_app.services;

import com.final_app.globals.Sender;
import com.final_app.models.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Enhanced evaluation service for more consistent language assessments
 */
public class EvaluationService {
    private static final Gson gson = new Gson();

    // Standard evaluation criteria with weights
    private static final Map<String, Double> CRITERIA_WEIGHTS = Map.of(
            "vocabulary", 0.30,
            "grammar", 0.30,
            "correctness", 0.2,
            "purpose", 0.1,
            "duration", 0.1
//            "fluency", 0.20,
//            "pronunciation", 0.10,
//            "comprehension", 0.10
    );

    // Scoring rubric descriptions (simplified)
    private static final Map<String, List<String>> SCORING_RUBRIC = Map.of(
            "vocabulary", Arrays.asList(
                    "Very limited vocabulary, only basic words",
                    "Limited vocabulary with some topic-specific terms",
                    "Adequate vocabulary for the topic, occasional use of advanced terms",
                    "Good range of vocabulary with appropriate topic-specific terms",
                    "Excellent vocabulary with precise word choice and idiomatic expressions"
            ),
            "grammar", Arrays.asList(
                    "Frequent major errors that impede understanding",
                    "Many errors, but meaning usually clear",
                    "Some errors in complex structures, basic structures generally correct",
                    "Only minor errors that don't impede understanding",
                    "Near-perfect grammar with only rare, minor errors"
            ),
            "fluency", Arrays.asList(
                    "Very hesitant, frequent long pauses",
                    "Noticeable hesitation, some pauses",
                    "Somewhat fluid with occasional hesitation",
                    "Generally fluid with minimal hesitation",
                    "Natural, fluid speech comparable to native speaker"
            ),
            "pronunciation", Arrays.asList(
                    "Often unintelligible, heavy accent impedes understanding",
                    "Frequent pronunciation errors, but generally understandable",
                    "Some pronunciation errors, but clearly understandable",
                    "Clear pronunciation with occasional non-native features",
                    "Near-native pronunciation"
            ),
            "comprehension", Arrays.asList(
                    "Minimal understanding of questions or prompts",
                    "Understands simple questions but often needs repetition",
                    "Understands most questions with occasional confusion",
                    "Good comprehension with rare misunderstandings",
                    "Complete comprehension of all questions and nuances"
            )
    );

    /**
     * Generate a structured evaluation prompt for the AI
     */
    public String generateEvaluationPrompt(UserConversation userConversation) throws SQLException {
        Conversation conversation = userConversation.getConversation();

        Settings settings = AppService.getInstance().getUserService().getUserSettings(AppService.getInstance().getCurrentUser().getId()).join();

        StringBuilder promptBuilder = new StringBuilder();

        String languageLevel = userConversation.getConversation().getLanguageLevel().getName();

        promptBuilder.append("You are a professional language expert. Your job is to grade a learner’s conversation based on their declared " + userConversation.getConversation().getLanguage().getLanguageLevelSystem().getName() + " level using the following criteria.\n" +
                "\n" +
                "Please assess each on a scale from 0–10 (integers only):\n" +
                "- vocabulary\n" +
                "- grammar\n" +
                "- correctness\n" +
                "- purpose\n" +
                "- duration\n" +
                "\n" +
                "Return your evaluation in JSON format exactly like this:\n" +
                "{\n" +
                "  \"score\": (overall score out of 100),\n" +
                "  \"max_score\": 100,\n" +
                "  \"vocabulary\": (1–10),\n" +
                "  \"grammar\": (1–10),\n" +
                "  \"correctness\": (1–10),\n" +
                "  \"purpose\": (1–10),\n" +
                "  \"duration\": (1–10),\n" +
                "  \"vocab\": (same as vocabulary),\n" +
                "  \"grammar_dup\": (same as grammar),\n" +
                "  \"description\": (≤100 words of feedback with examples and tips)\n" +
                "}\n" +
                "\n" +
                "IMPORTANT INSTRUCTIONS:\n" +
                "1. **Adjust expectations by declared level (" + languageLevel + "):**  \n" +
                "   - **Vocabulary (1–10):**  \n" +
                "     Evaluate whether the learner uses a **range of words appropriate for " + languageLevel + "**.  \n" +
                "     - 9–10 points if the learner employs a varied and appropriate set of words for the topic, even if simple.  \n" +
                "     - 7–8 points if there is some repetition or minor inaccuracies but the meaning is clear.  \n" +
                "     - 5–6 points if vocabulary is very limited but still conveys the main ideas.  \n" +
                "     - 1–4 points if the learner’s word choices frequently obscure meaning.  \n" +
                "   - **Grammar (1–10):**  \n" +
                "     Evaluate whether the learner’s sentence structures match what is expected at " + languageLevel + ".  \n" +
                "     - 9–10 points if they consistently use correct forms (e.g., appropriate tenses, basic agreement), with only minor, level-appropriate errors.  \n" +
                "     - 7–8 points if a few mistakes appear (e.g., missing articles or slight word-order slips) but meaning remains intelligible.  \n" +
                "     - 5–6 points if errors are more frequent but not completely obscuring communication.  \n" +
                "     - 1–4 points if grammatical errors often make the message hard to follow.\n" +
                "\n" +
                "2. **Correctness (1–10):**  \n" +
                "   - Compare each user turn to the previous AI prompt. Score based on how accurately and coherently the learner answers, **given their level**.  \n" +
                "   - 9–10 if the learner’s reply fully addresses the question/prompt in a way you would expect at " + languageLevel + ".  \n" +
                "   - 7–8 if there is a minor slip in content or relevance, but the answer is still on track.  \n" +
                "   - 5–6 if the learner partially misunderstands or omits key information.  \n" +
                "   - 1–4 if the reply is frequently off-topic or irrelevant.\n" +
                "\n" +
                "3. **Purpose (1–10):**  \n" +
                "   - Score how many of the **declared conversation goals** the learner achieved.  \n" +
                "   - 9–10 if all goals are met.  \n" +
                "   - 7–8 if most goals are met.  \n" +
                "   - 5–6 if only one goal is met.  \n" +
                "   - 1–4 if none of the goals are met.\n" +
                "\n" +
                "4. **Duration (1–10):**  \n" +
                "   - Count the total number of user and AI turns in the conversation. Adjust for expected length at " + languageLevel + " (e.g., lower levels often produce fewer turns). Use this bracket as a guideline:  \n" +
                "     - 0–2 turns → 0 points  \n" +
                "     - 3–4 turns → 4 points  \n" +
                "     - 5–7 turns → 6 points  \n" +
                "     - 8–10 turns → 8 points  \n" +
                "     - More than 10 turns → 10 points  \n" +
                "   - If the learner’s level normally implies brief exchanges, do not penalize too harshly for fewer turns.\n" +
                "\n" +
                "5. **Feedback (`description`, ≤100 words):**  \n" +
                "   - Provide 1–2 concrete examples from the learner’s responses (e.g., “You wrote ‘I go store’ without an article”).  \n" +
                "   - Offer concise, level-appropriate tips (e.g., “At " + languageLevel + ", try adding simple connectors such as ‘and’ or ‘but’ to link ideas”).  \n" +
                "   - Focus on the 2–3 most important improvements.\n" +
                "\n" +
                "6. **Transcription Errors:**  \n" +
                "   - If the conversation is transcribed, ignore very minor disfluencies (“um,” “uh”) and focus on the substantive string of words as well as the phonetic meaning.\n" +
                "\n" +
                "7. **Evaluate ONLY the “USER” role.** Do not score the assistant’s lines.\n" +
                "\n" +
                "8. **Scoring:**  \n" +
                "   - Compute `score = (vocabulary + grammar + correctness + purpose + duration) × (100/50)`.  \n" +
                "\n" +
                "9. Forgiving\n" +
                "   - the lower the level of the conversation the more forgiving and generous you have to be with points. This mean being forgiving if they make a small mistake or giving more credit when using more advanced words / sentence contructions.\n" +
                "   - Round to the nearest integer." +
                "10. Language of response has to be: " + settings.getLanguage().getName() + "(iso:"+ settings.getLanguage().getIso() + ")");

//        for(Message message : userConversation.getMessages()){
//            promptBuilder.append(message.getSender() + ":" + message.getText() + "\n");
//        }

        return promptBuilder.toString();
    }

    /**
     * Request an evaluation from the AI
     */
    public Optional<Evaluation> evaluateConversation(UserConversation userConversation) throws IOException {
        // Set up the conversation history in the repository
        ChatGPTService.resetMessageHistory();

        List<Map<String, String>> totalPrompt = new ArrayList<>();

        // Generate the structured evaluation prompt
        String evaluationPrompt = null;
        try {
            evaluationPrompt = generateEvaluationPrompt(userConversation);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //totalPrompt.add(Map.of("role", "developer", "content", evaluationPrompt));
        String header = "Conversation: \n";
        header += "Language: " + userConversation.getConversation().getLanguage().getName()
                + "\n Language level: " + userConversation.getConversation().getLanguageLevel().getName() + "\n"
                + "\n Scenario: " + userConversation.getConversation().getScenario().getDescription()
                + "\n Goals: ";
        for (String keyPoint : userConversation.getConversation().getScenario().getKeyPoints()){
            header += "-" + keyPoint + "\n";
        }
        totalPrompt.add(Map.of("role", "user", "content", header));

        String conversation = "";

        // Load the messages
        for (Message message : userConversation.getMessages()) {
            if(message.getSenderEnum() == Sender.TRANSLATION) continue;
            Map<String, String> messageMap = new HashMap<>();
            conversation += message.getSender() + ":" + message.getText() + "\n";
            //messageMap.put("role", Sender.USER == message.getSenderEnum() ? "user" : "assistant");
            //messageMap.put("content", message.getText());
            //totalPrompt.add(messageMap);
            //ChatGPTRepository.addMessage(messageMap);
        }
        totalPrompt.add(Map.of("role", "user", "content", conversation));

        System.out.println(evaluationPrompt);
        System.out.println("Total Prompt: " + totalPrompt.toString());



        // Request the evaluation
//        String response = ChatGPTRepository.sendMessage(evaluationPrompt,
//            AIModels.EVALUATION);
        //String response = ChatGPTRepository.sendMessage(gson.toJsonTree(totalPrompt).getAsString(), AIModels.EVALUATION);
        Optional<String> response = ChatGPTService.startEvaluation(evaluationPrompt, totalPrompt);
        // Parse the response
        if(response.isPresent()){
            JsonObject evaluationJson = parseEvaluationResponse(response.get());

            System.out.println("Evaluation response: " + evaluationJson.toString());

            // Create and return the evaluation object
            return Optional.of(createEvaluationFromJson(userConversation.getId(), evaluationJson, userConversation));
        }else{
            return Optional.empty();
        }
    }

    /**
     * Parse the evaluation response, handling potential formatting issues
     */
    private JsonObject parseEvaluationResponse(String response) throws IOException {
        // Try to extract JSON using regex pattern
        Pattern jsonPattern = Pattern.compile("\\{[\\s\\S]*?\\}");
        Matcher matcher = jsonPattern.matcher(response);

        if (matcher.find()) {
            String jsonString = matcher.group(0);
            try {
                return ChatGPTService.parseEvaluationResponse(jsonString);
            } catch (Exception e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
            }
        }

        // Fallback: try to parse the whole response
        return ChatGPTService.parseEvaluationResponse(response);
    }

    /**
     * Create an Evaluation object from the parsed JSON
     */
    private Evaluation createEvaluationFromJson(String userConversationId, JsonObject json, UserConversation userConversation) throws IOException {
        int score = getJsonIntValue(json, "score", 0);
        int maxScore = getJsonIntValue(json, "max_score", 100);

        // For backward compatibility, use vocab and grammar if present
        int vocab = getJsonIntValue(json, "vocab", 0);
        if (vocab == 0) {
            vocab = getJsonIntValue(json, "vocabulary", 3);
        }

        int duration = getJsonIntValue(json, "duration", 0);
        if(!userConversation.getMessages().isEmpty()) {
            int count = userConversation.getMessages().size();
            if(count < 4) duration = 4;
            if(count < 8) duration = 8;
            if(count >= 10) duration = 10;
        }

        int grammar = getJsonIntValue(json, "grammar", 3);
        int correctness = getJsonIntValue(json, "correctness", 0);
        int purpose = getJsonIntValue(json, "purpose", 0);
        //int duration = getJsonIntValue(json, "duration", 0);

        String feedback = getJsonStringValue(json, "description", "No feedback provided.");

        // Enhance feedback with scoring details
        //feedback = enhanceFeedback(json, feedback);
        // Map the different scores
        Map<String, Integer> criteriaScores = new HashMap<>();
        criteriaScores.put("vocabulary", vocab);
        criteriaScores.put("grammar", grammar);
        criteriaScores.put("correctness", correctness);
        criteriaScores.put("purpose", purpose);
        criteriaScores.put("duration", duration);

        score = calculateStandardizedScore(criteriaScores);

        return new Evaluation(userConversationId, score, maxScore, vocab, grammar, feedback, correctness, duration, purpose);
    }

    /**
     * Enhance the feedback with scoring details
     */
    private String enhanceFeedback(JsonObject json, String baseFeedback) {
        StringBuilder enhancedFeedback = new StringBuilder(baseFeedback);

        enhancedFeedback.append("\n\n**Detailed Scores:**\n");

        // Add each criterion with its score
        for (String criterion : CRITERIA_WEIGHTS.keySet()) {
            int score = getJsonIntValue(json, criterion, 0);
            if (score > 0) {
                enhancedFeedback.append("- **").append(criterion.substring(0, 1).toUpperCase())
                        .append(criterion.substring(1)).append("**: ")
                        .append(score).append("/5");

                // Add rubric description if available
                if (SCORING_RUBRIC.containsKey(criterion) && score > 0 && score <= 5) {
                    enhancedFeedback.append(" - ").append(SCORING_RUBRIC.get(criterion).get(score - 1));
                }

                enhancedFeedback.append("\n");
            }
        }

        // Calculate weighted score for transparency
        double weightedTotal = 0;
        int criteriaCount = 0;

        for (Map.Entry<String, Double> entry : CRITERIA_WEIGHTS.entrySet()) {
            int score = getJsonIntValue(json, entry.getKey(), 0);
            if (score > 0) {
                weightedTotal += score * entry.getValue();
                criteriaCount++;
            }
        }

        // Only show calculation if we have scores
        if (criteriaCount > 0) {
            double calculatedScore = (weightedTotal / criteriaCount) * 20; // Convert to 100-point scale
            DecimalFormat df = new DecimalFormat("#.##");

            enhancedFeedback.append("\nOverall score: ").append(df.format(calculatedScore))
                    .append(" out of 100 (calculated from weighted criteria)");
        }

        return enhancedFeedback.toString();
    }

    /**
     * Calculate a standardized score based on criteria weights
     */
    public int calculateStandardizedScore(Map<String, Integer> criteriaScores) {
        double weightedTotal = 0;
        double weightsUsed = 0;

        for (Map.Entry<String, Double> entry : CRITERIA_WEIGHTS.entrySet()) {
            String criterion = entry.getKey();
            double weight = entry.getValue();

            if (criteriaScores.containsKey(criterion)) {
                int score = criteriaScores.get(criterion);
                weightedTotal += score * weight;
                weightsUsed += weight;
            }
        }

        // If no weights were used, return 0
        if (weightsUsed == 0) {
            return 0;
        }

        // Normalize to account for missing criteria
        double normalizedScore = weightedTotal / weightsUsed;

        // Convert to 100-point scale (scores are 1-10, so multiply by 10)
        return (int) Math.round(normalizedScore * 10);
    }

    /**
     * Get an integer value from JSON with fallback
     */
    private int getJsonIntValue(JsonObject json, String key, int defaultValue) {
        try {
            if (json.has(key)) {
                return json.get(key).getAsInt();
            }
        } catch (Exception e) {
            System.err.println("Error getting int value for key " + key + ": " + e.getMessage());
        }
        return defaultValue;
    }

    /**
     * Get a string value from JSON with fallback
     */
    private String getJsonStringValue(JsonObject json, String key, String defaultValue) {
        try {
            if (json.has(key)) {
                return json.get(key).getAsString();
            }
        } catch (Exception e) {
            System.err.println("Error getting string value for key " + key + ": " + e.getMessage());
        }
        return defaultValue;
    }

    /**
     * Analyze the vocabulary used in a conversation for additional metrics
     */
    public Map<String, Object> analyzeVocabulary(UserConversation userConversation) {
        Map<String, Object> metrics = new HashMap<>();

        // Extract user messages only
        List<String> userMessages = userConversation.getMessages().stream()
                .filter(m -> "USER".equals(m.getSender()))
                .map(Message::getText)
                .collect(Collectors.toList());

        // Join all messages
        String allText = String.join(" ", userMessages);

        // Split into words and normalize
        String[] words = allText.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .split("\\s+");

        // Word count
        int wordCount = words.length;
        metrics.put("wordCount", wordCount);

        // Unique words
        Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
        int uniqueWordCount = uniqueWords.size();
        metrics.put("uniqueWordCount", uniqueWordCount);

        // Vocabulary diversity (unique/total)
        double diversity = wordCount > 0 ? (double) uniqueWordCount / wordCount : 0;
        metrics.put("vocabularyDiversity", diversity);

        // Word frequency
        Map<String, Long> wordFrequency = Arrays.stream(words)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Most used words (top 10)
        List<Map.Entry<String, Long>> mostUsedWords = wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        metrics.put("mostUsedWords", mostUsedWords);

        return metrics;
    }
}
