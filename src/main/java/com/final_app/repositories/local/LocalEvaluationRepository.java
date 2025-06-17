package com.final_app.repositories.local;

import com.final_app.db.dao.EvaluationDAO;
import com.final_app.interfaces.IEvaluationRepository;
import com.final_app.models.Evaluation;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalEvaluationRepository implements IEvaluationRepository {
    private EvaluationDAO evaluationDAO = new EvaluationDAO();

    private static LocalEvaluationRepository instance = null;

    public static IEvaluationRepository getInstance() {
        if(instance == null){
            instance = new LocalEvaluationRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<Void> addEvaluation(Evaluation evaluation) {
        try {
            evaluationDAO.insert(evaluation);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateEvaluation(Evaluation evaluation) {
        try {
            evaluationDAO.update(evaluation);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<Evaluation>> getEvaluationById(String id) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(evaluationDAO.findById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Evaluation>> getEvaluationByUserConversationId(String userConversationId) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Optional.ofNullable(evaluationDAO.findByUserConversationId(userConversationId));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteEvaluationById(String id) {
        try {
            evaluationDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Iterable<Evaluation>> getAllEvaluations() {
        return CompletableFuture.supplyAsync(()->{
            try {
                return evaluationDAO.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
