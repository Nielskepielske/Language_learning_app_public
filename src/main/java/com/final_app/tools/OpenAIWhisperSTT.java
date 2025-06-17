package com.final_app.tools;

import com.final_app.services.ChatGPTService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class OpenAIWhisperSTT {
    private static final String API_KEY = Dotenv.load().get("OPENAI_API_KEY"); // Zet hier je API-sleutel
    private static final String API_URL = "https://api.openai.com/v1/audio/transcriptions";

    private static OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) {
        String audioFilePath = "recorded_audio.wav"; // Vervang door je audiobestand
        String transcript = transcribeAudio(audioFilePath).get();
        System.out.println("Herkenning: " + transcript);
    }

    public static Optional<String> transcribeAudio(String filePath) {
        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            throw new IllegalArgumentException("Bestand niet gevonden: " + filePath);
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(),
                        RequestBody.create(audioFile, MediaType.parse("audio/mp3")))
                .addFormDataPart("model", "whisper-1")
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                return Optional.of(jsonObject.get("text").getAsString());
            } else {
                //return "Fout bij het verwerken: " + (response.body() != null ? response.body().string() : response.message());
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Transcribe error");
                    alert.setContentText("Something went wrong while trying to transcribe audio");
                    alert.showAndWait();
                });

                return Optional.empty();
            }
        } catch (IOException e) {
            //return "Netwerkfout: " + e.getMessage();
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Transcribe error");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });

            return Optional.empty();
        }
    }

    public static Optional<String> transcribeAudio(String filePath, String language) {
        File audioFile = new File(filePath);
        //System.out.println("Language: " + language);
        if (!audioFile.exists()) {
            throw new IllegalArgumentException("Bestand niet gevonden: " + filePath);
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(),
                        RequestBody.create(audioFile, MediaType.parse("audio/mp3")))
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("language", language)
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                return Optional.of(jsonObject.get("text").getAsString());
            } else {
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Transcribe error");
                    alert.setContentText("Something went wrong while trying to transcribe audio");
                    alert.showAndWait();
                });

                return Optional.empty();
                //return "Fout bij het verwerken: " + (response.body() != null ? response.body().string() : response.message());
            }
        } catch (IOException e) {
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Transcribe error");
                alert.setContentText("Something went wrong trying to connect to a network");
                alert.showAndWait();
            });

            //return "Netwerkfout: " + e.getMessage();
            return Optional.empty();
        }
    }
    public static Optional<String> transcribeAudio(String filePath, String language, String instructions) {
        File audioFile = new File(filePath);
        //System.out.println("Language: " + language);
        if (!audioFile.exists()) {
            throw new IllegalArgumentException("Bestand niet gevonden: " + filePath);
        }

        AtomicReference<String> fullInstructions = new AtomicReference<>("Three");

        try {
            PerformanceTimer.start("translateInstructions");
            ChatGPTService.translateMessage(fullInstructions.get(), "English", "iso: "+ language, "write everything in full. This means no numericals, in english 'five' would stay 'five', and not '5'. Be sure to translate it to the language specified.")
                    .ifPresentOrElse(fullInstructions::set,()->{
                        fullInstructions.set(instructions);
                    });
            PerformanceTimer.stop("translateInstructions");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(),
                        RequestBody.create(audioFile, MediaType.parse("audio/mp3"))) // wav, mp3
                .addFormDataPart("model", "gpt-4o-transcribe")
                .addFormDataPart("language", language)
                .addFormDataPart("prompt", fullInstructions.get())
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        PerformanceTimer.start("transcriptionRequest");
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                PerformanceTimer.stop("transcriptionRequest");
                return Optional.of(jsonObject.get("text").getAsString());
            } else {
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Transcribe error");
                    alert.setContentText("Something went wrong trying to transcribe audio");
                    alert.showAndWait();
                });

                return Optional.empty();
                //return "Fout bij het verwerken: " + (response.body() != null ? response.body().string() : response.message());
            }
        } catch (IOException e) {
            //return "Netwerkfout: " + e.getMessage();
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Transcribe error");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });

            return Optional.empty();
        }
    }
}

