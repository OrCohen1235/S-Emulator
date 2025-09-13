package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logic.dto.InstructionDTO;
import program.ProgramLoadException;
import ui.services.HistoryService;
import ui.services.ProgramService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RootController {

    @FXML private Spinner<Integer> spnDegree;

    @FXML private Label lblFilePath;
    @FXML private Label lblMaxDegree;

    @FXML private Button btnExpand;
    @FXML private Button btnCollapse;
    @FXML private Button onHighLight;

    @FXML private InstructionsController instructionsController;
    @FXML private ExecutionController executionController;
    @FXML private HistoryController historyController; // אופציונלי ב-FXML

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

        // אינטגרציית היסטוריה - בטוח לבצע גם אם historyController לא מוזרק
        programService.setHistory(historyService);
        if (historyController != null) {
            historyController.init(historyService);
        }
    }

    public ProgramService getProgramService() { return programService; }

    public int getDegree() {
        return (spnDegree != null && spnDegree.getValue() != null) ? spnDegree.getValue() : 0;
    }

    // שמירת תאימות: שם חדש
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

        // לסגור בהקלקה כפולה ולבצע היילייט
        list.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2 && list.getSelectionModel().getSelectedItem() != null) {
                highlightVariable(list.getSelectionModel().getSelectedItem());
                stage.close();
            }
        });

        stage.showAndWait();
    }

    // שמירת תאימות: שם ישן (FXML ישנים) -> מפנה לחדש
    @FXML private void onHighLight(ActionEvent e) { onHighLightVarOrLabel(e); }

    private void highlightVariable(String selectedItem) {
        List<Integer> res = new ArrayList<>();
        int index = 0;
        for (InstructionDTO dto : programService.getInstructionsDTO()){
            if (dto.getLabel().equals(selectedItem)){
                res.add(index);
            } else if (dto.getCommand().contains(selectedItem)){
                res.add(index);
            }
            index++;
        }

        // העדפה: סימון מרובה דרך ה-Controller (אם תומך)
        try {
            // אם קיימת מתודה setHighLightedRowIndexes(List<Integer>) ב-InstructionsController
            instructionsController.setHighLightedRowIndexes(res);
        } catch (Throwable t) {
            // נפילה אחורה: לעבור אחת-אחת
            for (Integer i : res) instructionsController.highlightRow(i);
        }
    }

    @FXML private void onLoadFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Program XML");
        File file = fc.showOpenDialog((lblFilePath != null) ? lblFilePath.getScene().getWindow() : null);
        if (file == null) return; // תקון NPE

        this.selectedFile = file;
        String fullPath = selectedFile.getAbsolutePath();
        tooltip = new Tooltip(fullPath);

        if (spnDegree != null) spnDegree.setDisable(true);

        try {
            programService.loadXml(file);

            // UI updates
            if (lblFilePath != null) lblFilePath.setText(file.getAbsolutePath());
            setMaxDegree(programService.getMaxDegree());
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

    public int getInstructionCount() {
        return instructionsController.getInstructionCount();
    }

    public Button getBtnExpand() { return btnExpand; }

    public Button getBtnCollapse() { return btnCollapse; }

    @FXML public void mouseEnter(MouseEvent mouseEvent) {
        if (lblFilePath != null && tooltip != null) {
            Tooltip.install(lblFilePath, tooltip);
        }
    }

    @FXML public void mouseExit(MouseEvent mouseEvent) { /* no-op */ }

    public ExecutionController getExecutionController() { return executionController; }
}
