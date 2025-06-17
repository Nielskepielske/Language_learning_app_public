package com.final_app.views.pages;

import com.final_app.viewmodels.RootViewModel;
import com.final_app.views.components.TopBarView;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class RootView implements FxmlView<RootViewModel> {

    @FXML
    private BorderPane contentArea;

    @InjectViewModel
    private RootViewModel viewModel;

    @FXML
    protected BorderPane root;

    @FXML
    private TopBarView topBar;

    @FXML
    public void initialize() {
        // Geef de contentArea door aan de ViewModel zodat de NavigationService geïnitialiseerd kan worden.
        viewModel.setContentArea(contentArea);
        // Optioneel: stel een beginpagina in (bijv. Page1)
        viewModel.getNavigationService().navigateTo(DashBoardView.class);

        Platform.runLater(() -> {
           topBar.setOnUserClicked(event -> {
               System.out.println("User clicked");
               VBox profileOptions = new VBox();
               profileOptions.setPrefHeight(300);
               profileOptions.setPrefWidth(300);
               profileOptions.setSpacing(10);
               profileOptions.setManaged(true);
               profileOptions.setAlignment(Pos.TOP_RIGHT);
               profileOptions.getStyleClass().add("border-debug");
               profileOptions.setTranslateZ(5);
               contentArea.setRight(profileOptions);
               contentArea.getRight().setManaged(false);
           });
        });
    }
}
