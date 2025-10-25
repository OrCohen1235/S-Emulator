package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logic.dto.InstructionDTO;
import program.ProgramLoadException;
import services.HistoryService;
import services.ProgramService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RootController {

    @FXML private ProgressBar progressBar;
    @FXML private Label lblStatus;
    @FXML private Spinner<Integer> spnDegree;

    @FXML private Label lblFilePath;
    @FXML private Label lblMaxDegree;

    @FXML private Button btnExpand;
    @FXML private Button btnCollapse;
    @FXML private Button onHighLight;

    @FXML private InstructionsController instructionsController;
    @FXML private ExecutionController executionController;
    @FXML private HistoryController historyController;

    private final ProgramService programService = new ProgramService();
    private final HistoryService historyService = new HistoryService();

    private File selectedFile;
    private Tooltip tooltip;

    @FXML private void initialize() {
        if (instructionsController != null) instructionsController.setParent(this);
        if (executionController != null) executionController.setParent(this);

        if (spnDegree != null) {
            spnDegree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
            spnDegree.setDisable(true);
            spnDegree.valueProperty().addListener((obs, ov, nv) -> {
                if (instructionsController != null) {
                    instructionsController.refresh(getDegree());
                }
            });
        }

        if (lblFilePath  != null) lblFilePath.setText("-");
        if (lblMaxDegree != null) lblMaxDegree.setText("/ 0");

        programService.setHistory(historyService);
        if (historyController != null) {
            historyController.init(historyService, this, executionController);
        }
    }

    public ProgramService getProgramService() { return programService; }

    public int getDegree() {
        return (spnDegree != null && spnDegree.getValue() != null) ? spnDegree.getValue() : 0;
    }

    public void setSpnDegree(int degree) {
        this.spnDegree.getValueFactory().setValue(degree);
    }

    @FXML private void onHighLightVarOrLabel(ActionEvent e) {
        TextField filter = new TextField();
        ListView<String> list = new ListView<>();
        list.setPrefHeight(280);
        VBox root = new VBox(8, filter, list);
        root.setPadding(new Insets(12));
        ObservableList<String> vars = FXCollections.observableArrayList();

        vars.setAll(programService.getAllVarsAndLables());

        FilteredList<String> variables = new FilteredList<>(vars, s -> true);
        filter.textProperty().addListener((obs,o,n) ->
                variables.setPredicate(s -> n==null || n.isBlank() || s.toLowerCase().contains(n.toLowerCase()))
        );
        list.setItems(variables);

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Choose a variable or a label");
        stage.initOwner(((Node)e.getSource()).getScene().getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(new Scene(root, 360, 360));
        stage.setResizable(false);

        list.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2 && list.getSelectionModel().getSelectedItem() != null) {
                highlightVariable(list.getSelectionModel().getSelectedItem());
                stage.close();
            }
        });

        stage.showAndWait();
    }

    @FXML private void onHighLight(ActionEvent e) { onHighLightVarOrLabel(e); }

    private void highlightVariable(String selectedItem) {
        List<Integer> res = new ArrayList<>();
        int index = 0;
        for (InstructionDTO dto : programService.getInstructionsDTO()){
            if (dto.getLabel().equals(selectedItem)){
                res.add(index);
            }
            else {
                String [] command = dto.getCommand().split(" ");
                for (String word : command){
                    if (word.equals(selectedItem)){
                        res.add(index);
                    }
                }
            }
            index++;
        }

        try {
            instructionsController.setHighLightedRowIndexes(res);
        } catch (Throwable t) {
            for (Integer i : res) instructionsController.highlightRow(i);
        }
    }


    public void onLoadFile(File file) {
        if (file == null) return;

        this.selectedFile = file;
        tooltip = new Tooltip(file.getAbsolutePath());

        if (spnDegree != null) spnDegree.setDisable(true);
        if (lblFilePath != null) lblFilePath.setText("Loading… " + file.getName());

        Task<Integer> loadTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                updateMessage("Reading file…");
                updateProgress(0, 1);

                int maxDegree = programService.loadXml(Path.of(file.getPath()));

                updateMessage("Finalizing…");


                updateProgress(1, 1);
                updateMessage("Done");
                return maxDegree;
            }
        };

        showLoadingDialog(loadTask);

        if (progressBar != null) {
            progressBar.progressProperty().bind(loadTask.progressProperty());
            progressBar.visibleProperty().bind(loadTask.runningProperty());
            progressBar.managedProperty().bind(progressBar.visibleProperty());
        }
        if (lblStatus != null) {
            lblStatus.textProperty().bind(loadTask.messageProperty());
            lblStatus.visibleProperty().bind(loadTask.runningProperty());
            lblStatus.managedProperty().bind(lblStatus.visibleProperty());
        }

        loadTask.setOnSucceeded(evt -> {
            int maxDegree = loadTask.getValue();

            if (lblFilePath != null) lblFilePath.setText(file.getAbsolutePath());

            if (spnDegree != null) {
                spnDegree.setDisable(false);
                spnDegree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
            }

            if (instructionsController != null) instructionsController.refresh(getDegree());
            if (executionController != null) executionController.onProgramLoaded();
            if (historyController != null) historyController.clearHistory();

            if (progressBar != null) {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
            }
            if (lblStatus != null) {
                lblStatus.textProperty().unbind();
                lblStatus.setText("");
            }
        });

        loadTask.setOnFailed(evt -> {
            Throwable ex = loadTask.getException();
            if (ex instanceof ProgramLoadException ple) {
                showError("Load failed", ple.getMessage());
            } else {
                showError("Load failed", ex != null ? ex.getMessage() : "Unknown error");
            }

            if (spnDegree != null) spnDegree.setDisable(false);
            if (lblFilePath != null) lblFilePath.setText(file.getAbsolutePath());

            if (progressBar != null) {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
            }
            if (lblStatus != null) {
                lblStatus.textProperty().unbind();
                lblStatus.setText("");
            }
        });

        loadTask.setOnCancelled(evt -> {
            if (spnDegree != null) spnDegree.setDisable(false);
            if (lblFilePath != null) lblFilePath.setText("Cancelled: " + file.getName());

            if (progressBar != null) {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
            }
            if (lblStatus != null) {
                lblStatus.textProperty().unbind();
                lblStatus.setText("");
            }
        });

        Thread t = new Thread(loadTask, "load-xml-task");
        t.setDaemon(true);
        t.start();
    }

    @FXML private void onExpand() {
        if (spnDegree == null || spnDegree.isDisabled()) return;
        int max = getSpinnerMax();
        int cur = getDegree();
        if (cur < max) spnDegree.getValueFactory().setValue(cur + 1);
        if (instructionsController != null) instructionsController.refresh(getDegree());
    }

    @FXML private void onCollapse() {
        if (spnDegree == null || spnDegree.isDisabled()) return;
        int cur = getDegree();
        if (cur > 0) spnDegree.getValueFactory().setValue(cur - 1);
        if (instructionsController != null) instructionsController.refresh(getDegree());
    }

    private void setMaxDegree(int max) {
        int m = Math.max(0, max);
        int current = (spnDegree.getValue() == null) ? 0 : spnDegree.getValue();
        int initial = Math.min(current, m);
        spnDegree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, m, initial));
        if (lblMaxDegree != null) lblMaxDegree.setText("/ " + m);
    }

    private int getSpinnerMax() {
        return (spnDegree != null && spnDegree.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory f)
                ? f.getMax() : 0;
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg == null ? "" : msg);
        a.showAndWait();
    }

    public void highlightInstruction(int index) {
        if (instructionsController != null) {
            instructionsController.highlightRow(index);
        }
    }

    public void clearInstructionHighlight() {
        if (instructionsController != null) {
            instructionsController.clearHighlight();
        }
    }

    public Button getBtnExpand() { return btnExpand; }
    public Button getBtnCollapse() { return btnCollapse; }

    public ExecutionController getExecutionController() { return executionController; }

    private void showLoadingDialog(Task<?> task) {
        ProgressBar pb = new ProgressBar();
        pb.setPrefWidth(300);
        pb.progressProperty().bind(task.progressProperty());

        Label status = new Label();
        status.textProperty().bind(task.messageProperty());

        VBox box = new VBox(10, status, pb);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Stage dialog = new Stage(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(lblFilePath != null ? lblFilePath.getScene().getWindow() : null);
        dialog.setScene(new Scene(box));
        dialog.setTitle("Loading...");
        dialog.setResizable(false);

        task.setOnSucceeded(e -> dialog.close());
        task.setOnFailed(e -> dialog.close());
        task.setOnCancelled(e -> dialog.close());

        dialog.show();
    }

    public void onFunctionSelector(ActionEvent actionEvent) {
        List<String> functions = programService.getFunctionsNames();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(functions.get(0), functions);
        dialog.setTitle("Choose Function");
        dialog.setHeaderText("Function to show");
        dialog.setContentText("Function:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(programService::switchToFunction);
        instructionsController.refresh(0);
        int maxDegree = programService.getMaxDegree();
        setMaxDegree(maxDegree);
        spnDegree.requestFocus();
    }

    public void onFunctionSelector(String functionName,int degree) {

        programService.switchToFunction(functionName);
        instructionsController.refresh(degree);
        int maxDegree = programService.getMaxDegree();
        setMaxDegree(maxDegree);
        spnDegree.requestFocus();
    }

    public void refreshInstructions(){
        instructionsController.refresh(getDegree());
    }

    public void markInstructionsAsExecuted(boolean executed) {
        if (instructionsController != null) {
            instructionsController.markAsExecuted(executed);
        }
    }
}
