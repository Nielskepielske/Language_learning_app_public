package com.final_app.tests;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.Detection; // For language detection
import com.google.auth.oauth2.GoogleCredentials; // If not using env variable
import com.google.auth.oauth2.ServiceAccountCredentials; // If not using env variable

import java.io.FileInputStream; // If not using env variable
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

public class TranslatorTest {

    public static void main(String[] args) {
        // Text to translate
        String textToTranslate = "Hello, world!";
        // Target language (e.g., "es" for Spanish, "fr" for French)
        String targetLanguage = "es";
        // Optional: Source language (if you know it, otherwise Google will detect)
        String sourceLanguage = "en"; // Or null for auto-detection

        try {
            // 1. Initialize Translate service
            // The library will automatically use credentials from the GOOGLE_APPLICATION_CREDENTIALS
            // environment variable if it's set.
            Translate translate = TranslateOptions.getDefaultInstance().getService();

            // Alternative: Explicitly provide credentials (if not using env variable)
            /*
            String credentialsPath = "/path/to/your/keyfile.json";
            GoogleCredentials credentials;
            try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
                credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            }
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(credentials).build();
            Translate translate = translateOptions.getService();
            */


            // 2. Perform Translation
            Translate.TranslateOption srcLangOpt = null;
            if (sourceLanguage != null && !sourceLanguage.isEmpty()) {
                srcLangOpt = Translate.TranslateOption.sourceLanguage(sourceLanguage);
            }
            Translate.TranslateOption targetLangOpt = Translate.TranslateOption.targetLanguage(targetLanguage);

            Translation translation;
            if (srcLangOpt != null) {
                translation = translate.translate(textToTranslate, srcLangOpt, targetLangOpt);
            } else {
                // Let Google detect the source language
                translation = translate.translate(textToTranslate, targetLangOpt);
            }

            // 3. Get the translated text
            String translatedText = translation.getTranslatedText();

            System.out.printf("Original text: %s%n", textToTranslate);
            System.out.printf("Detected source language: %s%n", translation.getSourceLanguage()); // Will be null if you provided source
            System.out.printf("Translated text (%s): %s%n", targetLanguage, translatedText);


            // Example: Language Detection
            Detection detection = translate.detect(textToTranslate);
            String detectedLangCode = detection.getLanguage();
            System.out.printf("Detected language for '%s': %s (Confidence: %.2f)%n",
                    textToTranslate, detectedLangCode, detection.getConfidence());

            // Example: Translating multiple texts (batch)
            List<String> textsToTranslateBatch = Arrays.asList("Good morning", "How are you?");
            List<Translation> translationsBatch = translate.translate(textsToTranslateBatch, targetLangOpt);
            System.out.println("\nBatch Translation:");
            for (int i = 0; i < textsToTranslateBatch.size(); i++) {
                System.out.printf("Original: %s => Translated: %s%n",
                        textsToTranslateBatch.get(i),
                        translationsBatch.get(i).getTranslatedText());
            }


        } catch (Exception e) {
            // This can catch com.google.cloud.GoogleCloudException for API errors
            System.err.println("Error during translation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}