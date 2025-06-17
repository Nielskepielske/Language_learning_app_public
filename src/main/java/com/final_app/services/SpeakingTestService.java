package com.final_app.services;

import com.final_app.factories.RepositoryFactory;
import com.final_app.globals.AIModels;
import com.final_app.models.*;
import com.google.gson.*;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service class for speaking test-related operations
 */
public class SpeakingTestService {
    // Base XP amounts
    private static final int BASE_XP_TEST_COMPLETION = 100;
    private static final int XP_PER_GRAMMAR_POINT = 5;
    private static final int XP_PER_VOCABULARY_POINT = 5;
    private static final int PERFECT_SCORE_BONUS = 50;

    /**
     * Create a new speaking test
     */
    public SpeakingTest createSpeakingTest(String title, String description, String languageId,
                                           String levelId, String grammarFocus, String vocabularyTheme,
                                           List<SpeakingTestQuestion> questions) throws SQLException, ExecutionException, InterruptedException {
        SpeakingTest test = new SpeakingTest();
        test.setTitle(title);
        test.setDescription(description);
        test.setLanguageId(languageId);
        test.setLevelId(levelId);
        test.setGrammarFocus(grammarFocus);
        test.setVocabularyTheme(vocabularyTheme);

        if (questions != null) {
            test.setQuestions(questions);
        }

        RepositoryFactory.getSpeakingTestRepository().addSpeakingTest(test).get();
        return test;
    }

    public SpeakingTest createSpeakingTest(SpeakingTest speakingTest) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getSpeakingTestRepository().addSpeakingTest(speakingTest).get();
        return speakingTest;
    }

    /**
     * Get a speaking test by ID
     */
    public SpeakingTest getSpeakingTestById(String id) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getSpeakingTestRepository().getSpeakingTestById(id).get().orElse(null);
    }

    /**
     * Get all speaking tests
     */
    public List<SpeakingTest> getAllSpeakingTests() throws SQLException, ExecutionException, InterruptedException {
        Iterable<SpeakingTest> iterableTests = RepositoryFactory.getSpeakingTestRepository().getAllSpeakingTests().get();
        return StreamSupport.stream(iterableTests.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get speaking tests by language
     */
    public List<SpeakingTest> getSpeakingTestsByLanguage(String languageId) throws SQLException, ExecutionException, InterruptedException {
        Iterable<SpeakingTest> iterableTests = RepositoryFactory.getSpeakingTestRepository().getAllSpeakingTestsFromLanguage(languageId).get();
        return StreamSupport.stream(iterableTests.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get speaking tests by level
     */
    public List<SpeakingTest> getSpeakingTestsByLevel(String levelId) throws SQLException, ExecutionException, InterruptedException {
        Iterable<SpeakingTest> iterableTests = RepositoryFactory.getSpeakingTestRepository().getAllSpeakingTestsFromLevel(levelId).get();
        return StreamSupport.stream(iterableTests.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Start a speaking test for a user
     */
    public UserSpeakingTest startSpeakingTest(String userId, String testId) throws SQLException, ExecutionException, InterruptedException {
        // Check if user already has this test in progress
        UserSpeakingTest existingTest = RepositoryFactory.getUserSpeakingTestRepository().getUserSpeakingTestByUserIdAndTestId(userId, testId).get().orElse(null);
        if (existingTest != null && !existingTest.getStatus().equals("COMPLETED")) {
            return existingTest;
        }

        // Create new user speaking test
        UserSpeakingTest userTest = new UserSpeakingTest();
        userTest.setUserId(userId);
        userTest.setTestId(testId);
        userTest.setStartedAt(Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        userTest.setStatus("IN_PROGRESS");
        userTest.setScore(0);

        RepositoryFactory.getUserSpeakingTestRepository().addUserSpeakingTest(userTest).get();
        return RepositoryFactory.getUserSpeakingTestRepository().getUserSpeakingTestById(userTest.getId()).get().orElse(null);
    }

    /**
     * Submit a response for a speaking test question
     */
    public UserSpeakingTestResponse submitResponse(String userTestId, String questionId, int questionIndex,
                                                   String transcribedText) throws SQLException, ExecutionException, InterruptedException {
        UserSpeakingTest userTest = RepositoryFactory.getUserSpeakingTestRepository().getUserSpeakingTestById(userTestId).get().orElse(null);
        if (userTest == null) {
            throw new IllegalArgumentException("User test not found");
        }

        SpeakingTestQuestion question = RepositoryFactory.getQuestionRepository().getQuestionById(questionId).get().orElse(null);
        if (question == null) {
            throw new IllegalArgumentException("Question not found");
        }

        // Create the response
        UserSpeakingTestResponse response = new UserSpeakingTestResponse();
        response.setUserSpeakingTestId(userTestId);
        response.setQuestionId(questionId);
        response.setQuestionIndex(questionIndex);
        response.setTranscribedText(transcribedText);
        response.setRespondedAt(Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        // Evaluate the response
        evaluateResponse(response, question, userTest.getTest().getLanguage());

        // Check if answer already exists
        UserSpeakingTestResponse existing = StreamSupport.stream(RepositoryFactory.getResponseRepository().getAllResponsesFromUserTest(userTestId).get().spliterator(), false).filter(res -> res.getQuestion().getId().equals(questionId)).findAny().orElse(null);
        if (existing != null) {
            response.setId(existing.getId());
            RepositoryFactory.getResponseRepository().updateResponse(response).get();
        } else {
            // Save the response
            RepositoryFactory.getResponseRepository().addResponse(response).get();
        }

        // Check if all questions have been answered
        List<UserSpeakingTestResponse> responses = StreamSupport.stream(RepositoryFactory.getResponseRepository().getAllResponsesFromUserTest(userTestId).get().spliterator(), false).collect(Collectors.toList());
        List<SpeakingTestQuestion> questions = StreamSupport.stream(RepositoryFactory.getQuestionRepository().getAllQuestionsFromTest(userTest.getTestId()).get().spliterator(), false).collect(Collectors.toList());

        if (responses.size() >= questions.size()) {
            completeTest(userTest);
        }

        return response;
    }

    /**
     * Evaluate a response to a speaking test question
     */
    private void evaluateResponse(UserSpeakingTestResponse response, SpeakingTestQuestion question, Language language) throws ExecutionException, InterruptedException {
        try {
            // Prepare AI evaluation prompt
            String evaluationPrompt = createEvaluationPrompt(response, question, language);

            // Send to AI for evaluation
            Optional<String> aiResponse = ChatGPTService.sendMessage(evaluationPrompt, AIModels.EVALUATION);

            if(aiResponse.isPresent()) {
                System.out.println("AI evaluation: " + aiResponse);

                // Parse AI response to extract scores and feedback
                Map<String, Object> evaluationResults = parseAIEvaluation(aiResponse.get(), question);

                // Update response with evaluation results
                response.setGrammarScore((Integer) evaluationResults.getOrDefault("grammarScore", 0));
                response.setVocabularyScore((Integer) evaluationResults.getOrDefault("vocabularyScore", 0));
                response.setOverallScore((Integer) evaluationResults.getOrDefault("overallScore", 0));
                response.setFeedback((String) evaluationResults.getOrDefault("feedback", ""));

                @SuppressWarnings("unchecked")
                Map<String, Boolean> grammarRules = (Map<String, Boolean>) evaluationResults.getOrDefault("grammarRules", new HashMap<>());
                response.setGrammarRulesCorrect(grammarRules);

                @SuppressWarnings("unchecked")
                Map<String, Boolean> vocabUsage = (Map<String, Boolean>) evaluationResults.getOrDefault("vocabularyUsed", new HashMap<>());
                response.setRequiredVocabularyUsed(vocabUsage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to basic evaluation
            response.setGrammarScore(1);
            response.setVocabularyScore(1);
            response.setOverallScore(1);
            response.setFeedback("Evaluation failed: " + e.getMessage());
        }
    }

    /**
     * Create an AI evaluation prompt for a response
     */
    private String createEvaluationPrompt(UserSpeakingTestResponse response, SpeakingTestQuestion question, Language language) {
        try {
            Settings settings = AppService.getInstance().getUserService().getUserSettings(AppService.getInstance().getCurrentUser().getId()).join();
            return "You are a professional language teacher and evaluator for " + language.getName() + ".\n\n" +
                    "Question: \"" + question.getQuestionText() + "\"\n\n" +
                    "Expected response pattern: \"" + question.getExpectedResponsePattern() + "\"\n\n" +
                    "Required vocabulary: " + String.join(", ", question.getRequiredVocabulary()) + "\n\n" +
                    "User's transcribed response: \"" + response.getTranscribedText() + "\"\n\n" +
                    "Please evaluate the response on the following criteria:\n" +
                    "1. Grammar usage (score 1-5)\n" +
                    "2. Vocabulary usage (score 1-5)\n" +
                    "3. Overall quality (score 1-5)\n\n" +
                    "IMPORTANT because these responses where spoken in, only compare the fonetic response!\n" +
                    "Provide detailed feedback identifying correct and incorrect usage, and suggestions for improvement.\n\n" +
                    "The language of the feedback should be " + settings.getLanguage().getName() + "(iso: " + settings.getLanguage().getIso() + ")\n" +
                    "Return your evaluation in the following JSON format:\n" +
                    "{\n" +
                    "  \"grammarScore\": [1-5],\n" +
                    "  \"vocabularyScore\": [1-5],\n" +
                    "  \"overallScore\": [1-5],\n" +
                    "  \"feedback\": \"detailed feedback...\",\n" +
                    "  \"grammarRules\": {\n" +
                    "    \"rule1\": true/false,\n" +
                    "    \"rule2\": true/false\n" +
                    "  },\n" +
                    "  \"vocabularyUsed\": {\n" +
                    "    \"word1\": true/false,\n" +
                    "    \"word2\": true/false\n" +
                    "  }\n" +
                    "}";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * Parse AI evaluation response to extract scores and feedback
     */
    private Map<String, Object> parseAIEvaluation(String aiResponse, SpeakingTestQuestion question) {
        Map<String, Object> result = new HashMap<>();

        // Default values
        result.put("grammarScore", 3);
        result.put("vocabularyScore", 3);
        result.put("overallScore", 3);
        result.put("feedback", "The response was evaluated.");

        Map<String, Boolean> grammarRules = new HashMap<>();
        Map<String, Boolean> vocabularyUsed = new HashMap<>();

        // Extract JSON from AI response
        int jsonStart = aiResponse.indexOf('{');
        int jsonEnd = aiResponse.lastIndexOf('}');

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            try {
                String jsonStr = aiResponse.substring(jsonStart, jsonEnd + 1);
                // Use your existing JSON parsing logic here
                JsonObject jsonObject = JsonParser.parseString(aiResponse).getAsJsonObject();
                result.put("vocabularyScore", jsonObject.get("vocabularyScore").getAsInt());
                result.put("grammarScore", jsonObject.get("grammarScore").getAsInt());
                result.put("overallScore", jsonObject.get("overallScore").getAsInt());
                result.put("feedback", jsonObject.get("feedback").getAsString());
                // For now, we'll just set some default values

                // Initialize vocabulary usage for all required words
                if (question.getRequiredVocabulary() != null) {
                    for (String word : question.getRequiredVocabulary()) {
                        // Simple check if word is in response
                        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
                        boolean used = pattern.matcher(aiResponse).find();
                        vocabularyUsed.put(word, used);
                    }
                }

                result.put("vocabularyUsed", vocabularyUsed);
                result.put("grammarRules", grammarRules);

            } catch (Exception e) {
                e.printStackTrace();
                // Keep default values
            }
        }

        return result;
    }

    /**
     * Complete a user speaking test and calculate final score
     */
    private void completeTest(UserSpeakingTest userTest) throws SQLException, ExecutionException, InterruptedException {
        // Load all responses
        List<UserSpeakingTestResponse> responses = StreamSupport.stream(RepositoryFactory.getResponseRepository().getAllResponsesFromUserTest(userTest.getTestId()).get().spliterator(), false).collect(Collectors.toList());

        // Calculate overall score (average of response scores)
        int totalScore = 0;
        int grammarTotal = 0;
        int vocabTotal = 0;

        for (UserSpeakingTestResponse response : responses) {
            totalScore += response.getOverallScore();
            grammarTotal += response.getGrammarScore();
            vocabTotal += response.getVocabularyScore();
        }

        int avgScore = responses.isEmpty() ? 0 : totalScore / responses.size();
        int avgGrammar = responses.isEmpty() ? 0 : grammarTotal / responses.size();
        int avgVocab = responses.isEmpty() ? 0 : vocabTotal / responses.size();

        // Update user test with final score
        userTest.setScore(avgScore);
        userTest.setCompletedAt(Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        userTest.setStatus("COMPLETED");

        RepositoryFactory.getUserSpeakingTestRepository().updateUserSpeakingTest(userTest).get();

        // Award XP
        int xpEarned = calculateXpForTest(avgScore, avgGrammar, avgVocab);

        AppService.getInstance().getUserService().addXp(userTest.getUserId(), xpEarned, "SPEAKING_TEST",
                "Completed speaking test: " + userTest.getTest().getTitle());

        // Add XP to the specific language
        UserLanguage userLanguage = AppService.getInstance().getLanguageService().getUserLanguage(
                userTest.getUserId(), userTest.getTest().getLanguageId());

        if (userLanguage != null) {
            AppService.getInstance().getLanguageService().addLanguageXp(userLanguage.getId(), xpEarned);
        }
    }

    /**
     * Calculate XP reward for completing a speaking test
     */
    private int calculateXpForTest(int overallScore, int grammarScore, int vocabScore) {
        int xp = BASE_XP_TEST_COMPLETION;

        // Add points for grammar and vocabulary scores
        xp += grammarScore * XP_PER_GRAMMAR_POINT;
        xp += vocabScore * XP_PER_VOCABULARY_POINT;

        // Perfect score bonus
        if (overallScore >= 5 && grammarScore >= 5 && vocabScore >= 5) {
            xp += PERFECT_SCORE_BONUS;
        }

        return xp;
    }

    /**
     * Get all user speaking tests for a user
     */
    public List<UserSpeakingTest> getUserSpeakingTests(String userId) throws SQLException, ExecutionException, InterruptedException {
        Iterable<UserSpeakingTest> iterableTests = RepositoryFactory.getUserSpeakingTestRepository().getAllUserSpeakingTestsFromUser(userId).get();
        return StreamSupport.stream(iterableTests.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get a user speaking test by ID
     */
    public UserSpeakingTest getUserSpeakingTestById(String id) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getUserSpeakingTestRepository().getUserSpeakingTestById(id).get().orElse(null);
    }

    /**
     * Get completed user speaking tests for a user
     */
    public List<UserSpeakingTest> getCompletedUserSpeakingTests(String userId) throws SQLException, ExecutionException, InterruptedException {
        List<UserSpeakingTest> allTests = StreamSupport.stream(RepositoryFactory.getUserSpeakingTestRepository().getAllUserSpeakingTestsFromUser(userId).get().spliterator(), false).collect(Collectors.toList());
        return allTests.stream()
                .filter(test -> "COMPLETED".equals(test.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Get in-progress user speaking tests for a user
     */
    public List<UserSpeakingTest> getInProgressUserSpeakingTests(String userId) throws SQLException, ExecutionException, InterruptedException {
        List<UserSpeakingTest> allTests = StreamSupport.stream(RepositoryFactory.getUserSpeakingTestRepository().getAllUserSpeakingTestsFromUser(userId).get().spliterator(), false).collect(Collectors.toList());
        return allTests.stream()
                .filter(test -> "IN_PROGRESS".equals(test.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Generates a speaking test by calling the AI service and then parsing the response.
     * This method handles the communication with the AI and delegates parsing.
     *
     * @param title The title for the test.
     * @param description The description for the test.
     * @param language The language the test is for (e.g., Spanish).
     * @param languageFrom The language the instructions should be in (e.g., English).
     * @param languageLevel The proficiency level for the test.
     * @return An Optional containing the fully formed SpeakingTest if successful.
     *         An empty Optional if the AI service returns no response.
     * @throws IOException if there is a network or communication error with the AI service.
     * @throws IllegalArgumentException if the AI returns a response that is malformed or invalid.
     */
    public Optional<SpeakingTest> generateSpeakingTest(String title, String description, Language language, Language languageFrom, LanguageLevel languageLevel) throws IOException, IllegalArgumentException {
        // 1. --- Input Validation ---
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (language == null) {
            throw new IllegalArgumentException("Language cannot be null");
        }
        if(languageFrom == null) {
            throw new IllegalArgumentException("Language from cannot be null");
        }
        if (languageLevel == null) {
            throw new IllegalArgumentException("Language level cannot be null");
        }

        // 2. --- Prompt Construction ---
        String baseLanguage = languageFrom.getName();
        StringBuilder promptBuilder = new StringBuilder()
                .append("You are a teacher in ").append(language.getName()).append(".\n")
                .append("I want you to generate a test based on a given title, description, and a level of proficiency in the language.\n")
                .append("You first generate a brief explanation of the materials you will go about. You explain grammar or necessary vocabulary rules as well.\n")
                .append("Then you will generate a couple of questions, the amount may vary between 10 and 20.\n")
                .append("IMPORTANT!! ALWAYS use single quotes in your response. Only use double quotes if it is for the entire string of a variable. \n")
                .append("The language of the explanation will be in: ").append(baseLanguage).append("\n")
                .append("The language of the questions will be in: ").append(baseLanguage).append("\n")
                .append("The given subject for the lesson is: ").append(title).append("\n")
                .append("the given description for the lesson is: ").append(description).append("\n")
                .append("The language levels of the explanation and questions have to stay within the criteria: ")
                .append(languageLevel.getName()).append(" in the grading system of ")
                .append(language.getLanguageLevelSystem().getName()).append(".\n")
                .append("IMPORTANT!! You will return the asked data in JSON format with given parameters:\n")
                .append("title: the title of the test/lesson\n")
                .append("description: the description of the test/lesson, the language of this will be in " + baseLanguage + "\n")
                .append("explanation: the explanation of the materials which we will go over in the test. In this explanation you teach the user about the concept of grammar and vocabulary used in this lesson.The explanation can be very thorough.\n")
                .append("grammar_focus: what kind of things are we looking out for in this lesson\n")
                .append("vocabulary_theme: which words do we explicitly want to use in this test as a string(word1, word2, ...),\n")
                .append("questions: and array of objects that is in following format:\n")
                .append("{\n")
                .append("question_text: the question itself,\n")
                .append("expected_response_pattern: what it would expect the answer to be,\n")
                .append("expected_response_language_iso: the iso of the language of the expected response,\n")
                .append("required_vocabulary: vocabulary that must be used in here, this is an array of strings [word1, word2, ...],\n")
                .append("difficulty_level: how difficult is the questions integer (0-10),\n")
                .append("order_index: what is the index of the question\n")
                .append("}\n");

        ChatGPTService.resetMessageHistory();

        // 3. --- AI Service Call ---
        Optional<String> aiResponse = ChatGPTService.sendMessage(promptBuilder.toString(), AIModels.CONVERSATION);

        // 4. --- Delegation and Return ---
        if (aiResponse.isPresent()) {
            // If we get a response, delegate to the robust parser.
            // If parsing fails, it will throw an exception that propagates up to the UI controller.
            SpeakingTest speakingTest = parseAndBuildSpeakingTestFromJson(aiResponse.get(), language, languageFrom, languageLevel);
            return Optional.of(speakingTest);
        } else {
            // If the AI returns nothing, we return an empty optional.
            // The UI controller should interpret this as a simple failure.
            return Optional.empty();
        }
    }


    /**
     * Parses a raw JSON string from the AI into a SpeakingTest object.
     * This method contains the core "Fail Fast" logic, collecting all errors before failing.
     *
     * @throws IllegalArgumentException if the JSON is malformed, missing required fields,
     * or contains any invalid question objects.
     */
    public SpeakingTest parseAndBuildSpeakingTestFromJson(String aiResponse, Language language, Language languageFrom, LanguageLevel languageLevel) throws IllegalArgumentException {
        String jsonString = extractJsonFromResponse(aiResponse);

        Gson gson = new GsonBuilder().setLenient().create();
        JsonObject jsonObject;
        try {
            jsonObject = gson.fromJson(jsonString, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("AI response contained malformed JSON. " + e.getMessage(), e);
        }

        // --- Validate top-level fields ---
        validateJsonField(jsonObject, "title");
        validateJsonField(jsonObject, "description");
        validateJsonField(jsonObject, "explanation");
        validateJsonField(jsonObject, "grammar_focus");
        validateJsonField(jsonObject, "vocabulary_theme");
        validateJsonField(jsonObject, "questions");

        // --- Construct the main SpeakingTest object ---
        SpeakingTest speakingTest = new SpeakingTest(
                jsonObject.get("title").getAsString(),
                jsonObject.get("description").getAsString(),
                jsonObject.get("explanation").getAsString(),
                language.getId(),
                languageLevel.getId(),
                jsonObject.get("grammar_focus").getAsString(),
                jsonObject.get("vocabulary_theme").getAsString()
        );
        speakingTest.setLanguageFrom(languageFrom);
        speakingTest.setLanguage(language);
        speakingTest.setLanguageLevel(languageLevel);

        // --- Parse questions with robust error collection ---
        List<SpeakingTestQuestion> speakingTestQuestions = new ArrayList<>();
        JsonArray questionsArray = jsonObject.getAsJsonArray("questions");
        List<String> parsingErrors = new ArrayList<>();

        int index = 0;
        for (JsonElement questionElement : questionsArray) {
            if (!questionElement.isJsonObject()) {
                parsingErrors.add(String.format("Item at index %d in 'questions' array was not a valid JSON object.", index));
                index++;
                continue;
            }
            JsonObject questionJson = questionElement.getAsJsonObject();
            try {
                // Validate all required fields for a question
                validateJsonField(questionJson, "question_text");
                validateJsonField(questionJson, "expected_response_pattern");
                validateJsonField(questionJson, "expected_response_language_iso");
                validateJsonField(questionJson, "required_vocabulary");
                validateJsonField(questionJson, "difficulty_level");
                validateJsonField(questionJson, "order_index");

                // Parse vocabulary array safely
                List<String> requiredVocabulary = new ArrayList<>();
                JsonArray vocabArray = questionJson.getAsJsonArray("required_vocabulary");
                for (JsonElement vocabElement : vocabArray) {
                    if (vocabElement.isJsonPrimitive()) {
                        requiredVocabulary.add(vocabElement.getAsString());
                    }
                }

                // Construct the question object
                SpeakingTestQuestion speakingTestQuestion = new SpeakingTestQuestion(
                        null, // ID will be set by DAO
                        questionJson.get("question_text").getAsString(),
                        questionJson.get("expected_response_pattern").getAsString(),
                        questionJson.get("expected_response_language_iso").getAsString(),
                        requiredVocabulary,
                        questionJson.get("difficulty_level").getAsInt(),
                        questionJson.get("order_index").getAsInt()
                );
                speakingTestQuestions.add(speakingTestQuestion);

            } catch (Exception e) {
                // If any part of the validation or creation fails, add a detailed error message.
                String error = String.format("Error in question at index %d [%s]: %s", index, questionJson, e.getMessage());
                parsingErrors.add(error);
            }
            index++;
        }

        // --- Final check: If there were ANY errors, fail the entire process ---
        if (!parsingErrors.isEmpty()) {
            String allErrors = String.join("\n- ", parsingErrors);
            throw new IllegalArgumentException("Failed to generate test due to invalid data in the 'questions' array.\n\nDetails:\n- " + allErrors);
        }

        // Optional check: Ensure we don't end up with an empty test if the AI returned an empty array
        if (speakingTestQuestions.isEmpty() && questionsArray.size() > 0) {
            throw new IllegalArgumentException("AI response contained a 'questions' array, but all items within it were invalid.");
        } else if (speakingTestQuestions.isEmpty()) {
            // You might want to consider this an error too.
            // For now, we'll allow an empty test if the AI explicitly provides an empty questions array.
            System.out.println("Warning: Generated a test with zero questions.");
        }

        speakingTest.setQuestions(speakingTestQuestions);
        return speakingTest;
    }


    /**
     * Extracts a JSON object from a text response that might contain additional markdown.
     */
    private String extractJsonFromResponse(String response) {
        // Find the first '{' and last '}' that should enclose the JSON object
        int startIdx = response.indexOf('{');
        int endIdx = response.lastIndexOf('}');

        if (startIdx == -1 || endIdx == -1 || endIdx <= startIdx) {
            throw new IllegalArgumentException("No valid JSON object found in the AI response.");
        }
        return response.substring(startIdx, endIdx + 1);
    }

    /**
     * Validates that a required field exists in a JsonObject.
     * @throws IllegalArgumentException if the field is missing.
     */
    private void validateJsonField(JsonObject json, String fieldName) {
        if (!json.has(fieldName) || json.get(fieldName).isJsonNull()) {
            throw new IllegalArgumentException("Missing required field: '" + fieldName + "'");
        }
    }
}