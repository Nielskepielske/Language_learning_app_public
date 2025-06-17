package com.final_app.converters;

import com.final_app.models.Language;
import com.final_app.services.AppService;
import com.final_app.services.LanguageService;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LanguageConverter extends StringConverter<Language> {
    private List<Language> languages;
    private final LanguageService languageService = AppService.getInstance().getLanguageService();
    public LanguageConverter(){
        try {
            languages = languageService.getAllLanguages();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public LanguageConverter(List<Language> languages){
        this.languages = languages;
    }
    @Override
    public String toString(Language language) {
        return (language == null) ? "" : language.getName();
    }

    @Override
    public Language fromString(String s) {
        for(Language l : languages){
            if(l.getName() == s) return l;
        }
        return null;
    }
}
