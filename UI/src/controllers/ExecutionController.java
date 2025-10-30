package controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.ProgramService;

import java.util.ArrayList;
import java.util.List;

public class ExecutionController {

    private final int FINISHED_DEBUGGING = -1;
    private boolean onReRun = false;
    private List<Long> reRunInputs = new ArrayList<>();

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
        varsTableController.clearVarsTable();
        rebuildInputFields();
        showInitialVariables();
        inputsScroll.setVvalue(0.0);


        debugging = false;
        debuggerLevel = 0;
        prgExecution.setProgress(0);
        lblCycles.setText("0");
        if (parent != null) parent.markInstructionsAsExecuted(false);
        refreshButtons();

        if (parent != null) parent.clearInstructionHighlight();
    }

    // ========= Actions =========

    @FXML
    private void onRun() {
        varsTableController.clearVarsTable();
        onClear();
        inputsContainer.setDisable(false);
        hBoxStart.setVisible(true);
        runAwaitingStart = true;


        debugging = false;
        debuggerLevel = 0;

        prefillInputsIfNeeded();

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
            programService.resetCycles();
            onProgramLoaded();
        }
        if (debuggerLevel==0){
            onProgramLoaded();
            highlightCurrentInstruction();
        }
        debugging=true;
        inputsContainer.setDisable(!debugging);
        hBoxStart.setVisible(debugging);
        btnStepOver.setDisable(debugging);
        btnDebug.setDisable(debugging);
        btnRun.setDisable(debugging);
        prefillInputsIfNeeded();
    }

    @FXML
    private void onStepOver() {
        if (!debugging) return;
        output = programService.executeProgramDebugger(parent.getDegree(), debuggerLevel);
        if (output == -1){
            parent.showError("Error", "Not enough credits to run!");
            parent.onReturnToDashboard();
            return;
        }
        int sumInstruction = programService.getCycles();
        parent.decreaseCredits(sumInstruction - sumOfCyclesDebugging);
        sumOfCyclesDebugging = programService.getCycles();


        lblCycles.setText(String.valueOf(sumOfCyclesDebugging));
        varsTableController.setItems(FXCollections.observableArrayList(programService.getVarsAtEndRun()));

        highlightCurrentInstruction();
        if (debuggerLevel != FINISHED_DEBUGGING) {
            debuggerLevel = Math.max(0, debuggerLevel + 1);
        }
        if (programService.isFinishedDebugging()){
            debugging = false;
            refreshButtons();
            return;
        }
        refreshButtons();
    }

    private void onClear() {
        debugging = false;
        programService.resetMaps();
        programService.resetCycles();
        parent.markInstructionsAsExecuted(false);
        onProgramLoaded();
        varsTableController.setdebugger(false);

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

        if (parent != null) parent.clearInstructionHighlight();
    }

    @FXML
    private void onStart() {
        try{applyVarsValues();}
        catch(Exception e){
            checkInputs(e);
            return;
        }

        if (parent.getSumOfCurrentArchitecture() + parent.getAvrageProgram(programService.getProgramName()) > parent.getCredits()){
            parent.showError("Error", "Credits lower than average needed + architecture required!");
            return;
        }
        else{
            parent.decreaseCredits(parent.getSumOfCurrentArchitecture());
            parent.clearHighlightedRows();
        }

        inputsContainer.setDisable(true);
        hBoxStart.setVisible(false);
        hBoxStart.getStyleClass().add("highlight");
        varsTableController.setdebugger(debugging);

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

    private void checkInputs(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Invalid Input");
        alert.show();
        btnRun.setDisable(false);
        btnDebug.setDisable(false);
        for (var tf: inputsContainer.getChildren()){
            TextField inputField = (TextField) tf;
            inputField.clear();
        }
    }

    @FXML
    private void onResume() {
        debuggerLevel = FINISHED_DEBUGGING;
        onStepOver();
    }

    private void doRun() {
        long  y = programService.executeProgram(parent.getDegree());
        if (y == -1){
            parent.showError("Error", "Not enough credits to run!");
            parent.onReturnToDashboard();
            return;
        }
        lblCycles.setText(String.valueOf(programService.getCycles()));
        parent.decreaseCredits(programService.getCycles());
        varsTableController.setItems(FXCollections.observableArrayList(programService.getVarsAtEndRun()));
        parent.markInstructionsAsExecuted(true);
        parent.refreshInstructions();

        programService.resetCycles();
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
                if (node instanceof TextField tf) {
                    String v = tf.getText();
                    try {
                        vals.add((v == null || v.isEmpty()) ? 0L : Long.parseLong(v));
                    } catch (NumberFormatException nfe) {
                        throw new NumberFormatException(nfe.getMessage());

                    }
            }
        }
        programService.loadVars(vals);
    }

    private void rebuildInputFields() {
        inputsContainer.getChildren().clear();

        var vars = programService.getInputsVars();
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


    private void showInitialVariables() {
        programService.resetMaps();
        /*if (programService == null || !programService.hasProgram()) {
            varsTableController.setItems(FXCollections.observableArrayList());

            return;
        }*/
        varsTableController.setItems(FXCollections.observableArrayList(programService.getAllVarsSorted()));

    }

    private void refreshButtons() {
        boolean atEnd = debugging && parent != null && programService.isFinishedDebugging();

        btnRun.setDisable(debugging);
        btnDebug.setDisable(debugging);
        btnResume.setDisable(!debugging || atEnd);
        btnStepOver.setDisable(!debugging || atEnd);
        btnStop.setDisable(!debugging ||atEnd);
        if (!parent.getArchitectureSelected()){
            btnRun.setDisable(true);
            btnDebug.setDisable(true);
        }
        else {
            btnRun.setDisable(false);
            btnDebug.setDisable(false);
        }
        parent.getBtnExpand().setDisable(debugging);
        parent.getBtnCollapse().setDisable(debugging);
    }

    public void setPendingRerunInputs(List<Long> inputs) {
        this.reRunInputs = inputs;
        this.onReRun = true;
    }

    private void prefillInputsIfNeeded() {
        if (!onReRun) return;

        if (inputsContainer.getChildren().isEmpty()) {
            rebuildInputFields();
        }

        fillInputFields(reRunInputs);
        onReRun = false;
    }

    private void fillInputFields(List<Long> inputs) {
        int i = 0;
        for (var node : inputsContainer.getChildren()) {
            if (node instanceof TextField tf) {
                String val = (i < inputs.size()) ? String.valueOf(inputs.get(i)) : "";
                tf.setText(val);
                i++;
            }
        }
    }

    public void NewRunOrDebugChoiceFromReRunButton() {
        debugging = false;
        debuggerLevel = 0;

        runAwaitingStart = false;
        hBoxStart.setVisible(false);
        inputsContainer.setDisable(true);

        refreshButtons();
    }
}
