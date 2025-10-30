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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import model.FunctionViewModel;
import model.HistoryRow;
import model.ProgramViewModel;
import model.UserViewModel;
import program.ProgramLoadException;
import services.*;

import java.io.File;
import java.io.IOException;
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

    // -------- BL: Run History Table --------
    @FXML private TableView<HistoryRow> historyTable;
    @FXML private TableColumn<HistoryRow, Number> colRunNumber;
    @FXML private TableColumn<HistoryRow, Boolean> colMainProgram;
    @FXML private TableColumn<HistoryRow, String> colNameOrUserString;
    @FXML private TableColumn<HistoryRow, String> colArchitecture;
    @FXML private TableColumn<HistoryRow, Number> colDegree;
    @FXML private TableColumn<HistoryRow, Number> colY;
    @FXML private TableColumn<HistoryRow, Number> colCycles;

    // -------- BR: Available Functions --------
    @FXML private TableView<FunctionViewModel> functionsTable;
    @FXML private TableColumn<FunctionViewModel, String> colFnName;
    @FXML private TableColumn<FunctionViewModel, String> colFnUploadProgram;
    @FXML private TableColumn<FunctionViewModel, String> colFnUploader;
    @FXML private TableColumn<FunctionViewModel, Number> colFnInstrCount;
    @FXML private TableColumn<FunctionViewModel, Number> colFnMaxDegree;
    @FXML private Button executeFunctionButton;

    // -------- TL: Connected Users table --------
    @FXML private TableView<UserViewModel> connectedUsersTable;
    @FXML private TableColumn<UserViewModel, String> colCUName;
    @FXML private TableColumn<UserViewModel, Number> colCUMain;
    @FXML private TableColumn<UserViewModel, Number> colCUFunctions;
    @FXML private TableColumn<UserViewModel, Number> colCUCreds;
    @FXML private TableColumn<UserViewModel, Number> colCUUsed;
    @FXML private TableColumn<UserViewModel, Number> colCURuns;

    // -------- Model-like state --------
    private final ObservableList<HistoryRow> history = FXCollections.observableArrayList();

    private final ObservableList<UserViewModel> connectedUsers =
            FXCollections.observableArrayList((UserViewModel u) -> new Observable[]{
                    u.nameProperty(), u.mainProgramsProperty(), u.functionsProperty(),
                    u.creditsCurrentProperty(), u.creditsUsedProperty(), u.runsProperty()
            });

    private final ObservableList<FunctionViewModel> functions =
            FXCollections.observableArrayList(f -> new Observable[]{
                    f.FunctionNameProperty(), f.UploadProgramNameProperty(),
                    f.uploaderProperty(), f.instructionCountProperty(),
                    f.MaxDegreeProperty()
            });

    private final ObservableList<ProgramViewModel> programs =
            FXCollections.observableArrayList(p -> new Observable[]{
                    p.programNameProperty(), p.uploaderProperty(),
                    p.instructionCountProperty(), p.maxLevelProperty(),
                    p.runCountProperty(), p.avgCostProperty()
            });

    private int credits = 0;
    private ProgramService programService;
    private RootController rootController = new RootController();
    private UserStatsService userStatsService;
    private ProgramStatsService programStatsService;
    private FunctionStateService functionStateService;
    private Timeline refreshFunctionsTimeline;
    private Timeline refreshUsersTimeline;
    private Timeline refreshProgramsTimeline;
    private UserService userService;
    private Scene thisScene = null;

    // ============== Initialize ==============
    @FXML
    private void initialize() {
        if (loadedFilePathField != null) loadedFilePathField.setEditable(false);
        if (creditsField != null) creditsField.setEditable(false);

        // ========== קישור טבלת ההיסטוריה ==========
        if (historyTable != null) {
            historyTable.setItems(history);

            if (colRunNumber != null)
                colRunNumber.setCellValueFactory(c -> c.getValue().runNumberProperty());

            if (colMainProgram != null) {
                colMainProgram.setCellValueFactory(c -> c.getValue().mainProgramProperty());
                // המרה לטקסט Main/Helper
                colMainProgram.setCellFactory(col -> new TableCell<HistoryRow, Boolean>() {
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "" : (item ? "Main" : "Helper"));
                    }
                });
            }

            if (colNameOrUserString != null)
                colNameOrUserString.setCellValueFactory(c -> c.getValue().nameOrUserStringProperty());

            if (colArchitecture != null) {
                colArchitecture.setCellValueFactory(c ->
                        Bindings.createStringBinding(() -> {
                            var arch = c.getValue().getArchitecture();
                            return arch != null ? arch.name() : "";
                        }, c.getValue().architectureProperty())
                );
            }

            if (colDegree != null)
                colDegree.setCellValueFactory(c -> c.getValue().degreeProperty());

            if (colY != null)
                colY.setCellValueFactory(c -> c.getValue().yProperty());

            if (colCycles != null)
                colCycles.setCellValueFactory(c -> c.getValue().cyclesProperty());
        }

        // ========== Functions Table ==========
        if (functionsTable != null) {
            functionsTable.setItems(functions);

            if (colFnName != null)
                colFnName.setCellValueFactory(c -> c.getValue().FunctionNameProperty());

            if (colFnUploadProgram != null)
                colFnUploadProgram.setCellValueFactory(c -> c.getValue().UploadProgramNameProperty());

            if (colFnUploader != null)
                colFnUploader.setCellValueFactory(c -> c.getValue().uploaderProperty());

            if (colFnInstrCount != null)
                colFnInstrCount.setCellValueFactory(c -> c.getValue().instructionCountProperty());

            if (colFnMaxDegree != null)
                colFnMaxDegree.setCellValueFactory(c -> c.getValue().MaxDegreeProperty());
        }

        if (executeFunctionButton != null) {
            executeFunctionButton.disableProperty().bind(
                    functionsTable.getSelectionModel().selectedItemProperty().isNull()
            );
            executeFunctionButton.setOnAction(e -> onExecuteFunction());
        }

        // ========== Buttons ==========
        if (loadFileButton != null) loadFileButton.setOnAction(e -> onLoadFile());
        if (chargeCreditsButton != null) chargeCreditsButton.setOnAction(e -> onChargeCredits());

        // ========== Available Programs Table ==========
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

        // ========== Connected Users Table ==========
        if (connectedUsersTable != null) {
            connectedUsersTable.setItems(connectedUsers);
            if (colCUName != null) colCUName.setCellValueFactory(c -> c.getValue().nameProperty());
            if (colCUMain != null) colCUMain.setCellValueFactory(c -> c.getValue().mainProgramsProperty());
            if (colCUFunctions != null) colCUFunctions.setCellValueFactory(c -> c.getValue().functionsProperty());
            if (colCUCreds != null) colCUCreds.setCellValueFactory(c -> c.getValue().creditsCurrentProperty());
            if (colCUUsed != null) colCUUsed.setCellValueFactory(c -> c.getValue().creditsUsedProperty());
            if (colCURuns != null) colCURuns.setCellValueFactory(c -> c.getValue().runsProperty());

            // ========== User Selection Listener ==========
            connectedUsersTable.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            onUserSelected(newValue);
                        }
                    }
            );
        }

        programService = new ProgramService();
        updateCreditsField();
    }

    // ============== User Selection Handler ==============
    /**
     * מטפל בלחיצה על משתמש בטבלת המשתמשים
     * @param user המשתמש שנבחר
     */
    private void onUserSelected(UserViewModel user) {
        if (user == null) return;

        // רענון אסינכרוני של ההיסטוריה
        refreshUserHistoryAsync(user.getName());
    }

    /**
     * מרענן את טבלת ההיסטוריה עבור משתמש ספציפי - גרסה אסינכרונית
     * @param username שם המשתמש
     */
    private void refreshUserHistoryAsync(String username) {
        new Thread(() -> {
            try {
                List<HistoryRow> userHistory = programService.getHistoryRowList(username);

                Platform.runLater(() -> {
                    history.clear();
                    history.addAll(userHistory);
                });

            } catch (Exception ex) {
                System.err.println("Failed to load history for user " + username + ": " + ex.getMessage());
                Platform.runLater(() -> showError("Failed to load user history for: " + username));
            }
        }, "UserHistoryRefresh-" + username).start();
    }

    /**
     * מנקה את טבלת ההיסטוריה
     */
    private void clearHistory() {
        history.clear();
    }

    /**
     * מנקה את הבחירה בטבלת המשתמשים
     */
    private void clearUserSelection() {
        if (connectedUsersTable != null) {
            connectedUsersTable.getSelectionModel().clearSelection();
        }
        clearHistory();
    }

    // ============== Execute Function ==============
    private void onExecuteFunction() {
        FunctionViewModel functionViewModel = (functionsTable != null)
                ? functionsTable.getSelectionModel().getSelectedItem()
                : null;

        if (functionViewModel == null) return;

        try {
            programService.startProgram(functionViewModel.getUploadProgramName());
            logAction(currentUserOrDash(), "Executed program: " + functionViewModel.getProgramName());
            dispose();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/viewFXML/root.fxml"));
            Parent root = loader.load();

            rootController = loader.getController();
            rootController.onFunctionSelector1(functionViewModel.getProgramName(), 0, functionViewModel.getUploadProgramName());
            rootController.setUserService(userService);
            rootController.setCredits(credits);
            rootController.setDashboardController(this);
            rootController.setPreviousScene(thisScene);
            rootController.updateUserDisplay();

            Scene scene = new Scene(root);
            Stage stage = (Stage) availableProgramsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Program Execution - " + functionViewModel.getProgramName());
            stage.show();

        } catch (ProgramLoadException e) {
            showError("Failed to start program: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showError("Failed to load execution view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============== UI Refresh ==============
    public void refreshUI() {
        if (creditsField != null) {
            creditsField.setText(String.valueOf(credits));
        }
        refreshAvailablePrograms();
        startRefreshUsersLoop();
    }

    public void setCredits(int credits) {
        this.credits = credits;
        userService.updateCredits(credits);
    }

    public void decreaseCredits(int creditsToDecrease) {
        this.credits = this.credits + creditsToDecrease;
        if (credits < 0) {
            credits = 0;
        }
        userService.updateCredits(credits);
    }

    public void setThisScene(Scene thisScene) {
        this.thisScene = thisScene;
    }

    // ============== Dependency Injection ==============
    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }

    public void setUserStatsService(UserStatsService service) {
        this.userStatsService = service;
        startRefreshUsersLoop();
    }

    public void setProgramStatsService(ProgramStatsService service) {
        this.programStatsService = service;
        startRefreshProgramsLoop();
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
        if (userService != null && userService.isLoggedIn() && userNameLabel != null) {
            userNameLabel.setText(userService.getCurrentUsername());
        }
    }

    public void setFunctionStatsService(FunctionStateService functionStateService) {
        this.functionStateService = functionStateService;
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
            try {
                programService.loadXml(Path.of(f.getPath()));
            } catch (Exception ex) {
                showError("Already loaded file: " + f.getName());
            }
            refreshAvailablePrograms();
        }
    }

    @FXML
    public void onChargeCredits() {
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
        rootController.setCredits(credits);
        rootController.updateUserDisplay();
    }

    @FXML
    private void onExecuteProgram() {
        ProgramViewModel program = (availableProgramsTable != null)
                ? availableProgramsTable.getSelectionModel().getSelectedItem()
                : null;

        if (program == null) return;

        try {
            programService.startProgram(program.getProgramName());
            logAction(currentUserOrDash(), "Executed program: " + program.getProgramName());
            dispose();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/viewFXML/root.fxml"));
            Parent root = loader.load();

            rootController = loader.getController();
            rootController.setUserService(userService);
            rootController.setCredits(credits);
            rootController.setDashboardController(this);
            rootController.setPreviousScene(thisScene);
            rootController.updateUserDisplay();

            Scene scene = new Scene(root);
            Stage stage = (Stage) availableProgramsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Program Execution - " + program.getProgramName());
            stage.show();

        } catch (ProgramLoadException e) {
            showError("Failed to start program: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showError("Failed to load execution view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============== Connected Users refresh ==============
    private void startRefreshUsersLoop() {
        if (userStatsService == null) {
            System.err.println("userStatsService is null – refresh loop not started");
            return;
        }
        if (refreshUsersTimeline != null) refreshUsersTimeline.stop();

        refreshUsersTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshConnectedUsers()));
        refreshUsersTimeline.setCycleCount(Animation.INDEFINITE);
        refreshUsersTimeline.play();
    }

    private void refreshConnectedUsers() {
        new Thread(() -> {
            try {
                List<UserViewModel> snapshot = userStatsService.fetchConnectedUsers();
                if (snapshot == null) snapshot = List.of();
                final List<UserViewModel> finalSnapshot = snapshot;
                Platform.runLater(() -> mergeUsersSnapshot(finalSnapshot));
            } catch (Exception ex) {
                System.err.println("Failed to refresh users: " + ex.getMessage());
            }
        }, "UserStatsRefresh").start();
    }

    private void mergeUsersSnapshot(List<UserViewModel> snapshot) {
        for (UserViewModel incoming : snapshot) {
            int idx = findUserIndexByName(incoming.getName());
            if (idx < 0) {
                connectedUsers.add(incoming);
            } else {
                connectedUsers.get(idx).updateFrom(incoming);
            }
        }
        connectedUsers.removeIf(curr ->
                snapshot.stream().noneMatch(s -> s.getName().equals(curr.getName())));
    }

    private int findUserIndexByName(String name) {
        for (int i = 0; i < connectedUsers.size(); i++) {
            if (connectedUsers.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    // ============== Available Programs refresh ==============
    private void startRefreshProgramsLoop() {
        if (programStatsService == null) {
            System.err.println("programStatsService is null – programs refresh loop not started");
            return;
        }
        if (refreshProgramsTimeline != null) refreshProgramsTimeline.stop();

        refreshProgramsTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> refreshAvailablePrograms()));
        refreshProgramsTimeline.setCycleCount(Animation.INDEFINITE);
        refreshProgramsTimeline.play();
        refreshAvailablePrograms();
    }

    private void refreshAvailablePrograms() {
        new Thread(() -> {
            try {
                startRefreshFunctionsLoop();
                List<ProgramViewModel> snapshot = programStatsService.fetchAllPrograms();
                if (snapshot == null) snapshot = List.of();
                final List<ProgramViewModel> finalSnapshot = snapshot;
                Platform.runLater(() -> mergeProgramsSnapshot(finalSnapshot));
            } catch (Exception ex) {
                System.err.println("Failed to refresh programs: " + ex.getMessage());
            }
        }, "ProgramStatsRefresh").start();
    }

    private void mergeProgramsSnapshot(List<ProgramViewModel> snapshot) {
        for (ProgramViewModel incoming : snapshot) {
            int idx = findProgramIndexByName(incoming.getProgramName());
            if (idx < 0) {
                programs.add(incoming);
            } else {
                programs.get(idx).updateFrom(incoming);
            }
        }
        programs.removeIf(curr ->
                snapshot.stream().noneMatch(s -> s.getProgramName().equals(curr.getProgramName())));
    }

    private int findProgramIndexByName(String name) {
        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).getProgramName().equals(name)) return i;
        }
        return -1;
    }

    // ============== Available Functions refresh ==============
    private void startRefreshFunctionsLoop() {
        if (functionStateService == null) {
            System.err.println("functionStatsService is null – functions refresh loop not started");
            return;
        }
        if (refreshFunctionsTimeline != null) refreshFunctionsTimeline.stop();

        refreshFunctionsTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> refreshAvailableFunctions()));
        refreshFunctionsTimeline.setCycleCount(Animation.INDEFINITE);
        refreshFunctionsTimeline.play();
        refreshAvailableFunctions();
    }

    private void refreshAvailableFunctions() {
        new Thread(() -> {
            try {
                List<FunctionViewModel> snapshot = functionStateService.fetchAllFunction();
                if (snapshot == null) snapshot = List.of();
                final List<FunctionViewModel> finalSnapshot = snapshot;
                Platform.runLater(() -> mergeFunctionsSnapshot(finalSnapshot));
            } catch (Exception ex) {
                System.err.println("Failed to refresh functions: " + ex.getMessage());
            }
        }, "FunctionStatsRefresh").start();
    }

    private void mergeFunctionsSnapshot(List<FunctionViewModel> snapshot) {
        for (FunctionViewModel incoming : snapshot) {
            int idx = findFunctionIndexByName(incoming.getProgramName());
            if (idx < 0) {
                functions.add(incoming);
            } else {
                functions.get(idx).updateFrom(incoming);
            }
        }
        functions.removeIf(curr ->
                snapshot.stream().noneMatch(s -> s.getProgramName().equals(curr.getProgramName())));
    }

    private int findFunctionIndexByName(String name) {
        for (int i = 0; i < functions.size(); i++) {
            if (functions.get(i).getProgramName().equals(name)) return i;
        }
        return -1;
    }

    public void dispose() {
        if (refreshUsersTimeline != null) refreshUsersTimeline.stop();
        if (refreshProgramsTimeline != null) refreshProgramsTimeline.stop();
        if (refreshFunctionsTimeline != null) refreshFunctionsTimeline.stop();
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

    public int getAvrageProgramSum(String programName) {
        for (ProgramViewModel program : availableProgramsTable.getItems()) {
            if (program.getProgramName().equalsIgnoreCase(programName)) {
                return (int) program.getAvgCost();
            }
        }
        return -1;
    }
}