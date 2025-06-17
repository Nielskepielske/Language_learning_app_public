package com.final_app.models;

import de.saxsys.mvvmfx.FxmlView;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;


public class NavigationItem {

    private final StringProperty title;
    private Class<? extends FxmlView<?>> viewClass = null;
    private Runnable action = null;
    private final String icon;

    public NavigationItem(StringProperty title, Class<? extends FxmlView<?>> viewClass, String icon) {
        this.title = title;
        this.viewClass = viewClass;
        this.icon = icon;
    }
    public NavigationItem(StringProperty title, String icon, Runnable action) {
        this.title = title;
        this.action = action;
        this.icon = icon;
        this.viewClass = null;
    }

    public StringProperty getTitle() {
        return title;
    }

    public Class<? extends FxmlView<?>> getViewClass() {
        return viewClass;
    }

    public String getIcon(){
        return icon;
    }

    @Override
    public String toString() {
        return title.get();
    }

    public Runnable getAction() {
        return action;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }
}