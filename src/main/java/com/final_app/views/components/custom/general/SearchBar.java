package com.final_app.views.components.custom.general;

import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class SearchBar extends HBox {
    private ImageView searchIcon;
    private VBox searchIconBox;
    private TextField searchField;

    public StringProperty textProperty = new SimpleStringProperty("");

    private StringProperty placeholderText = new SimpleStringProperty("Search");

    private Consumer<String> onSearch;
    public void setOnSearch(Consumer<String> onSearch){
        this.onSearch = onSearch;
    }
    public void search(String text){
        if(onSearch != null){
            onSearch.accept(text);
        }
    }
    public void clearSearch(){}

    public SearchBar(){
        super();
        searchIcon = new ImageView();
        searchField = new TextField();

        searchIconBox = new VBox();
        searchIconBox.getChildren().add(searchIcon);
        searchIconBox.getStyleClass().addAll("align-center");
        searchField.getStyleClass().addAll("primary", "b3", "bg-dark");
        searchField.promptTextProperty().bind(placeholderText);
        searchField.textProperty().bindBidirectional(textProperty);

        HBox.setHgrow(searchField, Priority.ALWAYS);

        this.getChildren().addAll(searchIconBox, searchField);
        setSearchIcon();

        this.getStyleClass().addAll("border-3", "border-radius-1", "p-1", "border-light");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal.isEmpty()){
                clearSearch();
                search("");
            }else{
                search(newVal);
            }
        });
        searchField.setOnKeyPressed(e -> {

        });
        reloadTranslations();

//        TranslationManager.get().addLanguageChangeListener(lang -> {
//            Platform.runLater(this::reloadTranslations);
//        });
    }
    private void reloadTranslations(){
        placeholderText.bind(TranslationManager.get().t(TKey.SEARCH));
    }

    public void setSearchIcon(){
        searchIcon.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "search_light.svg", 20,20));
    }
    public void setSearchFieldText(String text){
        searchField.setText(text);
    }
    public String getSearchFieldText(){
        return searchField.getText();
    }
}
