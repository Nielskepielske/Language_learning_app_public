package com.final_app.models;

import com.final_app.globals.ConversationStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserSpeakingTest {
    private String id;
    private String userId;
    private String testId;
    private Date startedAt;
    private Date completedAt;
    private String status; // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    private int score;
    private Date lastUpdate;

    private SpeakingTest test;
    private List<UserSpeakingTestResponse> responses;

    public UserSpeakingTest(){
        responses = new ArrayList<>();
    }
    public UserSpeakingTest(String userId, String testId, String status, int score){
        this.userId = userId;
        this.testId = testId;
        this.status = status;
        this.score = score;
        responses = new ArrayList<>();
    }

    public String getId(){return this.id;}
    public void setId(String id){this.id = id;}

    public String getUserId(){return userId;}
    public void setUserId(String userId){this.userId = userId;}

    public String getTestId(){return testId;}
    public void setTestId(String testId){this.testId = testId;}

    public Date getStartedAt(){return startedAt;}
    public void setStartedAt(Date startedAt){this.startedAt = startedAt;}

    public Date getCompletedAt(){return completedAt;}
    public void setCompletedAt(Date completedAt){this.completedAt = completedAt;}

    public String getStatus(){return status;}
    public void setStatus(String status){this.status = status;}

    public ConversationStatus getStatusEnum(){
        try{
            return ConversationStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public int getScore(){return score;}
    public void setScore(int score){this.score = score;}

    public List<UserSpeakingTestResponse> getResponses(){return responses;}
    public void setResponses(List<UserSpeakingTestResponse> responses){this.responses = responses;}

    public SpeakingTest getTest() {
        return test;
    }

    public void setTest(SpeakingTest test) {
        this.test = test;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
