package com.final_app.tests;


import com.final_app.globals.AIModels;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.SpeakingTest;
import com.final_app.services.ChatGPTService;
import com.final_app.services.SpeakingTestService;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SpeakingTestServiceTest {

    private SpeakingTestService speakingTestService;
    private Language language;
    private Language languageFrom;
    private LanguageLevel languageLevel;

    // We still need this for the few tests that check the top-level generateSpeakingTest method
    private MockedStatic<ChatGPTService> mockedChatGPTService;

    @BeforeEach
    void setUp() {
        speakingTestService = new SpeakingTestService();
        mockedChatGPTService = mockStatic(ChatGPTService.class);

        language = new Language("1", "Spanish", "es", "#fff", 1000);
        languageFrom = new Language("2", "English", "en", "#fff", 1000);
        languageLevel = new LanguageLevel("1", "A1", 1000);
    }

    @AfterEach
    void tearDown() {
        mockedChatGPTService.close();
    }

    // --- Tests for the Parsing Logic (parseAndBuildSpeakingTestFromJson) ---

    @Test
    @DisplayName("parseAndBuild should succeed with perfect and complete JSON")
    void parseAndBuild_Success_PerfectJson() {
        // Arrange
        String perfectJson = "{\n" +
                "  \"title\": \"Spanish Greetings\",\n" +
                "  \"description\": \"A test about common greetings.\",\n" +
                "  \"explanation\": \"This lesson covers...\",\n" +
                "  \"grammar_focus\": \"Present Tense\",\n" +
                "  \"vocabulary_theme\": \"greetings\",\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question_text\": \"How do you say 'Hello?'\",\n" +
                "      \"expected_response_pattern\": \"Hola\",\n" +
                "      \"expected_response_language_iso\": \"es\",\n" +
                "      \"required_vocabulary\": [\"hola\"],\n" +
                "      \"difficulty_level\": 1,\n" +
                "      \"order_index\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Act
        SpeakingTest result = speakingTestService.parseAndBuildSpeakingTestFromJson(perfectJson, language, languageFrom, languageLevel);

        // Assert
        assertNotNull(result);
        assertEquals("Spanish Greetings", result.getTitle());
        assertEquals(1, result.getQuestions().size());
        assertEquals("How do you say 'Hello?'", result.getQuestions().get(0).getQuestionText());
    }

    @Test
    @DisplayName("parseAndBuild should fail on malformed JSON syntax")
    void parseAndBuild_Failure_MalformedJsonSyntax() {
        // Arrange: JSON with a missing comma
        String malformedJson = "{\"title\": \"Bad JSON\" \"description\": \"...\"}";

        // Act & Assert
        // The service now wraps the JsonSyntaxException in an IllegalArgumentException, so we expect that instead.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> { // <--- CORRECTED LINE
            speakingTestService.parseAndBuildSpeakingTestFromJson(malformedJson, language, languageFrom, languageLevel);
        });

        // Optional: Add a more specific assertion to ensure it's the right kind of error.
        assertTrue(exception.getMessage().contains("malformed JSON"));
    }

    @Test
    @DisplayName("parseAndBuild should fail if a top-level required field is missing")
    void parseAndBuild_Failure_MissingTopLevelField() {
        // Arrange: JSON missing the "title" field
        String incompleteJson = "{\"description\": \"...\", \"explanation\": \"...\", \"grammar_focus\": \"...\", \"vocabulary_theme\": \"...\", \"questions\": []}";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            speakingTestService.parseAndBuildSpeakingTestFromJson(incompleteJson, language, languageFrom, languageLevel);
        });
        assertTrue(exception.getMessage().contains("Missing required field: 'title'"));
    }

    @Test
    @DisplayName("parseAndBuild should succeed if 'questions' array is present but empty")
    void parseAndBuild_Success_EmptyQuestionsArray() {
        // Arrange
        String jsonWithEmptyQuestions = "{\n" +
                "  \"title\": \"Empty Test\",\n" +
                "  \"description\": \"A test with no questions.\",\n" +
                "  \"explanation\": \"...\",\n" +
                "  \"grammar_focus\": \"...\",\n" +
                "  \"vocabulary_theme\": \"...\",\n" +
                "  \"questions\": []\n" +
                "}";

        // Act
        SpeakingTest result = speakingTestService.parseAndBuildSpeakingTestFromJson(jsonWithEmptyQuestions, language, languageFrom, languageLevel);

        // Assert
        assertNotNull(result);
        assertEquals("Empty Test", result.getTitle());
        assertTrue(result.getQuestions().isEmpty(), "The questions list should be empty.");
    }

    @Test
    @DisplayName("!!! CORE TEST: parseAndBuild should FAIL if even ONE question is invalid")
    void parseAndBuild_Failure_OneInvalidQuestionInArray() {
        // Arrange: A response where one question is valid, but the other is missing a required field.
        String jsonWithOneBadQuestion = "{\n" +
                "  \"title\": \"Mixed Quality Test\",\n" +
                "  \"description\": \"...\", \"explanation\": \"...\", \"grammar_focus\": \"...\", \"vocabulary_theme\": \"...\",\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question_text\": \"This is a good question.\",\n" +
                "      \"expected_response_pattern\": \"...\", \"expected_response_language_iso\": \"es\", \"required_vocabulary\": [], \"difficulty_level\": 5, \"order_index\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"expected_response_pattern\": \"This question is missing its text.\",\n" + // Missing "question_text"
                "      \"expected_response_language_iso\": \"es\", \"required_vocabulary\": [], \"difficulty_level\": 3, \"order_index\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Act & Assert: The entire method should now throw an exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            speakingTestService.parseAndBuildSpeakingTestFromJson(jsonWithOneBadQuestion, language, languageFrom, languageLevel);
        });

        // Assert that the exception message is detailed and helpful
        String message = exception.getMessage();
        assertTrue(message.contains("Failed to generate test due to invalid data"));
        assertTrue(message.contains("Missing required field: 'question_text'"), "The error message should pinpoint the missing field.");
    }

    // --- Tests for the Orchestrator Method (generateSpeakingTest) ---

//    @Test
//    @DisplayName("generateSpeakingTest should succeed when AI provides valid JSON")
//    void generateSpeakingTest_Success_ValidAIResponse() throws IOException {
//        // Arrange: A perfect JSON response
//        String validJsonResponse = "{\"title\":\"Valid Test\",\"description\":\"...\",\"explanation\":\"...\",\"grammar_focus\":\"...\",\"vocabulary_theme\":\"...\",\"questions\":[]}";
//        when(ChatGPTService.sendMessage(anyString(), any(AIModels.class))).thenReturn(Optional.of(validJsonResponse));
//
//        // Act
//        Optional<SpeakingTest> result = speakingTestService.generateSpeakingTest("Title", "Desc", language, languageFrom, languageLevel);
//
//        // Assert
//        assertTrue(result.isPresent());
//        assertEquals("Valid Test", result.get().getTitle());
//    }

//    @Test
//    @DisplayName("generateSpeakingTest should return empty Optional when AI gives no response")
//    void generateSpeakingTest_Failure_EmptyAIResponse() throws IOException {
//        // Arrange
//        when(ChatGPTService.sendMessage(anyString(), any(AIModels.class))).thenReturn(Optional.empty());
//
//        // Act
//        Optional<SpeakingTest> result = speakingTestService.generateSpeakingTest("Title", "Desc", language, languageFrom, languageLevel);
//
//        // Assert
//        assertFalse(result.isPresent());
//    }

//    @Test
//    @DisplayName("generateSpeakingTest should propagate exception when AI gives invalid JSON")
//    void generateSpeakingTest_Failure_PropagatesParsingException() throws IOException {
//        // Arrange: An invalid JSON response from the AI
//        String invalidJsonResponse = "{\"title\":\"This JSON is broken,}";
//        when(ChatGPTService.sendMessage(anyString(), any(AIModels.class))).thenReturn(Optional.of(invalidJsonResponse));
//
//        // Act & Assert: The exception from the parser should bubble up through this method.
//        assertThrows(JsonSyntaxException.class, () -> {
//            speakingTestService.generateSpeakingTest("Title", "Desc", language, languageFrom, languageLevel);
//        });
//    }
}
