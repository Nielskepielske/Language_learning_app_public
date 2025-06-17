package com.final_app.views.components;

import com.final_app.globals.TKey;
import com.final_app.tools.TranslationManager;
import com.final_app.views.components.custom.general.SearchBar;
import com.final_app.views.components.custom.list.ItemFormList;
import com.final_app.views.components.custom.selection.SelectableComponent;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;


public class AddItemFromListPopup<T> extends VBox {
    @FXML private SearchBar searchBar;
//    @FXML private ItemFormList<T> listResults;
    @FXML private FlowPane listResults;
    @FXML private HBox btnAdd;
    @FXML private Label lblAdd;
    @FXML private HBox btnCancel;
    @FXML private Label lblCancel;

    @FXML private Label lblTitle;
    @FXML private Label lblDescription;

    private Consumer<T> onAdd;
    private Consumer<List<T>> onAddList;
    public void setOnAdd(Consumer<T> onAdd){this.onAdd = onAdd;}
    public void setOnAddList(Consumer<List<T>> onAddList){this.onAddList = onAddList;}

    private Consumer<Void> onCancel;
    public void setOnCancel(Consumer<Void> onCancel){this.onCancel = onCancel;}

    private T selected;

    private ObservableList<T> list = FXCollections.observableArrayList();
    private ObservableList<T> selectedList = FXCollections.observableArrayList();

    SelectionModel<T> selectionModel;

    TKey titleKey;
    TKey descriptionKey;

    public AddItemFromListPopup(List<T> list, TKey title, TKey description) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/AddItemFromListPopup.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            this.titleKey = title;
            this.descriptionKey = description;

            //selectionModel = listResults.getSelectionModel();
            btnAdd.setDisable(true);

            this.list.setAll(list);

            //renderList();

            btnAdd.setOnMouseClicked(e -> {
                if(selected != null){
                    onAdd.accept(selected);
                }else if(!selectedList.isEmpty()){
                    onAddList.accept(selectedList);
                }
            });
            btnCancel.setOnMouseClicked(e -> {
                onCancel.accept(null);
            });

            searchBar.setOnSearch(txt -> {
                this.list.setAll(list.stream().filter(e -> txt.isEmpty() || e.toString().contains(txt)).toList());
                //renderList();
            });

            renderList(list);
            setBindings();
            reloadTranslations();

//            TranslationManager.get().addLanguageChangeListener(lang -> {
//                reloadTranslations();
//            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void reloadTranslations(){
        lblTitle.textProperty().bind(TranslationManager.get().t(titleKey));
        lblDescription.textProperty().bind(TranslationManager.get().t(descriptionKey));
        lblAdd.textProperty().bind(TranslationManager.get().t(TKey.FPADD));
        lblCancel.textProperty().bind(TranslationManager.get().t(TKey.CANCEL));
    }
    private void setBindings(){
        list.addListener((ListChangeListener<T>) c -> {
            renderList(list);
        });
    }

    private void renderList(List<T> list){
        listResults.getChildren().clear();
        for(T item : list){
            SelectableComponent<T> comp = new SelectableComponent<>(item);
            if(selectedList.contains(item)){
                comp.select(false);
            }else{
                comp.deselect(false);
            }

            comp.setOnSelectionChanged(selected -> {
                if(selectedList.contains(item)){
                    selectedList.remove(item);
                }else{
                    selectedList.add(item);
                }
                btnAdd.setDisable(selectedList.isEmpty());
            });
            listResults.getChildren().add(comp);
        }
    }
}
