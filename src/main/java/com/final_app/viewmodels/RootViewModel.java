package com.final_app.viewmodels;

import com.final_app.services.NavigationService;
import de.saxsys.mvvmfx.ViewModel;
import javafx.scene.layout.BorderPane;

public class RootViewModel implements ViewModel {

    // Voor eenvoud maken we hier een statische referentie zodat andere viewmodels (zoals in de navigatiepane)
    // deze makkelijk kunnen bereiken. In een productie‑applicatie kun je ook dependency injection of een service locator gebruiken.
    private static RootViewModel instance;

    private BorderPane contentArea;
    private NavigationService navigationService;

    public RootViewModel() {
        instance = this;
    }

    public static RootViewModel getInstance() {
        return instance;
    }

    public void setContentArea(BorderPane contentArea) {
        this.contentArea = contentArea;
        this.navigationService = new NavigationService(contentArea);
    }

    public NavigationService getNavigationService() {
        return navigationService;
    }
}
