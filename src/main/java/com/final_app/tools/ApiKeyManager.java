package com.final_app.tools;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApiKeyManager {

    private static final String ENV_FILE_NAME = ".env";
    private static final String API_KEY_NAME = "OPENAI_API_KEY";

    /**
     * Tries to load the OpenAI API key using the dotenv-java library.
     * This method is unchanged and works perfectly.
     *
     * @return An Optional containing the API key if found and valid, otherwise an empty Optional.
     */
    public static Optional<String> loadApiKey() {
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            String apiKey = dotenv.get(API_KEY_NAME);
            return Optional.ofNullable(apiKey)
                    .filter(key -> !key.trim().isEmpty() && key.startsWith("sk-"));
        } catch (Exception e) {
            System.err.println("An error occurred while loading the API key: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Saves or updates the API key in the .env file without overwriting other variables.
     *
     * @param apiKey The OpenAI API key to save.
     * @return true if the key was saved successfully, false otherwise.
     */
    public static boolean saveApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Cannot save an empty API key.");
            return false;
        }

        File envFile = new File(ENV_FILE_NAME);
        List<String> lines = new ArrayList<>();
        boolean keyFound = false;
        String newEntry = API_KEY_NAME + "=" + apiKey;

        // Step 1: Read existing lines if the file exists
        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Step 2: Check if the current line is the one we want to update
                    if (line.trim().startsWith(API_KEY_NAME + "=")) {
                        lines.add(newEntry); // Add the updated key
                        keyFound = true;
                    } else {
                        lines.add(line); // Keep the existing line
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading .env file for update: " + e.getMessage());
                return false;
            }
        }

        // Step 3: If the key was not found in the file, add it to the list
        if (!keyFound) {
            lines.add(newEntry);
            System.out.println("API key not found, adding new entry.");
        } else {
            System.out.println("API key found, updating existing entry.");
        }

        // Step 4: Write all lines (original + modified/new) back to the file
        try (PrintWriter writer = new PrintWriter(envFile, "UTF-8")) {
            for (String line : lines) {
                writer.println(line);
            }
            System.out.println("API Key saved successfully to " + ENV_FILE_NAME);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to .env file: " + e.getMessage());
            return false;
        }
    }
}