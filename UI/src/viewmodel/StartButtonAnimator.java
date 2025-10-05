package viewmodel;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class StartButtonAnimator {

    private StartButtonAnimator() {}

    public static Handle attach(Button btn) {
        return attach(btn, Duration.millis(650), Color.web("#4facfe"), Color.web("#00f2fe"));
    }

    public static Handle attach(Button btn, Duration breathingDuration, Color c1, Color c2) {
        if (btn == null) throw new IllegalArgumentException("btn is null");

        final String originalStyle = btn.getStyle();

        String base = baseStyle(c1, c2, Color.WHITE);
        btn.setStyle(base);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#6df0ff"));
        glow.setRadius(18);
        btn.setEffect(glow);

        ScaleTransition breathing = new ScaleTransition(breathingDuration, btn);
        breathing.setFromX(1.0);
        breathing.setFromY(1.0);
        breathing.setToX(1.08);
        breathing.setToY(1.08);
        breathing.setAutoReverse(true);
        breathing.setCycleCount(Animation.INDEFINITE);

        Timeline gradient = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(btn.styleProperty(), baseStyle(c1, c2, Color.WHITE))),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(btn.styleProperty(), baseStyle(c2, c1, Color.WHITE)))
        );
        gradient.setAutoReverse(true);
        gradient.setCycleCount(Animation.INDEFINITE);

        breathing.play();
        gradient.play();

        return new Handle(btn, breathing, gradient, originalStyle, c1, c2);
    }

    private static String baseStyle(Color c1, Color c2, Color text) {
        String cs1 = toHex(c1), cs2 = toHex(c2), ts = toHex(text);
        return String.join("",
                "-fx-font-size: 10px;",
                "-fx-font-weight: 700;",
                "-fx-text-fill: ", ts, ";",
                "-fx-padding: 10 22;",
                "-fx-background-radius: 20;",
                "-fx-background-insets: 0;",
                "-fx-cursor: hand;",
                "-fx-background-color: linear-gradient(to right, ", cs1, ", ", cs2, ");"
        );
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int)Math.round(c.getRed()*255),
                (int)Math.round(c.getGreen()*255),
                (int)Math.round(c.getBlue()*255));
    }

    public static final class Handle {
        private final Button btn;
        private final ScaleTransition breathing;
        private final Timeline gradient;
        private final String originalStyle;
        private final Color c1, c2;
        private boolean disposed = false;

        private Handle(Button btn, ScaleTransition breathing, Timeline gradient,
                       String originalStyle, Color c1, Color c2) {
            this.btn = btn;
            this.breathing = breathing;
            this.gradient = gradient;
            this.originalStyle = originalStyle;
            this.c1 = c1; this.c2 = c2;
        }

        public void playClick() {
            if (disposed) return;
            Runnable r = () -> {
                ScaleTransition click = new ScaleTransition(Duration.millis(140), btn);
                click.setFromX(btn.getScaleX());
                click.setFromY(btn.getScaleY());
                click.setToX(btn.getScaleX() + 0.15);
                click.setToY(btn.getScaleY() + 0.15);
                click.setAutoReverse(true);
                click.setCycleCount(2);

                Timeline flash = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(btn.styleProperty(),
                                        baseStyle(Color.WHITE, c2, Color.BLACK))),
                        new KeyFrame(Duration.millis(260),
                                new KeyValue(btn.styleProperty(),
                                        baseStyle(c1, c2, Color.WHITE)))
                );

                click.play();
                flash.play();
            };
            if (Platform.isFxApplicationThread()) r.run(); else Platform.runLater(r);
        }

        public void pause() {
            if (disposed) return;
            breathing.pause();
        }

        public void resume() {
            if (disposed) return;
            breathing.play();
            gradient.play();
        }

        public void dispose() {
            if (disposed) return;
            disposed = true;
            breathing.stop();
            gradient.stop();
            btn.setStyle(originalStyle);
            btn.setEffect(null);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        }
    }
}

