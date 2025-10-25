package controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import model.HistoryRow;
import model.ProgramViewModel;
import model.UserViewModel;
import services.ProgramService;
import services.UserService;
import services.UserStatsService;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    // -------- Top bar --------
    @FXML private Label userNameLabel;
    @FXML private Button loadFileButton;
    @FXML private TextField loadedFilePathField;
    @FXML private TextField creditsField;
    @FXML private Button chargeCreditsButton;

    // -------- TR: Available Programs --------
    @FXML private TableView<ProgramViewModel> availableProgramsTable;
    @FXML private TableColumn<ProgramViewModel, String> colProgramName;
    @FXML private TableColumn<ProgramViewModel, String> colProgramUploader;
    @FXML private TableColumn<ProgramViewModel, Number> colProgramInstructions;
    @FXML private TableColumn<ProgramViewModel, Number> colProgramMaxLevel;
    @FXML private TableColumn<ProgramViewModel, Number> colProgramRunCount;
    @FXML private TableColumn<ProgramViewModel, Number> colProgramAvgCost;
    @FXML private Button executeProgramButton;

    // -------- BL: Users History / Statistics --------
    @FXML private TableView<HistoryRow> historyTable;
    @FXML private TableColumn<HistoryRow, String> colUser;
    @FXML private TableColumn<HistoryRow, String> colAction;
    @FXML private TableColumn<HistoryRow, String> colTime;

    // -------- BR: Available Functions --------
    @FXML private ListView<String> functionsListView;

    // -------- TL: Connected Users table --------
    @FXML private TableView<UserViewModel> connectedUsersTable;
    @FXML private TableColumn<UserViewModel, String> colCUName;
    @FXML private TableColumn<UserViewModel, Number> colCUMain;
    @FXML private TableColumn<UserViewModel, Number> colCUFunctions;
    @FXML private TableColumn<UserViewModel, Number> colCUCreds;
    @FXML private TableColumn<UserViewModel, Number> colCUUsed;
    @FXML private TableColumn<UserViewModel, Number> colCURuns;

    // -------- Model-like state --------
    private final ObservableList<String> functions = FXCollections.observableArrayList();
    private final ObservableList<HistoryRow> history = FXCollections.observableArrayList();

    private final ObservableList<UserViewModel> connectedUsers =
            FXCollections.observableArrayList((UserViewModel u) -> new Observable[]{
                    u.nameProperty(), u.mainProgramsProperty(), u.functionsProperty(),
                    u.creditsCurrentProperty(), u.creditsUsedProperty(), u.runsProperty()
            });

    private final ObservableList<ProgramViewModel> programs =
            FXCollections.observableArrayList(p -> new Observable[]{
                    p.programNameProperty(), p.uploaderProperty(),
                    p.instructionCountProperty(), p.maxLevelProperty(),
                    p.runCountProperty(), p.avgCostProperty()
            });

    private int credits = 0;
    private ProgramService programService;
    private RootController rootController;
    private UserStatsService userStatsService;
    private Timeline refreshTimeline;
    private UserService userService;

    // ============== Initialize ==============
    @FXML
    private void initialize() {
        if (loadedFilePathField != null) loadedFilePathField.setEditable(false);
        if (creditsField != null) creditsField.setEditable(false);

        if (historyTable != null) {
            historyTable.setItems(history);
        }

        if (functionsListView != null) functionsListView.setItems(functions);

        if (loadFileButton != null) loadFileButton.setOnAction(e -> onLoadFile());
        if (chargeCreditsButton != null) chargeCreditsButton.setOnAction(e -> onChargeCredits());

        // Available Programs Table
        if (availableProgramsTable != null) {
            availableProgramsTable.setItems(programs);
            if (colProgramName != null)
                colProgramName.setCellValueFactory(c -> c.getValue().programNameProperty());
            if (colProgramUploader != null)
                colProgramUploader.setCellValueFactory(c -> c.getValue().uploaderProperty());
            if (colProgramInstructions != null)
                colProgramInstructions.setCellValueFactory(c -> c.getValue().instructionCountProperty());
            if (colProgramMaxLevel != null)
                colProgramMaxLevel.setCellValueFactory(c -> c.getValue().maxLevelProperty());
            if (colProgramRunCount != null)
                colProgramRunCount.setCellValueFactory(c -> c.getValue().runCountProperty());
            if (colProgramAvgCost != null)
                colProgramAvgCost.setCellValueFactory(c -> c.getValue().avgCostProperty());
        }

        if (executeProgramButton != null) {
            executeProgramButton.setOnAction(e -> onExecuteProgram());
            executeProgramButton.disableProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> availableProgramsTable == null ||
                                    availableProgramsTable.getSelectionModel().getSelectedItem() == null,
                            (availableProgramsTable != null) ?
                                    availableProgramsTable.getSelectionModel().selectedItemProperty() : null
                    )
            );
        }

        // --- Connected users table bindings ---
        if (connectedUsersTable != null) {
            connectedUsersTable.setItems(connectedUsers);
            if (colCUName != null) colCUName.setCellValueFactory(c -> c.getValue().nameProperty());
            if (colCUMain != null) colCUMain.setCellValueFactory(c -> c.getValue().mainProgramsProperty());
            if (colCUFunctions != null) colCUFunctions.setCellValueFactory(c -> c.getValue().functionsProperty());
            if (colCUCreds != null) colCUCreds.setCellValueFactory(c -> c.getValue().creditsCurrentProperty());
            if (colCUUsed != null) colCUUsed.setCellValueFactory(c -> c.getValue().creditsUsedProperty());
            if (colCURuns != null) colCURuns.setCellValueFactory(c -> c.getValue().runsProperty());
        }
        programService = new ProgramService();
        updateCreditsField();
    }

    // ============== Dependency Injection ==============
    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }

    public void setUserStatsService(UserStatsService service) {
        this.userStatsService = service;
        startRefreshLoop();
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
        if (userService != null && userService.isLoggedIn() && userNameLabel != null) {
            userNameLabel.setText(userService.getCurrentUsername());
        }
    }

    // ============== Actions ==============
    @FXML
    private void onLoadFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Load Program File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        Window owner = (loadFileButton != null && loadFileButton.getScene() != null)
                ? loadFileButton.getScene().getWindow()
                : null;

        File f = fc.showOpenDialog(owner);
        if (f != null) {
            if (loadedFilePathField != null) loadedFilePathField.setText(f.getAbsolutePath());
            logAction(currentUserOrDash(), "Loaded file: " + f.getName());
            programService.loadXml(Path.of(f.getPath()));
        }
    }

    @FXML
    private void onChargeCredits() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Charge Credits");
        dialog.setHeaderText(null);
        dialog.setContentText("How many credits to add?");
        dialog.getEditor().setPromptText("e.g. 10");

        dialog.showAndWait().ifPresent(text -> {
            try {
                int delta = Integer.parseInt(text.trim());
                if (delta <= 0) throw new NumberFormatException();
                credits += delta;
                updateCreditsField();
                logAction(currentUserOrDash(), "Charged +" + delta + " credits");
            } catch (NumberFormatException ex) {
                showError("Please enter a positive integer.");
            }
        });
    }

    @FXML
    private void onExecuteProgram() {
        ProgramViewModel program = (availableProgramsTable != null)
                ? availableProgramsTable.getSelectionModel().getSelectedItem()
                : null;
        if (program == null) return;

        logAction(currentUserOrDash(), "Executed program: " + program.getProgramName());
        showInfo("Program executed",
                "Program: " + program.getProgramName() + "\nUploader: " + program.getUploader());
    }

    // ============== Connected Users refresh ==============
    private void startRefreshLoop() {
        if (userStatsService == null) {
            System.err.println("userStatsService is null – refresh loop not started");
            return;
        }
        if (refreshTimeline != null) refreshTimeline.stop();

        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshConnectedUsers()));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();

    }

    private void refreshConnectedUsers() {
        new Thread(() -> {
            try {
                List<UserViewModel> snapshot = userStatsService.fetchConnectedUsers();
                if (snapshot == null) snapshot = List.of();
                final List<UserViewModel> finalSnapshot = snapshot;
                Platform.runLater(() -> mergeSnapshot(finalSnapshot));
            } catch (Exception ex) {
                System.err.println("Failed to refresh users: " + ex.getMessage());
            }
        }, "UserStatsRefresh").start();
    }

    private void mergeSnapshot(List<UserViewModel> snapshot) {
        for (UserViewModel incoming : snapshot) {
            int idx = findIndexByName(incoming.getName());
            if (idx < 0) {
                connectedUsers.add(incoming);
            } else {
                connectedUsers.get(idx).updateFrom(incoming);
            }
        }
        connectedUsers.removeIf(curr ->
                snapshot.stream().noneMatch(s -> s.getName().equals(curr.getName())));
    }

    private int findIndexByName(String name) {
        for (int i = 0; i < connectedUsers.size(); i++) {
            if (connectedUsers.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    public void dispose() {
        if (refreshTimeline != null) refreshTimeline.stop();
    }

    // ============== Program Management ==============
    public void addProgram(ProgramViewModel program) {
        programs.add(program);
    }

    public void removeProgram(String programName) {
        programs.removeIf(p -> p.getProgramName().equals(programName));
    }

    // ============== Helpers ==============
    private void updateCreditsField() {
        if (creditsField != null) {
            creditsField.setText(String.valueOf(credits));
        }

        if (userService != null && userService.isLoggedIn()) {
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return userService.updateCredits(credits);
                }
            };

            task.setOnSucceeded(e -> {
                boolean success = task.getValue();
                if (!success) {
                    Platform.runLater(() -> showError("Failed to update credits on server"));
                }
            });

            new Thread(task).start();
        }
    }

    private void logAction(String user, String action) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // history.add(0, new HistoryRow(user, action, time));
    }

    private String currentUserOrDash() {
        if (userService != null && userService.isLoggedIn()) {
            return userService.getCurrentUsername();
        }
        return "—";
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Error");
        a.showAndWait();
    }
}