package com.final_app.views.components;

import com.final_app.events.XpEarnedEvent;
import com.final_app.services.UserService.XpTransaction;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Component that displays XP earned notifications
 */
public class XpNotificationView extends VBox {
    @FXML
    private Label lblXpAmount;
    @FXML
    private Label lblSource;

    public XpNotificationView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/com/final_app/views/components/XpNotificationView.fxml"
        ));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            // Apply initial styling
            getStyleClass().add("xp-notification");
            setVisible(false);
            setOpacity(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Show the XP notification based on transaction data
     */
    public void showNotification(XpTransaction transaction) {
        // Update labels
        lblXpAmount.setText("+" + transaction.getAmount() + " XP");
        lblSource.setText(transaction.getDescription());

        // Set special style for level up
        if (transaction.isLeveledUp()) {
            getStyleClass().add("level-up-notification");
            lblXpAmount.setText("LEVEL UP! +" + transaction.getAmount() + " XP");
        } else {
            getStyleClass().remove("level-up-notification");
        }

        // Animate in
        setVisible(true);
        setManaged(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), this);
        slideIn.setFromY(-20);
        slideIn.setToY(0);

        ParallelTransition parallelIn = new ParallelTransition(fadeIn, slideIn);


        // Animate out after delay
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(3));
        fadeOut.setOnFinished(e -> {
            setVisible(false);
            setManaged(false);
        });

        // Play animations
        parallelIn.play();
        fadeOut.setDelay(Duration.seconds(4));
        fadeOut.play();
    }
}

