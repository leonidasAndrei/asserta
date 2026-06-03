package com.leonidasAndrei.asserta.utils;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AnimationHelper {

    public static void runDelayed(Runnable action, long millis) {
        PauseTransition pause = new PauseTransition(Duration.millis(millis));
        pause.setOnFinished(e -> action.run());
        pause.play();
    }

    public static void animateCardLift(StackPane pane, double targetY) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), pane);
        tt.setToY(targetY);
        tt.play();
    }

    public static void pulseNode(VBox node) {
        if (node.getProperties().containsKey("active-pulse")) return;

        ScaleTransition st = new ScaleTransition(Duration.millis(900), node);
        st.setFromX(1.0); st.setToX(1.04); st.setFromY(1.0); st.setToY(1.04);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);

        node.getProperties().put("active-pulse", st);
        st.play();
    }

    public static void stopPulseNode(VBox node) {
        ScaleTransition existing = (ScaleTransition) node.getProperties().remove("active-pulse");
        if (existing != null) existing.stop();
        node.setScaleX(1.0);
        node.setScaleY(1.0);
    }

    public static double[] getBotSeatScenePosition(int seatIndex) {
        return switch (seatIndex) {
            case 1 -> new double[]{130, 480};
            case 2 -> new double[]{640, 85};
            case 3 -> new double[]{1150, 480};
            default -> new double[]{640, 770};
        };
    }
}