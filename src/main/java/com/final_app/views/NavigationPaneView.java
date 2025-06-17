package com.final_app.views;

import com.final_app.events.EventBus;
import com.final_app.events.UserChangeEvent;
import com.final_app.globals.GlobalVariables;
import com.final_app.models.NavigationItem;
import com.final_app.tools.SVGUtil;
import com.final_app.viewmodels.NavigationPaneViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class NavigationPaneView implements FxmlView<NavigationPaneViewModel> {
    @FXML private ImageView imgIcon;

    @InjectViewModel
    private NavigationPaneViewModel viewModel;

    @FXML
    private ListView<NavigationItem> navListView;

    int iconSize = 30;

    @FXML
    public void initialize() {
        try {
            InputStream is = new FileInputStream("src/main/resources/com/final_app/icons/appIcon_t.png");
            imgIcon.setImage(new Image(is));
            imgIcon.setFitHeight(iconSize);
            imgIcon.setFitWidth(iconSize);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // 1) bind the list once
        navListView.setItems(viewModel.getNavigationItems());

        // 2) use a fixed cell size + bind prefHeight to item count
        navListView.setFixedCellSize(30); // adjust to your real cell height
        navListView.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(navListView.getItems().size() * navListView.getFixedCellSize(), 300),
                        navListView.getItems()
                )
        );

        // 3) custom cell, *clearing* both text+graphic on empty
        navListView.setFixedCellSize(60);
        navListView.setCellFactory(list -> new ListCell<NavigationItem>() {
            @Override
            protected void updateItem(NavigationItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Image icon = SVGUtil.loadSVG("/com/final_app/icons/" + item.getIcon(), 24, 24, "#ffffff");
                    ImageView iconView = new ImageView(icon);
                    ColorAdjust ca = new ColorAdjust();
                    ca.setHue(0.1);
                    iconView.setEffect(ca);

                    Label title = new Label();
                    title.textProperty().bind(item.getTitle());
                    HBox row = new HBox(10, iconView, title);
                    row.setPrefHeight(30);
                    row.setAlignment(Pos.CENTER_LEFT);

                    row.setPadding(new Insets(6, 12, 6, 12));

                    setText(null);
                    setGraphic(row);
                }
            }
        });

        // 4) navigation on selection
        navListView.getSelectionModel().selectedItemProperty().addListener((obs, old, neo) -> {
            if (neo != null) {
                viewModel.navigate(neo);
            }
        });

        // 5) show/hide when user logs in/out
        navListView.setVisible(false);
        EventBus.getInstance().subscribe(UserChangeEvent.ANY, e -> {
            navListView.setVisible(e.getUser() != null);

            if(e.getUser() != null) {
                navListView.getSelectionModel().selectFirst();
            }
        });
    }
}

