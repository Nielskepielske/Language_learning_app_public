package com.final_app.services;

import com.final_app.factories.RepositoryFactory;
import com.final_app.globals.Roles;
import com.final_app.interfaces.IScenarioRepository;
import com.final_app.models.Scenario;
import com.final_app.models.ScenarioKeyPoint;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service class for scenario-related operations
 */
public class ScenarioService {
    /**
     * Add a new scenario to the system
     */
    public Scenario addScenario(String description, Roles role, List<ScenarioKeyPoint> keyPoints) throws SQLException, ExecutionException, InterruptedException {
        Scenario scenario = new Scenario(description, role.name());
        scenario.setKeyPoints(keyPoints.stream().map(p -> p.toString()).collect(Collectors.toList()));
        RepositoryFactory.getScenarioRepository().addScenario(scenario).get(); // Wait for completion
        return scenario;
    }

    /**
     * Get all available scenarios
     */
    public List<Scenario> getAllScenarios() throws SQLException, ExecutionException, InterruptedException {
        Iterable<Scenario> iterableScenarios = RepositoryFactory.getScenarioRepository().getAllScenarios().get();
        return StreamSupport.stream(iterableScenarios.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Get a scenario by ID
     */
    public Scenario getScenarioById(String id) throws SQLException, ExecutionException, InterruptedException {
        return RepositoryFactory.getScenarioRepository().getScenarioById(id).get().orElseThrow();
    }

    /**
     * Update an existing scenario
     */
    public void updateScenario(Scenario scenario) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getScenarioRepository().updateScenario(scenario).get();
    }

    /**
     * Delete a scenario
     */
    public void deleteScenario(String id) throws SQLException, ExecutionException, InterruptedException {
        RepositoryFactory.getScenarioRepository().deleteScenarioById(id).get();
    }
}








