package com.final_app.repositories.local;

import com.final_app.db.dao.SpeakingTestQuestionDAO;
import com.final_app.interfaces.IQuestionRepository;
import com.final_app.models.SpeakingTestQuestion;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalQuestionRepository implements IQuestionRepository {
    private SpeakingTestQuestionDAO questionDAO = new SpeakingTestQuestionDAO();

    private static LocalQuestionRepository instance = null;

    public static IQuestionRepository getInstance() {
       if(instance == null){
           instance = new LocalQuestionRepository();
       }
       return instance;
    }

    @Override
    public CompletableFuture<Void> addQuestion(SpeakingTestQuestion question) {
        try {
            questionDAO.insert(question);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateQuestion(SpeakingTestQuestion question) {
        try {
            questionDAO.update(question);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<SpeakingTestQuestion>> getQuestionById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(questionDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteQuestionById(String id) {
        try {
            questionDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTestQuestion>> getAllQuestions() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return questionDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterable<SpeakingTestQuestion>> getAllQuestionsFromTest(String testId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return questionDAO.findByTestId(testId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
