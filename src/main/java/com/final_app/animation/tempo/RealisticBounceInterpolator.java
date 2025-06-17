package com.final_app.animation.tempo;

import javafx.animation.Interpolator;

public class RealisticBounceInterpolator extends Interpolator {

    private double amplitude;     // Initial bounce height
    private double dampingFactor; // How quickly the bounces decrease
    private double frequency;     // How frequent the bounces occur
    private double phaseShift;    // Adjusts the starting position
    private double endThreshold;  // Controls when we start transitioning to final state

    public RealisticBounceInterpolator(double amplitude, double dampingFactor, double frequency) {
        this.amplitude = amplitude;
        this.dampingFactor = dampingFactor;
        this.frequency = frequency;
        this.phaseShift = Math.PI / 2; // Start at peak of sine wave
        this.endThreshold = 0.85; // Start smoothing to final value at 85% of animation
    }

    @Override
    protected double curve(double t) {
        // Ensure the animation ends exactly at 1.0
        if (t >= 1.0) {
            return 1.0;
        }

        // Calculate the bounce effect
        double decay = Math.exp(-dampingFactor * t);
        double bounce = Math.abs(Math.sin(frequency * t * Math.PI * 2 + phaseShift));
        double bounceValue = 1.0 - (amplitude * decay * bounce);

        // Create a smooth transition to the final value
        if (t > endThreshold) {
            // Calculate how far we are into the transition period (0 to 1)
            double transitionProgress = (t - endThreshold) / (1.0 - endThreshold);

            // Use a smooth ease-in function for the transition
            double transitionFactor = smoothStep(transitionProgress);

            // Linear interpolation between bounce value and final value
            return bounceValue * (1 - transitionFactor) + 1.0 * transitionFactor;
        }

        return bounceValue;
    }

    // Smooth step function for easing (cubic Hermite interpolation)
    private double smoothStep(double x) {
        // Clamp input to 0..1 range
        x = Math.max(0, Math.min(1, x));
        // Smooth cubic function: 3x² - 2x³
        return x * x * (3 - 2 * x);
    }

    // Utility method to easily create preset bounce effects
    public static RealisticBounceInterpolator createWithPreset(String presetName) {
        switch (presetName.toLowerCase()) {
            case "basketball":
                return new RealisticBounceInterpolator(1.0, 5.0, 2.5);
            case "rubber":
                return new RealisticBounceInterpolator(0.8, 3.0, 3.5);
            case "subtle":
                return new RealisticBounceInterpolator(0.5, 4.0, 2.0);
            case "dramatic":
                return new RealisticBounceInterpolator(1.2, 2.5, 3.0);
            default:
                return new RealisticBounceInterpolator(1.0, 4.0, 2.5);
        }
    }
}
