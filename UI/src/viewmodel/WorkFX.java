package viewmodel;


import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/** Utility למצבי עבודה של כפתור: IDLE -> WORKING -> SUCCESS/ERROR -> RESET */
public final class WorkFX {

    private WorkFX() {}

    public static Handle attach(Button btn) {
        return new Handle(btn);
    }

    public static final class Handle {
        private final Button btn;
        private final String originalText;
        private final String originalStyle;
        private final DropShadow idleGlow;

        // UI קטן שנכניס לכפתור בזמן עבודה: [spinner][label]
        private final HBox workingBox = new HBox(8);
        private final ProgressIndicator spinner = new ProgressIndicator();
        private final Label workingLabel = new Label("Starting");

        private Timeline dotsTimeline;
        private Timeline pulseTimeline;
        private RotateTransition spinnerRotate; // אפשרי, למרות שלProgressIndicator יש אנימציה מובנית
        private boolean disposed = false;

        private enum State { IDLE, WORKING, SUCCESS, ERROR }
        private State state = State.IDLE;

        private Handle(Button btn) {
            this.btn = btn;
            this.originalText = btn.getText();
            this.originalStyle = btn.getStyle();

            // עיצוב בסיסי לכפתור (גרדיאנט עדין)
            if (originalStyle == null || originalStyle.isBlank()) {
                btn.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: white;"
                        + "-fx-padding: 10 22; -fx-background-radius: 18;"
                        + "-fx-background-color: linear-gradient(to right, #4facfe, #00f2fe);");
            }

            idleGlow = new DropShadow(16, Color.web("#6df0ff"));
            btn.setEffect(idleGlow);

            // הכנת תת־UI למצב WORKING
            spinner.setMaxSize(16, 16);
            spinner.setPrefSize(16, 16);
            spinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

            workingBox.setAlignment(Pos.CENTER);
            workingBox.getChildren().addAll(spinner, workingLabel);
        }

        /** מעבר למצב עבודה: נועל כפתור, שם טקסט "Starting..." עם נקודות מתווספות, מציג ספינר ופולס עדין. */
        public void start(String labelBase) {
            if (disposed) return;
            runFx(() -> {
                state = State.WORKING;

                // טקסט בסיס ל"נעשה/חושב"
                workingLabel.setText(labelBase == null || labelBase.isBlank() ? "Working" : labelBase);

                // נועל כפתור, משנה תוכן ל-[spinner + label]
                btn.setDisable(true);
                btn.setGraphic(workingBox);
                btn.setText(""); // מעבירים את הטקסט ללייבל
                btn.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: white;"
                        + "-fx-padding: 10 22; -fx-background-radius: 18;"
                        + "-fx-background-color: linear-gradient(to right, #0ea5ea, #0576ff);");

                // אנימציית נקודות ... על ה-label
                if (dotsTimeline != null) dotsTimeline.stop();
                dotsTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO,     e -> workingLabel.setText(workingLabel.getText().split("\\.")[0]     )),
                        new KeyFrame(Duration.millis(400), e -> workingLabel.setText(baseText() + ".")),
                        new KeyFrame(Duration.millis(800), e -> workingLabel.setText(baseText() + "..")),
                        new KeyFrame(Duration.millis(1200),e -> workingLabel.setText(baseText() + "..."))
                );
                dotsTimeline.setCycleCount(Animation.INDEFINITE);
                dotsTimeline.play();

                // פולס עדין לכפתור (נשימה)
                if (pulseTimeline != null) pulseTimeline.stop();
                pulseTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO,        new KeyValue(btn.scaleXProperty(), 1.0), new KeyValue(btn.scaleYProperty(), 1.0)),
                        new KeyFrame(Duration.millis(550), new KeyValue(btn.scaleXProperty(), 1.05), new KeyValue(btn.scaleYProperty(), 1.05))
                );
                pulseTimeline.setAutoReverse(true);
                pulseTimeline.setCycleCount(Animation.INDEFINITE);
                pulseTimeline.play();
            });
        }

        /** סימון הצלחה קצר + מעבר אוטומטי ל־reset אחרי delayMillis. */
        public void success(String successText, long delayMillis) {
            if (disposed) return;
            runFx(() -> {
                state = State.SUCCESS;
                stopWorkingAnimations();
                btn.setDisable(false);
                btn.setGraphic(null);
                btn.setText(successText == null || successText.isBlank() ? "Done ✓" : successText);
                btn.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: white;"
                        + "-fx-padding: 10 22; -fx-background-radius: 18;"
                        + "-fx-background-color: linear-gradient(to right, #22c55e, #16a34a);");

                // פייד קטן כמשוב
                FadeTransition ft = new FadeTransition(Duration.millis(180), btn);
                ft.setFromValue(0.8);
                ft.setToValue(1.0);
                ft.play();

                scheduleReset(delayMillis);
            });
        }

        /** סימון כישלון קצר (צבע אדום + Shake) + מעבר ל־reset אחרי delayMillis. */
        public void fail(String failText, long delayMillis) {
            if (disposed) return;
            runFx(() -> {
                state = State.ERROR;
                stopWorkingAnimations();
                btn.setDisable(false);
                btn.setGraphic(null);
                btn.setText(failText == null || failText.isBlank() ? "Failed ✕" : failText);
                btn.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: white;"
                        + "-fx-padding: 10 22; -fx-background-radius: 18;"
                        + "-fx-background-color: linear-gradient(to right, #ef4444, #dc2626);");

                // Shake קל
                TranslateTransition shake = new TranslateTransition(Duration.millis(60), btn);
                shake.setFromX(0); shake.setToX(6);
                shake.setCycleCount(6); shake.setAutoReverse(true);
                shake.play();

                scheduleReset(delayMillis);
            });
        }

        /** החזרה למצב IDLE (כפתור רגיל כפי שהיה) */
        public void reset() {
            if (disposed) return;
            runFx(() -> {
                stopWorkingAnimations();
                state = State.IDLE;
                btn.setDisable(false);
                btn.setGraphic(null);
                btn.setText(originalText);
                btn.setStyle(originalStyle == null ? "" : originalStyle);
                btn.setScaleX(1.0);
                btn.setScaleY(1.0);
                btn.setEffect(idleGlow);
            });
        }

        /** עדכון פס־התקדמות אם עברת למצב דטרמיניסטי (אופציונלי) */
        public void setProgress(double p) {
            if (disposed) return;
            runFx(() -> {
                if (state != State.WORKING) return;
                // אם תרצה מצב פרוגרס דטרמיניסטי:
                spinner.setProgress(Math.max(0, Math.min(1, p)));
            });
        }

        /** ניקוי מלא */
        public void dispose() {
            if (disposed) return;
            disposed = true;
            stopWorkingAnimations();
            btn.setGraphic(null);
            btn.setText(originalText);
            btn.setStyle(originalStyle == null ? "" : originalStyle);
            btn.setEffect(null);
        }

        private void stopWorkingAnimations() {
            if (dotsTimeline != null) dotsTimeline.stop();
            if (pulseTimeline != null) pulseTimeline.stop();
            // ProgressIndicator אינהרנטית מסתובב; אין צורך לעצור ידנית
        }

        private void scheduleReset(long delayMillis) {
            if (delayMillis <= 0) return;
            PauseTransition pause = new PauseTransition(Duration.millis(delayMillis));
            pause.setOnFinished(e -> reset());
            pause.play();
        }

        private String baseText() {
            // מחזיר את הטקסט בלי הנקודות אם נוספו
            String t = workingLabel.getText();
            int i = t.indexOf('.');
            return i == -1 ? t : t.substring(0, i);
        }

        private static void runFx(Runnable r) {
            if (Platform.isFxApplicationThread()) r.run(); else Platform.runLater(r);
        }
    }
}
