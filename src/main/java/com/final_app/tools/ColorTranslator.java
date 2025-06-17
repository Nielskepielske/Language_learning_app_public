package com.final_app.tools;

import javafx.scene.paint.Color;

public class ColorTranslator {
    // Offsets (background → foreground)
    private static final double DELTA_H = 14.0;    // degrees
    private static final double DELTA_S = 0.35;    // [0..1]
    private static final double DELTA_L = 0.47;    // [0..1]

    /** Converts a background hex (e.g. "#100E2A") to the calculated text hex. */
    public static String backgroundToText(String bgHex) {
        double[] hsl = hexToHSL(bgHex);
        double h = (hsl[0] + DELTA_H) % 360;
        double s = clamp(hsl[1] + DELTA_S, 0, 1);
        double l = clamp(hsl[2] + DELTA_L, 0, 1);
        return hslToHex(h, s, l);
    }

    /** Converts a text hex back to the calculated background hex. */
    public static String textToBackground(String textHex) {
        double[] hsl = hexToHSL(textHex);
        double h = (hsl[0] - DELTA_H + 360) % 360;
        double s = clamp(hsl[1] - DELTA_S, 0, 1);
        double l = clamp(hsl[2] - DELTA_L, 0, 1);
        return hslToHex(h, s, l);
    }

    // ──────────────── HSL / RGB / HEX helpers ─────────────────

    /** Parses "#RRGGBB" to an HSL triple [h (0–360), s (0–1), l (0–1)]. */
    private static double[] hexToHSL(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        double rd = r/255.0, gd = g/255.0, bd = b/255.0;
        double max = Math.max(rd, Math.max(gd, bd));
        double min = Math.min(rd, Math.min(gd, bd));
        double l = (max + min) / 2.0;

        double h, s;
        if (max == min) {
            h = 0;
            s = 0;
        } else {
            double d = max - min;
            s = d / (1 - Math.abs(2*l - 1));
            if (max == rd) {
                h = ((gd - bd) / d + (gd < bd ? 6 : 0)) * 60;
            } else if (max == gd) {
                h = ((bd - rd) / d + 2) * 60;
            } else {
                h = ((rd - gd) / d + 4) * 60;
            }
        }
        return new double[]{ h, s, l };
    }

    /** Converts HSL (h in 0–360, s/l in 0–1) back to "#RRGGBB". */
    private static String hslToHex(double h, double s, double l) {
        double c = (1 - Math.abs(2*l - 1)) * s;
        double x = c * (1 - Math.abs((h/60) % 2 - 1));
        double m = l - c/2;
        double r1, g1, b1;
        if      (h < 60)  { r1=c; g1=x; b1=0; }
        else if (h < 120) { r1=x; g1=c; b1=0; }
        else if (h < 180) { r1=0; g1=c; b1=x; }
        else if (h < 240) { r1=0; g1=x; b1=c; }
        else if (h < 300) { r1=x; g1=0; b1=c; }
        else              { r1=c; g1=0; b1=x; }

        int r = (int)Math.round((r1 + m) * 255);
        int g = (int)Math.round((g1 + m) * 255);
        int b = (int)Math.round((b1 + m) * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }
    /**
     * Converts a JavaFX Color object to its hexadecimal representation #RRGGBBAA.
     * RR = Red, GG = Green, BB = Blue, AA = Alpha (Opacity).
     * Each component is a two-digit hexadecimal value (00-FF).
     *
     * @param color The JavaFX Color to convert.
     * @return The hexadecimal string representation of the color (e.g., "#FF0000FF" for opaque red).
     */
    public static String toHexRGBA(Color color) {
        if (color == null) {
            return "#00000000"; // Or throw an IllegalArgumentException
        }

        // Get Red, Green, Blue, and Opacity components (values from 0.0 to 1.0)
        // Convert them to integer values from 0 to 255
        int r = (int) Math.round(color.getRed() * 255.0);
        int g = (int) Math.round(color.getGreen() * 255.0);
        int b = (int) Math.round(color.getBlue() * 255.0);
        int a = (int) Math.round(color.getOpacity() * 255.0);

        // Format as a two-digit hexadecimal string for each component
        // and concatenate with a leading '#'
        return String.format("#%02X%02X%02X%02X", r, g, b, a);
    }

    /**
     * Converts a JavaFX Color object to its hexadecimal representation #RRGGBB.
     * This format omits the alpha component and is common for fully opaque colors.
     *
     * @param color The JavaFX Color to convert.
     * @return The hexadecimal string representation of the color (e.g., "#FF0000" for red).
     */
    public static String toHexRGB(Color color) {
        if (color == null) {
            return "#000000"; // Or throw an IllegalArgumentException
        }

        int r = (int) Math.round(color.getRed() * 255.0);
        int g = (int) Math.round(color.getGreen() * 255.0);
        int b = (int) Math.round(color.getBlue() * 255.0);

        return String.format("#%02X%02X%02X", r, g, b);
    }

    /** Clamps v into the [min..max] range. */
    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}

