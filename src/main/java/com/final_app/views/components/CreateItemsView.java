package com.final_app.views.components;

import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.SubViewItem;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import com.final_app.views.components.forms.ConversationChainForm;
import com.final_app.views.components.forms.ConversationForm;
import com.final_app.views.components.forms.ScenarioForm;
import com.final_app.views.components.forms.SpeakingTestForm;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class CreateItemsView extends VBox implements Initializable {
    @FXML private VBox optionsList;
    @FXML private VBox optionView;
    @FXML private HBox btnClose;
    @FXML private ImageView imgClose;

    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblSubTitle;


    private List<SubViewItem> options = new ArrayList<>();

    private Consumer<Void> onClosedClicked;

    public void setOnClosedClicked(Consumer<Void> onClosedClicked) {
        this.onClosedClicked = onClosedClicked;
    }


    // System text properties
    private StringProperty titleProperty = TranslationManager.get().t(TKey.FTITLE);
    private StringProperty descriptionProperty = TranslationManager.get().t(TKey.FDESCRIPTION);
    private StringProperty subTitleProperty = TranslationManager.get().t(TKey.FSUBTITLE);

    public CreateItemsView(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/CreateItemsView.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            Platform.runLater(()->{
                setBindings();
                setActions();
                btnClose.setOnMouseClicked(e -> {
                    onClosedClicked.accept(null);
                });
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void setBindings(){
        lblTitle.textProperty().bind(TranslationManager.get().t(TKey.FTITLE));
        lblDescription.textProperty().bind(TranslationManager.get().t(TKey.FDESCRIPTION));
        lblSubTitle.textProperty().bind(TranslationManager.get().t(TKey.FSUBTITLE));
    }
    private void setActions(){
        Platform.runLater(this::reloadSystemText);

//        TranslationManager.get().addLanguageChangeListener(lng -> {
//            Platform.runLater(()->{
//                reloadSystemText();
//                loadAll();
//            });
//        });
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(()->{
            loadAll();
        });
    }
    private void loadAll(){
        options.clear();
        String color1 = "#c574fb";
        String color2 = "#3B83F7";
        String color3 = "#F97217";
        String color4 = "#23C55F";
        options.add(
                new SubViewItem(
                        TranslationManager.get().t(TKey.FCONVERSATIONCHAIN),
                        TranslationManager.get().t(TKey.CCFDESCRIPTIONNAV),
                        new ConversationChainForm(),
                        color1,
                        GlobalVariables.ICONS + "chain_dark.svg",
                        "#100E2A" ));
        //options.add(new SubViewItem("Conversation Chain", new ConversationChainForm(), color1, GlobalVariables.ICONS + "book_light.svg" ));
        options.add(
                new SubViewItem(
                        TranslationManager.get().t(TKey.FCONVERSATION),
                        TranslationManager.get().t(TKey.CFDESCRIPTIONNAV),
                        new ConversationForm(),
                        color2,
                        GlobalVariables.ICONS + "conversation.svg",
                        "#09152E"
                ));
        options.add(
                new SubViewItem(
                        TranslationManager.get().t(TKey.FSCENARIO),
                        TranslationManager.get().t(TKey.SFDESCRIPTIONNAV),
                        new ScenarioForm(),
                        color3,
                        GlobalVariables.ICONS + "text_light.svg",
                        "#1B1216"
                ));
        options.add(
                new SubViewItem(
                        TranslationManager.get().t(TKey.FSPEAKINGTEST),
                        TranslationManager.get().t(TKey.SFDESCRIPTIONSPEAKINGTESTNAV),
                        new SpeakingTestForm(),
                        color4,
                        GlobalVariables.ICONS + "microphone_light.svg",
                        "#041B1E"
                ));

        imgClose.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/back_arrow_light.svg", 30, 30));
        initializeOptions();
    }

    private void initializeOptions(){
        optionsList.getChildren().clear();
        for(SubViewItem subViewItem : options){
            Label label = new Label();
            label.textProperty().bind(subViewItem.getTitle());
            label.getStyleClass().addAll("primary", "b2");
            VBox textBox = new VBox();
            Label description = new Label();
            description.textProperty().bind(subViewItem.getDescription());
            description.getStyleClass().addAll("b3", "secondary");
            textBox.getChildren().addAll(label, description);
            HBox btn = new HBox(textBox);
            btn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            if(subViewItem.getIcon() != null){
                ImageView icon = new ImageView(subViewItem.getIcon());

                HBox iconBox = new HBox(icon);
                iconBox.getStyleClass().addAll("align-center", "circle");
                iconBox.setStyle("-fx-background-color: " + subViewItem.getBackgroundColor());
                iconBox.setPrefSize(35, 35);
                iconBox.setMinSize(35, 35);
                iconBox.setMaxSize(35, 35);
                iconBox.setPadding(new Insets(5,5,5,5));
                btn.getChildren().addFirst(iconBox);
            }
            btn.setSpacing(15);
            btn.getStyleClass().addAll("bg-dark", "cell", "align-center-left");

            btn.setOnMouseClicked(e -> {
                optionsList.getChildren().forEach(child -> {
                    child.getStyleClass().remove("selected");
                });
                btn.getStyleClass().add("selected");

                optionView.getChildren().clear();
                subViewItem.getForm().initialize();
                optionView.getChildren().add(subViewItem.getForm());

            });
            optionsList.getChildren().add(btn);
        }

        optionsList.getChildren().get(0).getStyleClass().add("selected");
        optionView.getChildren().clear();
        optionView.getChildren().add(options.getFirst().getForm());
    }

    private void reloadSystemText(){
//        titleProperty.set(TranslationManager.get().t(TKey.FTITLE));
//        descriptionProperty.set(TranslationManager.get().t(TKey.FDESCRIPTION));
//        subTitleProperty.set(TranslationManager.get().t(TKey.FSUBTITLE));
    }
}
