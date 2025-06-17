package com.final_app.converters;

import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.services.AppService;
import com.final_app.services.LanguageService;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LanguageLevelConverter extends StringConverter<LanguageLevel> {
    private List<LanguageLevel> languageLevels;
    private final LanguageService languageService = AppService.getInstance().getLanguageService();
    public LanguageLevelConverter(){
        try {
            languageLevels = languageService.getAllLanguageLevels();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public LanguageLevelConverter(List<LanguageLevel> languageLevels){
        this.languageLevels = languageLevels;
    }
    @Override
    public String toString(LanguageLevel languageLevel) {
        return (languageLevel == null) ? "" : languageLevel.getName();
    }

    @Override
    public LanguageLevel fromString(String s) {
        for(LanguageLevel l : languageLevels){
            if(l.getName() == s) return l;
        }
        return null;
    }
}
