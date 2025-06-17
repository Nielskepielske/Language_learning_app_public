package com.final_app.views.pages;

import com.final_app.events.XpEarnedEvent;
import com.final_app.views.components.XpNotificationView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

/**
 * Extension to RootView to handle XP notifications globally
 */
public class EnhancedRootView extends RootView {
    private final XpNotificationView xpNotification = new XpNotificationView();

    @FXML
    private StackPane overlayContainer;

    @FXML
    @Override
    public void initialize() {
        super.initialize();

        // Set up the overlay container for notifications
        if (overlayContainer == null) {
            overlayContainer = new StackPane();
            overlayContainer.setMouseTransparent(true);
            overlayContainer.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.getChildren().add(overlayContainer);
        }

        // Add XP notification to overlay
        overlayContainer.getChildren().add(xpNotification);

        // Position the notification
        xpNotification.setTranslateX(20);
        xpNotification.setTranslateY(70); // Below top bar

        // Add event listener for XP events
        root.addEventHandler(XpEarnedEvent.ANY, event -> {
            xpNotification.showNotification(event.getTransaction());
        });
    }
}
