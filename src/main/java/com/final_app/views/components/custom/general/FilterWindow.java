package com.final_app.views.components.custom.general;

import com.final_app.globals.TKey;
import com.final_app.models.Language;
import com.final_app.tools.TranslationManager;
import com.final_app.views.components.custom.selection.SelectableComponent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class FilterWindow extends VBox {
    private VBox textContainer;
    private Label lblTitle;
    private Label lblDescription;

    private VBox languageContainer;
    private FlowPane languageFlowPane;

    private HBox btnContainer;
    private Button btnCancel;
    private Button btnApply;

    private Consumer<List<Language>> onApply;
    public void setOnApply(Consumer<List<Language>> onApply){ this.onApply = onApply;}

    private Consumer<Void> onCancel;
    public void setOnCancel(Consumer<Void> onCancel){ this.onCancel = onCancel;}

    private ObservableList<Language> selectedLanguages = FXCollections.observableArrayList();

    TKey titleKey;
    TKey descriptionKey;

    public FilterWindow(){
        super();
        textContainer = new VBox();
        lblTitle = new Label("Languages");
        lblDescription = new Label("Select languages to filter by");

        lblTitle.getStyleClass().addAll("h3","primary");
        lblDescription.getStyleClass().addAll("b3","secondary");

        languageContainer = new VBox();
        languageFlowPane = new FlowPane();
        languageFlowPane.setHgap(10);
        languageFlowPane.setVgap(10);
        languageContainer.getChildren().add(languageFlowPane);

        btnContainer = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        btnCancel = new Button("Cancel");
        btnApply = new Button("Apply");
        btnApply.getStyleClass().addAll("primary", "btn-modern", "bg-purple", "p-label-m");
        btnCancel.getStyleClass().addAll("primary", "btn-modern", "bg-blue", "p-label-m");
        btnContainer.setSpacing(20);

        this.getChildren().addAll(textContainer, languageContainer, btnContainer);
        this.getStyleClass().addAll("card", "bg-dark", "p-2");
        this.setSpacing(30);
        this.setMinHeight(300);

        textContainer.getChildren().addAll(lblTitle, lblDescription);
        btnContainer.getChildren().addAll(spacer, btnCancel, btnApply);

        setBindings();
        reloadTranslations();

//        TranslationManager.get().addLanguageChangeListener(lang -> {
//            Platform.runLater(this::reloadTranslations);
//        });
    }
    private void reloadTranslations(){
        if(titleKey != null && descriptionKey != null){
            lblTitle.textProperty().bind(TranslationManager.get().t(titleKey));
            lblDescription.textProperty().bind(TranslationManager.get().t(descriptionKey));
        }
        btnCancel.textProperty().bind(TranslationManager.get().t(TKey.CANCEL));
        btnApply.textProperty().bind(TranslationManager.get().t(TKey.APPLY));
    }
    public void setLanguageKeys(TKey title, TKey description){
        this.titleKey = title;
        this.descriptionKey = description;
        reloadTranslations();
    }


    public void setItems(List<Language> list){
        languageFlowPane.getChildren().clear();
        //selectedLanguages.clear();
        for(Language l : list){
            SelectableComponent<Language> comp = new SelectableComponent<>(l);
            languageFlowPane.getChildren().add(comp);



            comp.widthProperty().addListener((obs, oldVal, newVal)->{
                double width = (list.size() / 4 > 1 ? newVal.doubleValue() * 4 + 80: newVal.doubleValue() * list.size() + 80);
                this.minWidth(width);
                this.setMaxHeight(Region.USE_PREF_SIZE);
                this.setMaxWidth(width);
            });

            comp.setOnSelectionChanged(lng -> {
                if(comp.isSelected){
                    selectedLanguages.add(l);
                }else{
                    selectedLanguages.remove(l);
                }
            });
            if(selectedLanguages.contains(l)){
                System.out.println("Contains " + l.getName());
                comp.select(false);
            }
        }
    }

    private void setBindings(){
        btnCancel.setOnMouseClicked(e -> {
           onCancel.accept(null);
        });
        btnApply.setOnMouseClicked(e -> {
            onApply.accept(selectedLanguages);
        });
    }

}
