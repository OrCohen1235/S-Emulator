package controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.FunctionStateService;
import services.ProgramStatsService;
import services.UserService;
import services.UserStatsService;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private UserService userService;
    private UserStatsService userStatsService;
    private ProgramStatsService programStatsService;
    private FunctionStateService functionStateService;

    // ← הוסף את המתודה הזו ←
    public void setServices(UserService userService, UserStatsService userStatsService, ProgramStatsService programStatsService, FunctionStateService functionStateService) {
        this.userService = userService;
        this.userStatsService = userStatsService;
        this.programStatsService = programStatsService;
        this.functionStateService = functionStateService;
    }

    @FXML
    public void initialize() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText();

        if (username == null || username.trim().isEmpty()) {
            showError("Username is required");
            return;
        }

        username = username.trim();
        final String finalUsername = username;

        loginButton.setDisable(true);
        hideError();

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() {
                return userService.login(finalUsername);
            }
        };

        loginTask.setOnSucceeded(e -> {
            boolean success = loginTask.getValue();
            if (success) {
                navigateToMain(finalUsername);
            } else {
                loginButton.setDisable(false);
                showError("Login failed - username may already be taken");
            }
        });

        loginTask.setOnFailed(e -> {
            loginButton.setDisable(false);
            showError("Connection error - please try again");
        });

        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void navigateToMain(String username) {
        try {
            URL url = Objects.requireNonNull(
                    LoginController.class.getResource("/viewFXML/newroot.fxml"),
                    "newroot.fxml not found on classpath at /viewFXML/newroot.fxml"
            );

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.setUserService(userService);
            dashboardController.setUserStatsService(userStatsService); // ← הוסף את השורה הזו ←
            dashboardController.setProgramStatsService(programStatsService);
            dashboardController.setFunctionStatsService(functionStateService);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("S-Emulator - " + username);

        } catch (IOException e) {
            loginButton.setDisable(false);
            showError("Failed to load main screen");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }
}