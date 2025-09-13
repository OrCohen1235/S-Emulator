package ui.controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.model.VarRow;
import ui.services.ProgramService;
import ui.viewmodel.StartButtonAnimator;
import ui.viewmodel.WorkFX;

import java.util.ArrayList;
import java.util.List;

public class ExecutionController {

    private final int FINISHED_DEBUGGING = -1;

    // Buttons
    @FXML private Button btnRun, btnDebug, btnStepOver, btnClear, btnStop, btnStart, btnResume;

    // Vars table
    @FXML private VarsTableController varsTableController;
    @FXML private ScrollPane inputsScroll;

    // Misc UI
    @FXML private Label lblCycles;
    @FXML private ProgressBar prgExecution;
    @FXML private VBox inputsContainer;
    @FXML private HBox hBoxStart;

    private RootController parent;
    private ProgramService programService;

    // Local debug state
    private boolean debugging = false;
    private int debuggerLevel = 0;
    private boolean runAwaitingStart = false;
    private int sumOfCyclesDebugging = 0;
    private long output=0;

    public void setParent(RootController parent) {
        this.parent = parent;
        this.programService = parent.getProgramService();

        inputsContainer.setManaged(false);
        inputsContainer.setVisible(false);
        hBoxStart.setVisible(false);
        prgExecution.setProgress(0);
        refreshButtons();
    }


    public void onProgramLoaded() {
        rebuildInputFields();
        showInitialVariables();
        inputsScroll.setVvalue(0.0);

        debugging = false;
        debuggerLevel = 0;
        prgExecution.setProgress(0);
        lblCycles.setText("0");
        refreshButtons();

        if (parent != null) parent.clearInstructionHighlight();
    }

    // ========= Actions =========

    @FXML
    private void onRun() {
        onClear();
        inputsContainer.setDisable(false);
        hBoxStart.setVisible(true);
        runAwaitingStart = true;

        debugging = false;
        debuggerLevel = 0;

        refreshButtons();
        btnRun.setDisable(runAwaitingStart);
        btnDebug.setDisable(true);
        btnStop.setDisable(true);
        if (debuggerLevel == 0) {
            highlightCurrentInstruction();
        }

        if (parent != null) parent.clearInstructionHighlight();
    }

    @FXML
    private void onDebug() {
        if (programService.isFinishedDebugging()){
            programService.resetDebugger();
            onProgramLoaded();
        }
        if (debuggerLevel==0){
            highlightCurrentInstruction();
        }
        debugging=true;
        inputsContainer.setDisable(!debugging);
        hBoxStart.setVisible(debugging);
        btnStepOver.setDisable(debugging);
        btnDebug.setDisable(debugging);
        btnRun.setDisable(debugging);
    }

    @FXML
    private void onStepOver() {
        if (!debugging) return;


        output = programService.executeProgramDebugger(parent.getDegree(), debuggerLevel);

        sumOfCyclesDebugging = programService.getEngine().getSumOfCycles();

        lblCycles.setText(String.valueOf(sumOfCyclesDebugging));
        if (programService.isFinishedDebugging()){
            varsTableController.setItems(FXCollections.observableArrayList(programService.getVariablesEND()));
        }

        highlightCurrentInstruction();
        if (debuggerLevel != FINISHED_DEBUGGING) {
            debuggerLevel = Math.max(0, debuggerLevel + 1);
        }
        refreshButtons();
    }

    private void onClear() {
        debugging = false;
        programService.resetMaps();
        onProgramLoaded();

    }

    @FXML
    private void onStop() {
        debuggerLevel =Math.max(0, debuggerLevel - 2);
        programService.addHistory(parent.getDegree(), output);
        programService.resetDebugger();
        debugging = false;
        debuggerLevel = 0;
        prgExecution.setProgress(0);
        refreshButtons();
        onClear();

        // NEW: עצרת דיבוג – נקה סימון
        if (parent != null) parent.clearInstructionHighlight();
    }

    @FXML
    private void onStart() {
        applyVarsValues();
        inputsContainer.setDisable(true);
        hBoxStart.setVisible(false);
        hBoxStart.getStyleClass().add("highlight");

        if (debugging) {
            runAwaitingStart = false;
            refreshButtons();
            return;
        }

        if (runAwaitingStart) {
            runAwaitingStart = false;
            btnRun.setDisable(true);
            doRun();
            refreshButtons();
        }
    }

    @FXML
    private void onResume() {
        debuggerLevel = FINISHED_DEBUGGING;
        onStepOver();
        onClear();
    }

    private void doRun() {
        long y = programService.executeProgram(parent.getDegree());
        lblCycles.setText(String.valueOf(programService.getCycles()));
        programService.resetCycles();
        varsTableController.setItems(FXCollections.observableArrayList(programService.getVariablesEND()));

        programService.resetMaps();
        prgExecution.setProgress(1.0);

        btnStop.setDisable(true);

        if (parent != null) parent.clearInstructionHighlight();
    }

    // ========= Helpers =========

    private void highlightCurrentInstruction() {
        if (parent != null) {
            int idx = programService.getCurrentInstructionIndex();
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
        programService.loadVars(vals);
    }

    private void rebuildInputFields() {
        inputsContainer.getChildren().clear();

        if (programService == null || !programService.hasProgram()) {
            inputsContainer.setManaged(false);
            inputsContainer.setVisible(false);
            return;
        }

        var vars = programService.getVariables();
        boolean has = !vars.isEmpty();
        for (var node : vars) {
            TextField tf = new TextField();
            tf.setPromptText(node.getName());
            inputsContainer.getChildren().add(tf);
        }
        inputsContainer.setManaged(has);
        inputsContainer.setVisible(true);
        inputsContainer.setDisable(true);
        if (!has) return;

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
        inputsContainer.heightProperty().addListener((obs, oldV, newV) -> {
            inputsScroll.setVvalue(1.0);
        });
    }

    private void showInitialVariables() {
        if (programService == null || !programService.hasProgram()) {
            varsTableController.setItems(FXCollections.observableArrayList());

            return;
        }
        varsTableController.setItems(FXCollections.observableArrayList(programService.getAllVars()));

    }

    private void refreshButtons() {
        //boolean atLeastOneStep = debuggerLevel > 0;
        boolean atEnd = debugging && parent != null && programService.isFinishedDebugging();

        btnRun.setDisable(debugging);
        btnDebug.setDisable(debugging);
        btnResume.setDisable(!debugging || atEnd);
        btnStepOver.setDisable(!debugging || atEnd);
        btnStop.setDisable(!debugging ||atEnd);

        parent.getBtnExpand().setDisable(debugging);
        parent.getBtnCollapse().setDisable(debugging);
    }
}
