package ui.controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.model.VarRow;
import ui.services.ProgramService;

import java.util.ArrayList;
import java.util.List;

public class ExecutionController {
    // Buttons
    @FXML private Button btnRun, btnDebug, btnStepOver, btnStepBack, btnStop, btnStart,btnResume;

    // Vars table
    @FXML private TableView<VarRow> tblVariables;
    @FXML private TableColumn<VarRow, String> colVarName, colVarType, colVarValue;




    // Misc UI
    @FXML private Label lblCycles;
    @FXML private ProgressBar prgExecution;
    @FXML private VBox inputsContainer;
    @FXML private TextField txtOutput;
    @FXML private HBox hBoxStart;

    private RootController parent;
    private ProgramService service;


    // Local debug state
    private boolean debugging = false;
    private int debuggerLevel = 0;
    private boolean runAwaitingStart = false;

    public void setParent(RootController parent) {
        this.parent = parent;
        this.service = parent.getProgramService();

        initVariableTableColumns();
        inputsContainer.setManaged(false);
        inputsContainer.setVisible(false);
        hBoxStart.setVisible(false);
        prgExecution.setProgress(0);
        refreshButtons();
    }


    private void initVariableTableColumns() {
        colVarName.setCellValueFactory(c -> c.getValue().nameProperty());
        colVarType.setCellValueFactory(c -> c.getValue().typeProperty());
        colVarValue.setCellValueFactory(c -> c.getValue().valueProperty());
    }


    public void onProgramLoaded() {
        rebuildInputFields();
        showInitialVariables();

        debugging = false;
        debuggerLevel = 0;
        prgExecution.setProgress(0);
        txtOutput.clear();
        lblCycles.setText("0");
        refreshButtons();

        if (parent != null) parent.clearInstructionHighlight();
    }

    // ========= Actions =========

    @FXML
    private void onRun() {
        btnRun.setText("Re-Run");
        hBoxStart.setVisible(true);
        runAwaitingStart = true;

        debugging = false;
        debuggerLevel = 0;

        refreshButtons();
        if (debuggerLevel == 0){
            highlightCurrentInstruction();
        }

        if (parent != null) parent.clearInstructionHighlight();

    }


    @FXML
    private void onDebug() {
        hBoxStart.setVisible(true);
        debugging = true;
        btnStepBack.setDisable(true);
        refreshButtons();
    }

    @FXML
    private void onStepOver() {
        if (!debugging) return;

        long y = service.executeProgramDebugger(parent.getDegree(), debuggerLevel);
        txtOutput.setText(String.valueOf(y));
        lblCycles.setText(String.valueOf(service.getCycles()));
        tblVariables.setItems(FXCollections.observableArrayList(service.getVariablesEND()));

        highlightCurrentInstruction();
        debuggerLevel = Math.max(0, debuggerLevel + 1);
        refreshButtons();
    }

    @FXML
    private void onStepBack() {
        debuggerLevel = Math.max(0, debuggerLevel - 2);
        onStepOver();

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
    private void onStart() {
        applyVarsValues();
        hBoxStart.setVisible(false);
        hBoxStart.getStyleClass().add("highlight");

        if (debugging) {
            runAwaitingStart = false;
            refreshButtons();
            return;
        }

        if (runAwaitingStart) {
            runAwaitingStart = false;
            doRun();
        }
    }

    private void doRun() {

        long y = service.executeProgram(parent.getDegree());
        txtOutput.setText(String.valueOf(y));
        lblCycles.setText(String.valueOf(service.getCycles()));
        tblVariables.setItems(FXCollections.observableArrayList(service.getVariablesEND()));

        service.resetMaps();
        prgExecution.setProgress(1.0);
        refreshButtons();

        if (parent != null) parent.clearInstructionHighlight();
    }



    // ========= Helpers =========

    private void highlightCurrentInstruction() {
        if (parent != null) {
            int idx = service.getCurrentInstructionIndex();
            parent.highlightInstruction(idx);
        }
    }

    private void applyVarsValues() {
        List<Long> vals = new ArrayList<>();
        int size = inputsContainer.getChildren().size();
        for (var node : inputsContainer.getChildren()) {
            if (inputsContainer.getChildren().get(size - 1) != node) {
                if (node instanceof TextField tf) {
                    String v = tf.getText();
                    try {
                        vals.add((v == null || v.isEmpty()) ? 0L : Long.parseLong(v));
                    } catch (NumberFormatException nfe) {
                        vals.add(0L);
                    }
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
        addTailField(1);

    }
    @FXML
    private void onResume(){
        debuggerLevel=-1;
        onStepOver();
    }

    private void addTailField(int index) {
        TextField tf = new TextField();
        tf.setPromptText("X" + index);

        tf.prefWidthProperty().bind(inputsContainer.widthProperty());

        final ChangeListener<String>[] holder = new ChangeListener[1];

        holder[0] = (obs, oldV, newV) -> {
            if (newV != null && !newV.isEmpty()) {
                tf.textProperty().removeListener(holder[0]);
                addTailField(index + 1);
            }
        };

        tf.textProperty().addListener(holder[0]);

        inputsContainer.getChildren().add(tf);
        inputsContainer.prefWidthProperty().bind(inputsContainer.widthProperty());
        inputsContainer.prefHeightProperty().bind(inputsContainer.heightProperty());
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
        boolean atEnd = debugging && parent != null
                && debuggerLevel >= parent.getInstructionCount()+1;

        btnRun.setDisable(debugging);
        btnDebug.setDisable(debugging);
        btnStepOver.setDisable(!debugging || atEnd);
        btnStepBack.setDisable(!debugging || !atLeastOneStep);
        btnStop.setDisable(!debugging);
        parent.getBtnExpand().setDisable(debugging);
        parent.getBtnCollapse().setDisable(debugging);

    }

    public Button getBtnRun() {
        return btnRun;
    }

    public void setBtnRun(Button btnRun) {
        this.btnRun = btnRun;
    }

    public Button getBtnDebug() {
        return btnDebug;
    }

    public void setBtnDebug(Button btnDebug) {
        this.btnDebug = btnDebug;
    }

    public Button getBtnStepOver() {
        return btnStepOver;
    }

    public void setBtnStepOver(Button btnStepOver) {
        this.btnStepOver = btnStepOver;
    }

    public Button getBtnStepBack() {
        return btnStepBack;
    }

    public void setBtnStepBack(Button btnStepBack) {
        this.btnStepBack = btnStepBack;
    }

    public Button getBtnStop() {
        return btnStop;
    }

    public void setBtnStop(Button btnStop) {
        this.btnStop = btnStop;
    }

    public Button getBtnStart() {
        return btnStart;
    }

    public void setBtnStart(Button btnStart) {
        this.btnStart = btnStart;
    }
}
