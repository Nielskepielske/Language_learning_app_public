package com.final_app.models;

import com.final_app.tools.SVGUtil;
import com.final_app.views.components.forms.BaseForm;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class SubViewItem {
    private StringProperty title;
    private BaseForm form;
    private StringProperty description;
    private String color;
    private String backgroundColor;
    private String icon;

    public SubViewItem(){}
    public SubViewItem(StringProperty title, BaseForm form){
        this.title = title;
        this.form = form;
    }
    public SubViewItem(StringProperty title, StringProperty description, BaseForm form, String color, String icon, String backgroundColor){
        this.title = title;
        this.description = description;
        this.form = form;
        this.color = color;
        this.backgroundColor = backgroundColor;
        this.icon = icon;
    }

    public StringProperty getTitle(){return title;}
    public void setTitle(StringProperty title){this.title = title;}


    public BaseForm getForm(){return form;}
    public void setForm(BaseForm form){this.form = form;}

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Image getIcon() {
        return SVGUtil.loadSVG(icon, 20, 20, color);
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public StringProperty getDescription() {
        return description;
    }

    public void setDescription(StringProperty description) {
        this.description = description;
    }
}
