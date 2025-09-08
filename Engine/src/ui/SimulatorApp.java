package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class SimulatorApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL url = Objects.requireNonNull(
                SimulatorApp.class.getResource("/viewFXML/root.fxml"),
                "root.fxml not found on classpath at /viewFXML/root.fxml"
        );
        FXMLLoader loader = new FXMLLoader(url);
        stage.setScene(new Scene(loader.load()));
        stage.setWidth(1400);
        stage.setHeight(900);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.centerOnScreen();
        stage.setTitle("S-Emulator");
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}
