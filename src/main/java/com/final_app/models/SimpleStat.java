package com.final_app.models;

public class SimpleStat {
    public String title;
    public int value;
    public String extra;
    public String icon;

    public SimpleStat(){

    }
    public SimpleStat(String title, int value, String extra, String icon){
        this.title = title;
        this.value = value;
        this.extra = extra;
        this.icon = icon;
    }

    public void setTitle(String title){
        this.title = title;
    }
    public void setValue(int value){
        this.value = value;
    }
    public void setExtra(String extra){
        this.extra = extra;
    }
    public void setIcon(String path){
        this.icon = path;
    }
}
