package com.final_app.views.components.custom.table;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.List;

import com.final_app.models.UserConversation;

/**
 * A self-contained JavaFX component wrapping a UserConversation table.
 * To use in FXML, register its namespace:
 *   xmlns:ui="http://your.package/ui"
 * then include:
 *   <ui:ConversationTableView fx:id="convTable" />
 */
public class CustomTableView<T> extends VBox {

    @FXML private TableView<UserConversation> table;
    private CustomTableController<T> controller;


    public CustomTableView() {
        // Load the nested FXML (must be on classpath under com/final_app/ui)
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/final_app/views/components/custom/table/CustomTableView.fxml")
        );
        loader.setRoot(this);
        // Let controller inject
        controller = new CustomTableController<>();
        loader.setController(controller);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed loading ConversationTableView.fxml", e);
        }
    }

    /**
     * Populate the table with a list of conversations.
     */
    public void setConversations(List<T> list) {

        controller.setConversations(list);
    }

    /**
     * Access the raw TableView for further customizations.
     */
    public TableView<UserConversation> getTableView() {
        return table;
    }
}

