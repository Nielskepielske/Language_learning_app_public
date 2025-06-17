package com.final_app.converters;

import com.final_app.models.LanguageLevel;
import com.final_app.models.Scenario;
import com.final_app.services.AppService;
import com.final_app.services.LanguageService;
import com.final_app.services.ScenarioService;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScenarioConverter extends StringConverter<Scenario> {
    private List<Scenario> scenarios;
    private final ScenarioService scenarioService = AppService.getInstance().getScenarioService();
    public ScenarioConverter(){
        try {
            scenarios = scenarioService.getAllScenarios();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public ScenarioConverter(List<Scenario> scenarios){
        this.scenarios = scenarios;
    }
    @Override
    public String toString(Scenario scenario) {
        return (scenario == null) ? "" : scenario.getDescription();
    }

    @Override
    public Scenario fromString(String s) {
        for(Scenario sc : scenarios){
            if(sc.getDescription() == s) return sc;
        }
        return null;
    }
}
