package com.final_app.views.components.custom.list;

import com.final_app.models.Conversation;
import com.final_app.models.ConversationChainItem;
import com.final_app.models.IndexedItem;
import com.final_app.models.SpeakingTestQuestion;
import com.final_app.views.components.custom.selection.SelectableComponent;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ItemFormList<T> extends VBox {
    @FXML private ListView<T> listResults;

    private final ObservableList<T> list;
    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
    private Consumer<List<T>> onListChanged;

    public ItemFormList(List<T> initialList) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/custom/list/ItemFormList.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.list = FXCollections.observableArrayList(initialList);
        initialize();
    }

    public ItemFormList() {
        this(List.of());
    }

    /**
     * Vervang de volledige inhoud van de lijst met nieuwe items.
     */
    public void setItems(List<T> items) {
        this.list.setAll(items);
    }

    /**
     * Retourneer de huidige lijst (immutable copy).
     */
    public List<T> getItems() {
        return List.copyOf(list);
    }

    /**
     * Voeg een item toe aan het einde van de lijst.
     */
    public void addItem(T item) {
        list.add(item);
    }

    /**
     * Voeg een item op een specifieke positie toe.
     */
    public void addItem(int index, T item) {
        list.add(index, item);
    }

    /**
     * Verwijder een item uit de lijst.
     */
    public void removeItem(T item) {
        list.remove(item);
    }

    /**
     * Verwijder een item op index.
     */
    public void removeItem(int index) {
        if (index >= 0 && index < list.size()) {
            list.remove(index);
        }
    }

    /**
     * Callback registreren om wijzigingen (move, add, delete) op te vangen.
     */
    public void setOnListChanged(Consumer<List<T>> callback) {
        this.onListChanged = callback;
    }

    private void initialize() {
        // Bind de SJ lijst aan de ListView
        listResults.setItems(list);

        // Luister op alle wijzigingen: add, remove of reorder
        list.addListener((ListChangeListener<T>) change -> {
            updateOrderIndices();
            listResults.refresh();
            fireListChanged();
        });

        // CellFactory om per item de ListItemComponent te tonen
        listResults.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    if(item instanceof ConversationChainItem || item instanceof IndexedItem || item instanceof SpeakingTestQuestion){
                        ListItemComponent<T> comp = new ListItemComponent<>(item);

                        // bind comp’s max/pref width to 80% of the ListView’s width:
                        comp.maxWidthProperty().bind(listResults.widthProperty().multiply(0.95));
                        comp.prefWidthProperty().bind(comp.maxWidthProperty());

                        comp.setOnItemUp(ItemFormList.this::moveItemUp);
                        comp.setOnItemDown(ItemFormList.this::moveItemDown);
                        comp.setOnItemDelete(ItemFormList.this::deleteItem);
                        setGraphic(comp);
                    }else if(item instanceof Conversation){
                        SelectableComponent<T> comp = new SelectableComponent<>(item);

                        if(selectedItems.contains(item)){
                            comp.select(false);
                        }

                        comp.setOnSelectionChanged(_ -> {
                            if(comp.isSelected){
                                //comp.deselect(false);
                                selectedItems.add(item);
                            }else{
                                //comp.select();
                                selectedItems.remove(item);
                            }
                        });
                        setGraphic(comp);
                    }

                }
            }
        });
    }

    private void moveItemUp(T item) {
        int idx = list.indexOf(item);
        if (idx > 0) {
            Collections.swap(list, idx, idx - 1);
        }
    }

    private void moveItemDown(T item) {
        int idx = list.indexOf(item);
        if (idx < list.size() - 1) {
            Collections.swap(list, idx, idx + 1);
        }
    }

    private void deleteItem(T item) {
        list.remove(item);
    }

    /**
     * Update volgorde-index per item
     */
    private void updateOrderIndices() {
        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            int newIndex = i + 1;
            if (item instanceof SpeakingTestQuestion) {
                ((SpeakingTestQuestion) item).setOrderIndex(newIndex);
            } else if (item instanceof ConversationChainItem) {
                ((ConversationChainItem) item).setConversationIndex(newIndex);
            } else if (item instanceof IndexedItem) {
                ((IndexedItem) item).setIndex(newIndex);
            }
        }
    }

    public void clear() {
        list.clear();
    }

    private void fireListChanged() {
        if (onListChanged != null) {
            onListChanged.accept(List.copyOf(list));
        }
    }
}

