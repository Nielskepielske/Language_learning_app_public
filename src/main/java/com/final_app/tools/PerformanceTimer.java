package com.final_app.tools;

import java.util.HashMap;
import java.util.Map;

public class PerformanceTimer {

    private static final Map<String, Long> startTimes = new HashMap<>();

    /**
     * Starts the timer for a given label.
     */
    public static void start(String label) {
        startTimes.put(label, System.nanoTime());
    }

    /**
     * Stops the timer and prints the elapsed time in milliseconds.
     */
    public static void stop(String label) {
        Long startTime = startTimes.get(label);
        if (startTime == null) {
            System.out.println("⚠️ No timer started for: " + label);
            return;
        }
        long elapsedNs = System.nanoTime() - startTime;
        double elapsedMs = elapsedNs / 1_000_000.0;
        System.out.printf("⏱ %s took %.3f ms%n", label, elapsedMs);
        startTimes.remove(label); // optional: keep timers clean
    }
}

