package com.final_app.views.components;

import com.final_app.globals.ConversationStatus;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.UserConversation;
import com.final_app.models.UserSpeakingTest;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import com.final_app.views.components.custom.table.CustomTableView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class CustomGroupWindow<T> extends VBox {
    //@FXML private GridPane rowContainer;
    @FXML private VBox btnBack;
    @FXML private ImageView imgBack;

    @FXML private Label lblTitle;
    @FXML private Label lblExtra;

    @FXML private Label lblDescriptionTitle;
    @FXML private Label lblDescription;
    @FXML private HBox pillBox;

    @FXML private HBox btnCreate;
    @FXML private Label lblCreate;

    @FXML private CustomTableView<T> tblItems;

    private Consumer<Void> onBackButtonClicked;
    public void setOnBackButtonClicked(Consumer<Void> onBackButtonClicked){this.onBackButtonClicked = onBackButtonClicked;}

    private Consumer<T> onCreateButtonClicked;
    public void setOnCreateButtonClicked(Consumer<T> onCreateButtonClicked){this.onCreateButtonClicked = onCreateButtonClicked;}


    public CustomGroupWindow() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/CustomGroupWindow.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            configureBackButton();
            reloadTranslations();

//            TranslationManager.get().addLanguageChangeListener(lang -> {
//                Platform.runLater(this::reloadTranslations);
//            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public CustomGroupWindow(List<T> items) {
        this();
        initializeRow(items);
    }

    private void reloadTranslations(){
        lblDescriptionTitle.textProperty().bind(TranslationManager.get().t(TKey.FPDESCRIPTION));
        lblCreate.textProperty().bind(TranslationManager.get().t(TKey.CREATENEW));
    }

    private void setBindings(T item){
        if(item instanceof UserConversation){
            UserConversation uc = (UserConversation) item;
            lblTitle.setText(uc.getConversation().getTitle());
            lblExtra.setText(uc.getConversation().getLanguage().getName() + " • " + uc.getConversation().getLanguageLevel().getName());
            lblDescription.setText(uc.getConversation().getDescription());
        }else if(item instanceof UserSpeakingTest){
            UserSpeakingTest us = (UserSpeakingTest) item;
            lblTitle.setText(us.getTest().getTitle());
            lblExtra.setText(us.getTest().getLanguage().getName() + " • " + us.getTest().getLanguageLevel().getName());
            lblDescription.setText(us.getTest().getDescription());
        }

        btnCreate.setOnMouseClicked(e -> {
            onCreateButtonClicked.accept(item);
        });

    }
    private void configureBackButton() {
        imgBack.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "back_arrow_light.svg", 30, 30));
        btnBack.setOnMouseClicked(e -> {
            onBackButtonClicked.accept(null);
        });
    }

    public void initializeRow(List<T> items){
        setBindings(items.getFirst());
        tblItems.setConversations(items);
        pillBox.getChildren().clear();

        if(!items.isEmpty()){
            if(items.getFirst() instanceof UserConversation){
                // Hbox pills
                pillBox.getChildren().clear();

                List<UserConversation> completedConvos = ((List<UserConversation>) items).stream().filter(uc -> uc.getStatusEnum() == ConversationStatus.COMPLETED).toList();

                int completed = completedConvos.size();
                int averageScore = (int) (completedConvos.stream().map(uc -> ((double)uc.getEvaluation().getScore() / uc.getEvaluation().getMaxScore()) * 100).reduce(0.0, Double::sum) / completed);

                Label lblFirst = new Label(Integer.toString(completed) + " " + TranslationManager.get().t(TKey.COMPLETED).get().toLowerCase());
                Label lblSecond = new Label(Integer.toString(averageScore) + " " + TranslationManager.get().t(TKey.AVERAGESCORE).get().toLowerCase());
                lblFirst.getStyleClass().addAll("primary");
                lblSecond.getStyleClass().addAll("primary");

                ImageView icon1 = new ImageView(SVGUtil.loadSVG(GlobalVariables.ICONS + "check_light.svg", 13, 13));
                ImageView icon2 = new ImageView(SVGUtil.loadSVG(GlobalVariables.ICONS + "star_light.svg", 13, 13));

                HBox hbox1 = new HBox(icon1, lblFirst);
                HBox hbox2 = new HBox(icon2, lblSecond);

                hbox1.setSpacing(5);
                hbox2.setSpacing(5);
                hbox1.getStyleClass().addAll("pill");
                hbox2.getStyleClass().addAll("pill");
                pillBox.getChildren().addAll(hbox1, hbox2);
            }else if(items.getFirst() instanceof UserSpeakingTest){
                pillBox.getChildren().clear();

                List<UserSpeakingTest> completedTests = ((List<UserSpeakingTest>) items).stream().filter(uc -> uc.getStatusEnum() == ConversationStatus.COMPLETED).toList();

                int completed = completedTests.size();
                int averageScore = (int) (completedTests.stream().map(us -> (double)us.getResponses().stream().map( e -> (double)e.getOverallScore() / e.maxScore).reduce(0.0, Double::sum) / us.getResponses().size() * 100).reduce(0.0, Double::sum) / completed);

                Label lblFirst = new Label(Integer.toString(completed) + " " + TranslationManager.get().t(TKey.COMPLETED).get().toLowerCase());
                Label lblSecond = new Label(Integer.toString(averageScore) + " " + TranslationManager.get().t(TKey.AVERAGESCORE).get().toLowerCase());
                lblFirst.getStyleClass().addAll("primary");
                lblSecond.getStyleClass().addAll("primary");

                ImageView icon1 = new ImageView(SVGUtil.loadSVG(GlobalVariables.ICONS + "check_light.svg", 13, 13));
                ImageView icon2 = new ImageView(SVGUtil.loadSVG(GlobalVariables.ICONS + "star_light.svg", 13, 13));

                HBox hbox1 = new HBox(icon1, lblFirst);
                HBox hbox2 = new HBox(icon2, lblSecond);

                hbox1.setSpacing(5);
                hbox2.setSpacing(5);
                hbox1.getStyleClass().addAll("pill");
                hbox2.getStyleClass().addAll("pill");
                pillBox.getChildren().addAll(hbox1, hbox2);
            }
        }
    }
}
