package com.final_app.animation.tempo;

import javafx.animation.Interpolator;

public class BounceInterpolator extends Interpolator {

    private double amplitude;
    private double frequency;

    public BounceInterpolator(double amplitude, double frequency) {
        this.amplitude = amplitude;
        this.frequency = frequency;
    }

    @Override
    protected double curve(double t) {
        double adjustedT = t / frequency; // Pas de tijd aan met de frequentie
        double bounce = 0;

        if (adjustedT < 1) {
            bounce = amplitude * adjustedT * (1 - adjustedT); // Gebruik een parabool om de bounce te creëren
        }

        return t + bounce; // Voeg de bounce toe aan de lineaire voortgang
    }
}
