import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class SimulatorApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL url = Objects.requireNonNull(
                getClass().getResource("/s-emulator-ui.fxml"),
                "FXML not found on classpath at /s-emulator-ui.fxml"
        );
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load(); // <-- כאן נופל
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("S-Emulator");
            stage.show();
        } catch (Exception ex) {
            System.err.println("LOAD FAILED: " + ex);
            Throwable t = ex;
            while ((t = t.getCause()) != null) {
                System.err.println("CAUSE: " + t.getClass().getName() + " - " + t.getMessage());
            }
            throw ex; // להשאיר כדי לראות stacktrace מלא
        }
    }
    public static void main(String[] args) { launch(args); }
}
