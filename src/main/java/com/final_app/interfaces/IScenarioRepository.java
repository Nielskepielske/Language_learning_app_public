package com.final_app.interfaces;

import com.final_app.models.Scenario;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IScenarioRepository {
    CompletableFuture<Void> addScenario(Scenario scenario);
    CompletableFuture<Void> updateScenario(Scenario scenario);
    CompletableFuture<Optional<Scenario>> getScenarioById(String id);
    CompletableFuture<Void> deleteScenarioById(String id);
    CompletableFuture<Iterable<Scenario>> getAllScenarios();
}
