package com.final_app.globals;

public enum Difficulty {
    EASY(1),
    MEDIUM(2),
    INTERMEDIATE(3),
    HARD(4),
    ADVANCED(5)
    ;

    private int value;
    public int getValue(){
        return this.value;
    }

    Difficulty(int value){
        this.value = value;
    }

}
