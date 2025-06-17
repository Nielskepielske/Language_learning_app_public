package com.final_app.globals;

public enum AIModels {
    CONVERSATION("gpt-4.1-mini"), // gpt-3.5-turbo, gpt-4o-mini, gpt-o3-mini, gpt-4.1-nano
    EVALUATION("gpt-4.1"), // gpt-4, gpt-4o, gpt-4.1
    COMPLEXITY("gpt-4.1"),// gpt-4, gpt-4o, gpt-4.1
    SPEECH("gpt-4o-mini-tts"); // tts-1-hd, gpt-4o-mini-tts

    private String model;

    public String getModel(){
        return model;
    }

    AIModels(String model){
        this.model = model;
    }
}
