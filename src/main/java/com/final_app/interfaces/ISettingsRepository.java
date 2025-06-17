package com.final_app.interfaces;

import com.final_app.models.Settings;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ISettingsRepository {
    CompletableFuture<Void> saveSettings(Settings settings);
    CompletableFuture<Optional<Settings>> getSettingsFromUser(String userId);
}
