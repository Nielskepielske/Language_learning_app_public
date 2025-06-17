package com.final_app.repositories.local;

import com.final_app.db.dao.ScenarioDAO;
import com.final_app.interfaces.IScenarioRepository;
import com.final_app.models.Scenario;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalScenarioRepository implements IScenarioRepository {
    private ScenarioDAO scenarioDAO = new ScenarioDAO();

    private static LocalScenarioRepository instance = null;

    public static IScenarioRepository getInstance() {
        if(instance == null){
            instance = new LocalScenarioRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addScenario(Scenario scenario) {
        try {
            scenarioDAO.insert(scenario);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateScenario(Scenario scenario) {
        try {
            scenarioDAO.update(scenario);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<Scenario>> getScenarioById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(scenarioDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteScenarioById(String id) {
        try {
            scenarioDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<Scenario>> getAllScenarios() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return scenarioDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
