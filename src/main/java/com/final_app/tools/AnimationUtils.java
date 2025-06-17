package com.final_app.tools;

import com.final_app.animation.tempo.BounceInterpolator;
import com.final_app.animation.tempo.RealisticBounceInterpolator;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {

    public static void fadeIn(Node node, Duration duration, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        ft.play();
    }

    public static void fadeOut(Node node, Duration duration, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        ft.play();
    }

    public static void slideIn(Node node, Duration duration, double fromX, double fromY, Runnable onFinished) {
        node.setTranslateX(fromX);
        node.setTranslateY(fromY);
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setToX(0);
        tt.setToY(0);
        tt.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        tt.play();
    }

    public static void slideOut(Node node, Duration duration, double toX, double toY, Runnable onFinished) {
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setToX(toX);
        tt.setToY(toY);
        tt.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        tt.play();
    }

    public static void scaleIn(Node node, Duration duration, double fromScaleX, double fromScaleY, Runnable onFinished) {
        node.setScaleX(fromScaleX);
        node.setScaleY(fromScaleY);
        ScaleTransition st = new ScaleTransition(duration, node);
        st.setToX(1);
        st.setToY(1);
        st.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        st.play();
    }

    public static void scaleOut(Node node, Duration duration, double toScaleX, double toScaleY, Runnable onFinished) {
        ScaleTransition st = new ScaleTransition(duration, node);
        st.setToX(toScaleX);
        st.setToY(toScaleY);
        st.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        st.play();
    }

    public static void rotate(Node node, Duration duration, double fromAngle, double toAngle, Runnable onFinished) {
        node.setRotate(fromAngle);
        RotateTransition rt = new RotateTransition(duration, node);
        rt.setToAngle(toAngle);
        rt.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        rt.play();
    }

    public static void fadeInSlideIn(Node node, Duration duration, double fromX, double fromY, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        node.setTranslateX(fromX);
        node.setTranslateY(fromY);
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setToX(0);
        tt.setToY(0);
        RealisticBounceInterpolator dampenedBounceInterpolator = RealisticBounceInterpolator.createWithPreset("basketball");
        tt.setInterpolator(dampenedBounceInterpolator);

        // Stel de translateX en translateY waarden in na het maken van de TranslateTransition

        ParallelTransition pt = new ParallelTransition(node, tt, ft);
        pt.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        pt.play();
    }
    public static void fadeOutSlideOut(Node node, Duration duration, double toX, double toY, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(1);
        ft.setToValue(0);
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setToX(toX);
        tt.setToY(toY);
        tt.setInterpolator(Interpolator.EASE_BOTH);

        // Stel de translateX en translateY waarden in na het maken van de TranslateTransition

        ParallelTransition pt = new ParallelTransition(node, tt, ft);
        pt.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        pt.play();
    }
}
