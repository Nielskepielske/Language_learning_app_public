package com.final_app.repositories.local;

import com.final_app.db.dao.SpeakingTestDAO;
import com.final_app.interfaces.ISpeakingTestRepository;
import com.final_app.models.SpeakingTest;
import org.checkerframework.checker.units.qual.C;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalSpeakingTestRepository implements ISpeakingTestRepository {
    private SpeakingTestDAO speakingTestDAO = new SpeakingTestDAO();

    private static LocalSpeakingTestRepository instance = null;

    public static ISpeakingTestRepository getInstance() {
        if(instance == null){
            instance = new LocalSpeakingTestRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addSpeakingTest(SpeakingTest speakingTest) {
        try {
            speakingTestDAO.insert(speakingTest);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateSpeakingTest(SpeakingTest speakingTest) {
        try {
            speakingTestDAO.update(speakingTest);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<SpeakingTest>> getSpeakingTestById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(speakingTestDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteSpeakingTestById(String id) {
        try {
            speakingTestDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTest>> getAllSpeakingTests() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return speakingTestDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTest>> getAllSpeakingTestsFromLanguage(String languageId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return speakingTestDAO.findByLanguageId(languageId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTest>> getAllSpeakingTestsFromLevel(String levelId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return speakingTestDAO.findByLevelId(levelId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
