package ui.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ui.model.VarRow;
import ui.services.ProgramService;

import java.util.ArrayList;
import java.util.List;

public class ExecutionController {
    // Buttons
    @FXML private Button btnRun, btnDebug, btnStepOver, btnStepBack, btnStop, btnReRun;

    // Vars table
    @FXML private TableView<VarRow> tblVariables;
    @FXML private TableColumn<VarRow, String> colVarName, colVarType, colVarValue;

    // Misc UI
    @FXML private Label lblCycles;
    @FXML private ProgressBar prgExecution;
    @FXML private VBox inputsContainer;
    @FXML private TextField txtOutput;

    private RootController parent;
    private ProgramService service;

    // Local debug state
    private boolean debugging = false;
    private int debuggerLevel = 0;

    public void setParent(RootController parent) {
        this.parent = parent;
        this.service = parent.getProgramService();

        initVariableTableColumns();
        inputsContainer.setManaged(false);
        inputsContainer.setVisible(false);
        prgExecution.setProgress(0);
        refreshButtons();
    }

    private void initVariableTableColumns() {
        colVarName.setCellValueFactory(c -> c.getValue().nameProperty());
        colVarType.setCellValueFactory(c -> c.getValue().typeProperty());
        colVarValue.setCellValueFactory(c -> c.getValue().valueProperty());
    }

    /** נקראת מה-Root אחרי loadXml() */
    public void onProgramLoaded() {
        rebuildInputFields();
        showInitialVariables();
        debugging = false;
        debuggerLevel = 0;
        prgExecution.setProgress(0);
        txtOutput.clear();
        lblCycles.setText("0");
        refreshButtons();
        // NEW: ודא שאין סימון ישן
        if (parent != null) parent.clearInstructionHighlight();
    }

    // ========= Actions =========

    @FXML
    private void onRun() {
        debugging = false;
        debuggerLevel = 0;
        applyVarsValues();

        long y = service.executeProgram(parent.getDegree());
        txtOutput.setText(String.valueOf(y));
        lblCycles.setText(String.valueOf(service.getCycles()));
        tblVariables.setItems(FXCollections.observableArrayList(service.getVariablesEND()));

        service.resetMaps();
        prgExecution.setProgress(1.0);
        refreshButtons();

        // NEW: בריצה רגילה – לא מסמנים שורה
        if (parent != null) parent.clearInstructionHighlight();
    }

    @FXML
    private void onDebug() {
        debugging = true;
        debuggerLevel = 0;
        refreshButtons();

        // NEW: בתחילת דיבוג – סמן את השורה הראשונה אם קיימת
        if (parent != null) parent.highlightInstruction(0);
    }

    @FXML
    private void onStepOver() {
        if (!debugging) return;

        if (debuggerLevel == 0) applyVarsValues();
        debuggerLevel = Math.max(0, debuggerLevel + 1);

        long y = service.executeProgramDebugger(parent.getDegree(), debuggerLevel);
        txtOutput.setText(String.valueOf(y));
        lblCycles.setText(String.valueOf(service.getCycles()));
        tblVariables.setItems(FXCollections.observableArrayList(service.getVariablesEND()));
        rebuildInputFields();
        prgExecution.setProgress(1.0);

        // NEW: סמן את השורה הנוכחית לפי המנוע
        highlightCurrentInstruction();

        refreshButtons();
    }

    @FXML
    private void onStepBack() {
        if (!debugging) return;

        if (debuggerLevel == 0) applyVarsValues();
        debuggerLevel = Math.max(1, debuggerLevel - 1);

        long y = service.executeProgramDebugger(parent.getDegree(), debuggerLevel);
        txtOutput.setText(String.valueOf(y));
        lblCycles.setText(String.valueOf(service.getCycles()));
        tblVariables.setItems(FXCollections.observableArrayList(service.getVariablesEND()));
        service.resetMaps();
        rebuildInputFields();
        prgExecution.setProgress(1.0);

        // NEW: סמן את השורה הנוכחית לפי המנוע
        highlightCurrentInstruction();

        refreshButtons();
    }

    @FXML
    private void onStop() {
        debugging = false;
        debuggerLevel = 0;
        prgExecution.setProgress(0);
        refreshButtons();

        // NEW: עצרת דיבוג – נקה סימון
        if (parent != null) parent.clearInstructionHighlight();
    }

    @FXML
    public void onReRun(ActionEvent actionEvent) {
        onRun();
    }

    // ========= Helpers =========

    private void highlightCurrentInstruction() { // NEW
        if (parent != null) {
            int idx = service.getCurrentInstructionIndex();
            parent.highlightInstruction(idx);
        }
    }

    private void applyVarsValues() {
        List<Long> vals = new ArrayList<>();
        for (var node : inputsContainer.getChildren()) {
            if (node instanceof TextField tf) {
                String v = tf.getText();
                try {
                    vals.add((v == null || v.isEmpty()) ? 0L : Long.parseLong(v));
                } catch (NumberFormatException nfe) {
                    vals.add(0L);
                }
            }
        }
        service.loadVars(vals);
    }

    private void rebuildInputFields() {
        inputsContainer.getChildren().clear();

        if (service == null || !service.hasProgram()) {
            inputsContainer.setManaged(false);
            inputsContainer.setVisible(false);
            return;
        }

        var vars = service.getVariables();
        boolean has = !vars.isEmpty();
        inputsContainer.setManaged(has);
        inputsContainer.setVisible(has);
        if (!has) return;

        for (var v : vars) {
            TextField tf = new TextField();
            tf.setPromptText(v.getName());
            inputsContainer.getChildren().add(tf);
        }
    }

    private void showInitialVariables() {
        if (service == null || !service.hasProgram()) {
            tblVariables.setItems(FXCollections.observableArrayList());
            return;
        }
        tblVariables.setItems(FXCollections.observableArrayList(service.getVariables()));
    }

    private void refreshButtons() {
        boolean atLeastOneStep = debuggerLevel > 0;

        btnRun.setDisable(debugging);
        btnDebug.setDisable(debugging);
        btnStepOver.setDisable(!debugging);
        btnStepBack.setDisable(!debugging || !atLeastOneStep);
        btnStop.setDisable(!debugging);
        if (btnReRun != null) btnReRun.setDisable(false);
    }
}
