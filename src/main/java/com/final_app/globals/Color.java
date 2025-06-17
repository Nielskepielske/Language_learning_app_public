package com.final_app.globals;

public enum Color {
    LIGHTBLUE("#4fdafb"),
    BLUE("#5528AE"),
    DARKBLUE("#164685"),
    LIGHTGREEN("#68f055"),
    GREEN("#6bbf2c"),
    DARKGREEN("#447b1c"),
    YELLOW("#e4e105"),
    LIGHTRED("#f16b68"),
    RED("#ea211b"),
    DARKRED("#8a0d0a"),
    PURPLE("#8227aa"),
    GRAY("#a29da4"),
    DARKGRAY("#4a484a"),
    BLACK("#000000"),
    WHITE("#ffffff"),
    ORANGE("#f58e25"),
    LIGHTORANGE("#f5b040"),
    ;

    private String value;
    public String getValue(){
        return this.value;
    }

    Color(String value){
        this.value = value;
    }

}
