package com.final_app.views.components.custom.list;

import com.final_app.globals.GlobalVariables;
import com.final_app.models.Conversation;
import com.final_app.models.ConversationChainItem;
import com.final_app.models.IndexedItem;
import com.final_app.models.SpeakingTestQuestion;
import com.final_app.tools.SVGUtil;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;


public class ListItemComponent<T> extends HBox {
    @FXML private Label lblOrderNumber;
    @FXML private Label lblText;

    @FXML private VBox orderBtnsBox;
    @FXML private ImageView btnUp;
    @FXML private ImageView btnDown;

    @FXML private HBox btnUpBox;
    @FXML private HBox btnDownBox;

    @FXML private VBox deleteBtnBox;
    @FXML private ImageView btnDelete;

    // properties
    private IntegerProperty orderNumberProperty = new SimpleIntegerProperty();
    private StringProperty textProperty = new SimpleStringProperty();
    private T item;


    // Consumers
    private Consumer<T> onItemUp;
    private Consumer<T> onItemDown;
    private Consumer<T> onItemDelete;
    public void setOnItemUp(Consumer<T> onItemUp){this.onItemUp = onItemUp;}
    public void setOnItemDown(Consumer<T> onItemDown){this.onItemDown = onItemDown;}
    public void setOnItemDelete(Consumer<T> onItemDelete){this.onItemDelete = onItemDelete;}

    public ListItemComponent(T item) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/custom/list/ListItemComponent.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            bindProperties();
            renderItem(item);
            bindActions();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void bindProperties(){
        lblOrderNumber.textProperty().bind(this.orderNumberProperty.asString());
        lblText.textProperty().bind(this.textProperty);

        int iconSize = 20;

        btnUp.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "arrow_up_light.svg", iconSize,iconSize));
        btnDown.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "arrow_down_light.svg", iconSize,iconSize));
        btnDelete.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "delete_light.svg", iconSize,iconSize));
    }
    private void bindActions(){
        btnUpBox.setOnMouseClicked(e->{
            if(onItemUp != null){
                onItemUp.accept(item);
            }
        });
        btnDownBox.setOnMouseClicked(e->{
            if(onItemDown != null){
                onItemDown.accept(item);
            }
        });
        btnDelete.setOnMouseClicked(e->{
            if(onItemDelete != null){
                onItemDelete.accept(item);
            }
        });
    }

    private void renderItem(T item){
        if(item instanceof SpeakingTestQuestion){
            var tempItem = (SpeakingTestQuestion) item;
            orderNumberProperty.set(tempItem.getOrderIndex());
            textProperty.set(tempItem.getQuestionText() + "\n" + tempItem.getExpectedResponsePattern());
            this.item = item;
        }
        if(item instanceof ConversationChainItem){
            var tempItem = (ConversationChainItem) item;
            orderNumberProperty.set(tempItem.getConversationIndex());
            textProperty.set(tempItem.getConversation().getTitle());
            this.item = item;
        }
        if(item instanceof IndexedItem){
            var tempItem = (IndexedItem) item;
            orderNumberProperty.set(tempItem.getIndex());
            textProperty.set(tempItem.getText());
            this.item = item;
        }
        if(item instanceof Conversation){
            var tempItem = (Conversation) item;
            lblOrderNumber.setVisible(false);
            textProperty.set(tempItem.getTitle());
            this.item = item;
        }
        if(item instanceof String){
            orderNumberProperty.set(0);
            textProperty.set((String) item);
            this.item = item;
        }
    }
}
