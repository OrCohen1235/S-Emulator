import controllers.DashboardController;
import controllers.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.*;

import java.net.URL;
import java.util.Objects;

public class SimulatorApp extends Application {

    private UserService userService;
    private UserStatsService userStatsService;

    @Override
    public void start(Stage stage) throws Exception {
        String serverUrl = "http://localhost:8080/web_demo_Web";
        userService = new UserService();
        userStatsService = new RemoteUserStatsService(serverUrl);
        ProgramStatsService programStatsService= new RemoteProgramStatsService(serverUrl);
        FunctionStateService functionStateService= new RemoteFunctionStatsService(serverUrl);

        URL url = Objects.requireNonNull(
                SimulatorApp.class.getResource("viewFXML/login.fxml"),
                "login.fxml not found"
        );
        FXMLLoader loader = new FXMLLoader(url);
        Scene scene = new Scene(loader.load());

        LoginController loginController = loader.getController();
        loginController.setServices(userService, userStatsService,programStatsService,functionStateService);

        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(650);
        stage.setTitle("S-Emulator");

        // ← הוסף את זה - logout כשסוגרים את החלון ←
        stage.setOnCloseRequest(event -> {
            performLogout();
        });

        stage.show();
    }

    // ← הוסף מתודה לlogout ←
    private void performLogout() {
        if (userService != null && userService.isLoggedIn()) {
            System.out.println("Logging out user: " + userService.getCurrentUsername());
            userService.logout();
        }
    }

    @Override
    public void stop() throws Exception {
        // גם כשסוגרים דרך Application lifecycle
        performLogout();
        super.stop();
    }

    public static void main(String[] args) { launch(); }
}