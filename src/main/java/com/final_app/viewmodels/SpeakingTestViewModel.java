package com.final_app.viewmodels;

import com.final_app.models.SpeakingTest;
import com.final_app.models.SpeakingTestQuestion;
import com.final_app.models.UserSpeakingTest;
import com.final_app.models.UserSpeakingTestResponse;
import com.final_app.services.AppService;
import com.final_app.services.SpeakingTestService;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SpeakingTestViewModel implements ViewModel {
    private UserSpeakingTest userSpeakingTest = new UserSpeakingTest();

    private List<UserSpeakingTestResponse> userSpeakingTestResponses = new ArrayList<>();


    private final AppService appService = AppService.getInstance();
    private final SpeakingTestService speakingTestService = appService.getSpeakingTestService();


    public IntegerProperty questionIndex = new SimpleIntegerProperty(0);

    public void setUserSpeakingTest(UserSpeakingTest userSpeakingTest) {
        this.userSpeakingTest = userSpeakingTest;
    }
    public UserSpeakingTest getUserSpeakingTest(){
        return userSpeakingTest;
    }

    public ObservableList<SpeakingTestQuestion> getSpeakingTestQuestions(){
        return FXCollections.observableArrayList(userSpeakingTest.getTest().getQuestions());
    }
    public ObservableList<UserSpeakingTestResponse> getUserSpeakingTestResponses(){
        return FXCollections.observableArrayList(userSpeakingTestResponses);
    }

    public void initialize(){
        loadLists();
    }

    private void loadLists(){
        Platform.runLater(()->{
            var temp = userSpeakingTest.getResponses().stream().sorted((a, b) -> a.getQuestion().getOrderIndex() - b.getQuestion().getOrderIndex()).toList();
            List<UserSpeakingTestResponse> startResponses = new ArrayList<>(temp);
            questionIndex.set(!startResponses.isEmpty() ? startResponses.getLast().getQuestion().getOrderIndex(): 0);
            userSpeakingTest.getTest().getQuestions().forEach(question->{
                UserSpeakingTestResponse userSpeakingTestResponse = new UserSpeakingTestResponse();
                userSpeakingTestResponse.setQuestion(question);
                userSpeakingTestResponse.setQuestionIndex(question.getOrderIndex());
                if(!startResponses.isEmpty()){
                    if(question.getOrderIndex() > startResponses.getLast().getQuestion().getOrderIndex()){

                        startResponses.add(userSpeakingTestResponse);
                    }
                }else{
                    startResponses.add(userSpeakingTestResponse);
                }
            });
            userSpeakingTestResponses.addAll(startResponses);
        });
    }

    public UserSpeakingTestResponse evaluateResponse(String response, UserSpeakingTestResponse userSpeakingTestResponse){
        try {
            UserSpeakingTestResponse evaluatedResponse =speakingTestService.submitResponse(userSpeakingTest.getId(), userSpeakingTestResponse.getQuestionId(), userSpeakingTestResponse.getQuestionIndex(), response);
            userSpeakingTestResponses.set(evaluatedResponse.getQuestionIndex(), evaluatedResponse);
            return evaluatedResponse;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void navigateBack(){
        RootViewModel.getInstance().getNavigationService().navigateBack();
    }

}
