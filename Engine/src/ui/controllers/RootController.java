package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import program.ProgramLoadException;
import ui.services.ProgramService;

import java.io.File;

public class RootController {

    @FXML private Spinner<Integer> spnDegree;
    @FXML private Label lblSummary;
    @FXML private Label lblFilePath;
    @FXML private Label lblMaxDegree;

    @FXML private Button btnExpand;
    @FXML private Button btnCollapse;


    @FXML private InstructionsController instructionsController;
    @FXML private ExecutionController executionController;

    private final ProgramService programService = new ProgramService();
    private File selectedFile;
    private Tooltip tooltip;




    @FXML
    private void initialize() {

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


        if (lblSummary   != null) lblSummary.setText("Instructions: 0 | B/S: 0/0 | Total Program Cycles: 0");
        if (lblFilePath  != null) lblFilePath.setText("-");
        if (lblMaxDegree != null) lblMaxDegree.setText("/ 0");
    }

    public ProgramService getProgramService() { return programService; }
    public int getDegree() {
        return (spnDegree != null && spnDegree.getValue() != null) ? spnDegree.getValue() : 0;
    }
    public void updateSummary(String s) { if (lblSummary != null) lblSummary.setText(s); }


    @FXML
    private void onLoadFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Program XML");
        File file = fc.showOpenDialog((lblFilePath != null) ? lblFilePath.getScene().getWindow() : null);
        this.selectedFile = file;
        String fullPath = selectedFile.getAbsolutePath();

        tooltip = new Tooltip(fullPath);
        if (file == null) return;

        spnDegree.setDisable(true);

        try {
            programService.loadXml(file);


            // UI updates
            if (lblFilePath != null) lblFilePath.setText(file.getAbsolutePath());
            setMaxDegree(programService.getMaxDegree()); // מעדכן ספינר + תווית "/ N"
            if (spnDegree != null) spnDegree.setDisable(false);

            // רענון הפאנלים
            if (instructionsController != null) instructionsController.refresh(getDegree());
            if (executionController != null) executionController.onProgramLoaded();


        } catch (ProgramLoadException ex) {
            showError("Load failed", ex.getMessage());
        } catch (Exception ex) {
            showError("Unexpected error", String.valueOf(ex));
        }
    }


    @FXML
    private void onExpand() {
        if (spnDegree == null || spnDegree.isDisabled()) return;
        int max = getSpinnerMax();
        int cur = getDegree();
        if (cur < max) spnDegree.getValueFactory().setValue(cur + 1);
        if (instructionsController != null) instructionsController.refresh(getDegree());
    }

    @FXML
    private void onCollapse() {
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

    public int getInstructionCount() {
        return instructionsController.getInstructionCount();
    }

    public Button getBtnExpand() {
        return btnExpand;
    }

    public Button getBtnCollapse() {
        return btnCollapse;
    }

    @FXML
    public void mouseEnter(MouseEvent mouseEvent) {
        Tooltip.install(lblFilePath, tooltip);
    }

    @FXML
    public void mouseExit(MouseEvent mouseEvent) {

    }
}
