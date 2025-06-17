package com.final_app.repositories.local;

import com.final_app.db.dao.LanguageDAO;
import com.final_app.db.dao.LanguageLevelDAO;
import com.final_app.db.dao.LanguageLevelSystemDAO;
import com.final_app.interfaces.ILanguageRepository;
import com.final_app.models.Language;
import com.final_app.models.LanguageLevel;
import com.final_app.models.LanguageLevelSystem;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalLanguageRepository implements ILanguageRepository {
    private LanguageDAO languageDAO = new LanguageDAO();
    private LanguageLevelDAO languageLevelDAO = new LanguageLevelDAO();
    private LanguageLevelSystemDAO languageLevelSystemDAO = new LanguageLevelSystemDAO();

    private static LocalLanguageRepository instance = null;

    public static ILanguageRepository getInstance() {
        if(instance == null){
            instance = new LocalLanguageRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addLanguage(Language language) {
        try {
            languageDAO.save(language);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateLanguage(Language language) {
        try {
            languageDAO.save(language);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteLanguage(String id) {
        try {
            languageDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<Language>> getLanguage(Language language) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.of(languageDAO.findById(language.getId()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Language>> getLanguageById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try{
                return Optional.ofNullable(languageDAO.findById(id));
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public CompletableFuture<Optional<Language>> getLanguageByName(String name) {
        return CompletableFuture.supplyAsync(()->{
            try{
                return Optional.ofNullable(languageDAO.findByName(name));
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<Language>> getAllLanguages() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return languageDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> addLanguageLevel(LanguageLevel languageLevel) {
        try {
            languageLevelDAO.save(languageLevel);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateLanguageLevel(LanguageLevel languageLevel) {
        try {
            languageLevelDAO.save(languageLevel);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteLanguageLevel(LanguageLevel languageLevel) {
        try {
            languageLevelDAO.delete(languageLevel.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<LanguageLevel>> getLanguageLevelById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return languageLevelDAO.findById(id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<LanguageLevel>> getLanguageLevelByName(String name) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return languageLevelDAO.findByName(name);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<LanguageLevel>> getAllLanguageLevels() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return languageLevelDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> addLanguageSystem(LanguageLevelSystem languageLevelSystem) {
        try {
            languageLevelSystemDAO.save(languageLevelSystem);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateLanguageSystem(LanguageLevelSystem languageLevelSystem) {
        try {
            languageLevelSystemDAO.update(languageLevelSystem);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteLanguageSystem(LanguageLevelSystem languageLevelSystem) {
        try {
            languageLevelSystemDAO.delete(languageLevelSystem.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<LanguageLevelSystem>> getLanguageLevelSystemById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(languageLevelSystemDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<LanguageLevelSystem>> getLanguageLevelSystemByName(String name) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(languageLevelSystemDAO.findByName(name));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<LanguageLevelSystem>> getAllLanguageSystems() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return languageLevelSystemDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
