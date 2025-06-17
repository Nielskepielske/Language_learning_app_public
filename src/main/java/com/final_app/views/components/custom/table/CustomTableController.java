package com.final_app.views.components.custom.table;

import com.final_app.globals.Color;
import com.final_app.globals.TKey;
import com.final_app.models.User;
import com.final_app.models.UserConversation;
import com.final_app.globals.ConversationStatus;
import com.final_app.models.UserSpeakingTest;
import com.final_app.tools.ColorTranslator;
import com.final_app.tools.TranslationManager;
import com.final_app.viewmodels.RootViewModel;
import com.final_app.views.pages.ChatView;
import com.final_app.views.pages.SpeakingTestPage;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CustomTableController<T> {
    @FXML private TableView<T> table;
    @FXML private TableColumn<T, ConversationStatus> statusCol;
    @FXML private TableColumn<T, Date> dateCol;
    @FXML private TableColumn<T, T> scoreBarCol;
    @FXML private TableColumn<T, String> durationCol;
    @FXML private TableColumn<T, T> actionCol;

    private double fixedRowHeight = 70;

    private final ObservableList<T> data = FXCollections.observableArrayList();

    public void initialize() {
        data.addListener((ListChangeListener<? super T>) c -> adaptHeight());

        // Configure Status column
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusEnum"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ConversationStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(status.getText());
                    lbl.getStyleClass().add("pill");
                    switch (status) {
                        case NOTSTARTED -> {
                            String color = "#f05555";
                            lbl.setText(TranslationManager.get().t(TKey.NOTSTARTED).get());
                            lbl.setStyle("-fx-text-fill: -not-started-color;-fx-background-color: " + ColorTranslator.textToBackground(color));
                        }
                        case IN_PROGRESS -> {
                            String color = "#3dbbff";
                            lbl.setText(TranslationManager.get().t(TKey.INPROGRESS).get());
                            lbl.setStyle("-fx-text-fill: -in-progress-color;-fx-background-color: " + ColorTranslator.textToBackground(color));
                        }
                        case COMPLETED -> {
                            String color = "#68f055";
                            lbl.setText(TranslationManager.get().t(TKey.COMPLETED).get());
                            lbl.setStyle("-fx-text-fill: -completed-color;-fx-background-color: " + ColorTranslator.textToBackground(color));
                        }
                    }
                    setGraphic(lbl);
                }
            }
        });

        // Configure Date column
        dateCol.setCellValueFactory(new PropertyValueFactory<>("completedAt"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            private final SimpleDateFormat fmt1 = new SimpleDateFormat("MMM d, yyyy");
            private final SimpleDateFormat fmt2 = new SimpleDateFormat("hh:mm a");
            private final Label lbl1 = new Label("MMM d, yyyy");
            private final Label lbl2 = new Label("hh:mm a");
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if( empty || date == null ) {
                    setGraphic(null);
                    return;
                }else{
                    lbl1.setText(fmt1.format(date));
                    lbl1.getStyleClass().addAll("b3", "primary", "bold");
                    lbl2.setText(fmt2.format(date));
                    lbl2.getStyleClass().addAll("b4", "secondary");
                    VBox vbox = new VBox(lbl1, lbl2);
                    vbox.getStyleClass().addAll("align-center-left");
                    setGraphic(vbox);
                }
            }
        });

        // Configure Score numeric column (hidden or not needed)
        //scoreCol.setCellValueFactory(new PropertyValueFactory<>("evaluation.score"));

        // Configure ScoreBar column
        scoreBarCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        scoreBarCol.setSortable(true);
        scoreBarCol.setComparator((a, b) -> {
            if(a instanceof UserConversation && b instanceof UserConversation){
                UserConversation ucA = (UserConversation) a;
                UserConversation ucB = (UserConversation) b;
                if(ucA.getStatusEnum() == ConversationStatus.COMPLETED && ucB.getStatusEnum() == ConversationStatus.COMPLETED){
                    return ucA.getEvaluation().getScore() - ucB.getEvaluation().getScore();
                }else{
                    if(ucA.getStatusEnum() == ConversationStatus.COMPLETED){
                        return 1;
                    }else if(ucB.getStatusEnum() == ConversationStatus.COMPLETED){
                        return -1;
                    }
                }
            }else if(a instanceof UserSpeakingTest){
                UserSpeakingTest usA = (UserSpeakingTest) a;
                UserSpeakingTest usB = (UserSpeakingTest) b;
                return usA.getScore() - usB.getScore();
            }
            return 0;
        });
        scoreBarCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<T, T> call(TableColumn<T, T> col) {
                return new TableCell<>() {
                    private final ProgressBar bar = new ProgressBar(0);
                    private final Label lbl = new Label("0%");
                    private final HBox hbox = new HBox(10, lbl, bar);
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item instanceof UserConversation){
                            UserConversation uc = (UserConversation) item;
                            if (empty || item == null || uc.getEvaluation() == null) {
                                setGraphic(null);
                            } else {
                                double pct = (double) uc.getEvaluation().getScore() / uc.getEvaluation().getMaxScore();
                                bar.setProgress(pct);
                                bar.setPrefWidth(120);
                                bar.setPrefHeight(10);
                                bar.getStyleClass().addAll("progress-bar-new");
                                lbl.setText(Math.round(pct * 100) + "%");
                                lbl.getStyleClass().addAll("primary", "b3", "bold");
                                hbox.getStyleClass().addAll("align-center");

                                setGraphic(hbox);
                            }
                        }else if(item instanceof UserSpeakingTest){
                            UserSpeakingTest us = (UserSpeakingTest) item;
                            double sum = us.getResponses().stream().map(e -> (double) e.getOverallScore() / e.maxScore * 100).reduce(0.0, Double::sum);
                            double avg = Math.round(sum / us.getResponses().size());
                            double pct = 0.0;
                            if(us.getStatusEnum() == ConversationStatus.COMPLETED){
                                pct = avg / 100;
                            }
                            bar.setProgress(pct);
                            bar.setPrefWidth(120);
                            bar.setPrefHeight(10);
                            bar.getStyleClass().addAll("progress-bar-new");
                            lbl.setText(pct * 100 + "%");
                            lbl.getStyleClass().addAll("primary", "b3", "bold");
                            hbox.getStyleClass().addAll("align-center");
                            setGraphic(hbox);
                        }

                    }
                };
            }
        });

        // Configure Duration column
        durationCol.setSortable(true);
        durationCol.setCellValueFactory(param -> {
            long minutes = 0;
            if(param.getValue() instanceof UserConversation){
                UserConversation uc = (UserConversation) param.getValue();
                if (uc.getCompletedAt() != null && uc.getCreatedAt() != null) {
                    long diff = uc.getCompletedAt().getTime() - uc.getCreatedAt().getTime();
                    minutes = diff / (1000 * 60);
                }
            }else if(param.getValue() instanceof UserSpeakingTest){
                UserSpeakingTest us = (UserSpeakingTest) param.getValue();
                long diff = us.getCompletedAt().getTime() - us.getStartedAt().getTime();
                minutes = diff / (1000 * 60);
            }
            return new ReadOnlyObjectWrapper<>(minutes + " min");
        });

        // Configure Action column (Review button)
        actionCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setOnAction(e -> onReview(getItem()));
            }
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if( empty || item == null ){
                    setGraphic(null);
                }else{
                    if(item instanceof UserConversation){
                        UserConversation uc = (UserConversation) item;
                        switch (uc.getStatusEnum()) {
                            case NOTSTARTED -> {
                                btn.setText(TranslationManager.get().t(TKey.START).get());
                                break;
                            }
                            case IN_PROGRESS -> {
                                btn.setText(TranslationManager.get().t(TKey.CONTINUE).get());
                                break;
                            }
                            case COMPLETED -> {
                                btn.setText(TranslationManager.get().t(TKey.REVIEW).get());
                                break;
                            }
                        }
                    }else if(item instanceof UserSpeakingTest){
                        UserSpeakingTest us = (UserSpeakingTest) item;
                        switch (us.getStatusEnum()) {
                            case NOTSTARTED -> {
                                btn.setText(TranslationManager.get().t(TKey.START).get());
                                break;
                            }
                            case IN_PROGRESS -> {
                                btn.setText(TranslationManager.get().t(TKey.CONTINUE).get());
                                break;
                            }
                            case COMPLETED -> {
                                btn.setText(TranslationManager.get().t(TKey.REVIEW).get());
                                break;
                            }
                        }
                    }
                    setGraphic(btn);
                }
            }
        });

        // Load data
        table.setItems(data);

        reloadTranslations();

//        TranslationManager.get().addLanguageChangeListener(lang -> {
//            Platform.runLater(this::reloadTranslations);
//        });
    }
    private void reloadTranslations(){
        statusCol.textProperty().bind(TranslationManager.get().t(TKey.COLSTATUS));
        scoreBarCol.textProperty().bind(TranslationManager.get().t(TKey.COLSCORE));
        dateCol.textProperty().bind(TranslationManager.get().t(TKey.COLDATE));
        durationCol.textProperty().bind(TranslationManager.get().t(TKey.COLDURATION));
        actionCol.textProperty().bind(TranslationManager.get().t(TKey.COLACTION));
    }

    private void adaptHeight(){
        Node header = table.lookup(".column-header-background");
        double headerHeight = 0;
        if (header instanceof Region) {
            headerHeight = ((Region) header).getHeight();
            System.out.println("Header height: " + headerHeight);
        } else {
            System.out.println("Header node not found or is not a Region.");
            // Handle the case where the header isn't found, maybe use an estimated height
        }

        System.out.println("Header height: " + headerHeight);
        table.setFixedCellSize(fixedRowHeight);
        table.prefHeightProperty().bind(table.fixedCellSizeProperty().multiply(fixedRowHeight).add(headerHeight));
        table.maxHeightProperty().bind(table.fixedCellSizeProperty().multiply(data.size()).add(headerHeight + 1.4));
        System.out.println("Table cell height: " + table.getFixedCellSize());
        System.out.println("Table height: " + table.getMaxHeight());
    }

    public void setConversations(List<T> list) {
        data.setAll(list);
    }

    private void onReview(T item) {
        if(item instanceof UserConversation){
            RootViewModel.getInstance().getNavigationService().navigateTo(ChatView.class, vm -> {
                vm.setUserConversation((UserConversation) item);
            });
        }else if(item instanceof UserSpeakingTest){
            RootViewModel.getInstance().getNavigationService().navigateTo(SpeakingTestPage.class, vm -> {
                vm.setUserSpeakingTest((UserSpeakingTest) item);
            });
        }
    }
}

