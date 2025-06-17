package com.final_app.views.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class CircularProgressBar extends StackPane {
    private final Arc backgroundArc;
    private final Arc progressArc;
    private final Text percentageText;
    private final Group arcsGroup; // Using a Group to maintain precise positioning
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final StringProperty trackColor = new SimpleStringProperty("#26272B");
    private final StringProperty progressColor = new SimpleStringProperty("#FFFFFF");

    // Default values
    private double size = 100;
    private double strokeWidth = 10;

    public CircularProgressBar() {
        // Center everything in the StackPane
        setAlignment(Pos.CENTER);

        // Create a Group to hold both arcs to maintain positioning
        arcsGroup = new Group();

        // Create the background track arc
        backgroundArc = new Arc();
        backgroundArc.setStartAngle(0);
        backgroundArc.setLength(360);
        backgroundArc.setType(ArcType.OPEN);
        backgroundArc.setStroke(Color.web(trackColor.get()));
        backgroundArc.setFill(null);
        backgroundArc.setStrokeLineCap(StrokeLineCap.ROUND);

        // Create the progress arc
        progressArc = new Arc();
        progressArc.setStartAngle(90);  // Start at the top (12 o'clock)
        progressArc.setLength(0);
        progressArc.setType(ArcType.OPEN);
        progressArc.setStroke(Color.web(progressColor.get()));
        progressArc.setFill(null);
        progressArc.setStrokeLineCap(StrokeLineCap.ROUND);

        // Add arcs to the group
        arcsGroup.getChildren().addAll(backgroundArc, progressArc);

        // Create percentage text in the center
        percentageText = new Text("0%");
        percentageText.setFont(Font.font("System", 16));
        percentageText.setFill(Color.WHITE);
        percentageText.setTextAlignment(TextAlignment.CENTER);

        // Add all elements to the StackPane
        getChildren().addAll(arcsGroup, percentageText);

        // Initial setup with default size
        updateSizeAndLayout();

        // Bind the progress property to update the arc
        progress.addListener((observable, oldValue, newValue) -> {
            double progressValue = newValue.doubleValue();
            // Calculate arc length (negative for clockwise direction)
            double arcLength = -progressValue * 360;
            progressArc.setLength(arcLength);
            percentageText.setText(String.format("%.0f%%", progressValue * 100));
        });

        // Bind color properties
        trackColor.addListener((observable, oldValue, newValue) -> {
            backgroundArc.setStroke(Color.web(newValue));
        });

        progressColor.addListener((observable, oldValue, newValue) -> {
            progressArc.setStroke(Color.web(newValue));
        });
    }

    private void updateSizeAndLayout() {
        // Update preferred size
        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);

        // Calculate radius (accounting for stroke width to keep arcs inside the bounds)
        double radius = (size - strokeWidth) / 2;

        // Set arc dimensions - critical to set centerX/centerY to the same value for both arcs
        double centerPoint = size / 2;

        // Update background arc
        backgroundArc.setCenterX(centerPoint);
        backgroundArc.setCenterY(centerPoint);
        backgroundArc.setRadiusX(radius);
        backgroundArc.setRadiusY(radius);
        backgroundArc.setStrokeWidth(strokeWidth);

        // Update progress arc (must match background arc dimensions exactly)
        progressArc.setCenterX(centerPoint);
        progressArc.setCenterY(centerPoint);
        progressArc.setRadiusX(radius);
        progressArc.setRadiusY(radius);
        progressArc.setStrokeWidth(strokeWidth);

        // Update text size based on component size
        double fontSize = Math.max(12, size / 6);
        percentageText.setFont(Font.font("System", fontSize));
    }

    /**
     * Sets the current progress (0.0 to 1.0)
     */
    public void setProgress(double value) {
        // Ensure value is between 0 and 1
        double clampedValue = Math.min(1.0, Math.max(0.0, value));
        progress.set(clampedValue);
    }

    /**
     * Gets the current progress
     */
    public double getProgress() {
        return progress.get();
    }

    /**
     * Sets the track color (background arc)
     */
    public void setTrackColor(String color) {
        trackColor.set(color);
    }

    /**
     * Sets the progress color (foreground arc)
     */
    public void setProgressColor(String color) {
        progressColor.set(color);
    }

    /**
     * Sets the size of the circular progress bar
     */
    public void setSize(double newSize) {
        this.size = newSize;
        updateSizeAndLayout();
    }

    /**
     * Sets the stroke width (thickness) of the progress arcs
     */
    public void setStrokeWidth(double width) {
        this.strokeWidth = width;
        updateSizeAndLayout();
    }

    /**
     * Shows/hides the percentage text in the center
     */
    public void showPercentage(boolean show) {
        percentageText.setVisible(show);
    }

    /**
     * Animates the progress from the current value to the target value
     */
    public void animateProgress(double targetValue, int durationMs) {
        double targetProgress = Math.min(1.0, Math.max(0.0, targetValue));
        double startProgress = progress.get();
        double change = targetProgress - startProgress;

        int steps = 60;
        double stepDuration = durationMs / (double) steps;

        Timeline timeline = new Timeline();
        for (int i = 0; i <= steps; i++) {
            final int step = i;
            double progressValue = startProgress + (change * step / steps);
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(step * stepDuration), e ->
                            setProgress(progressValue)
                    )
            );
        }

        timeline.setCycleCount(1);
        timeline.play();
    }
}