package ui;
import javafx.css.PseudoClass;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import logic.dto.InstructionDTO;
import logic.variable.Variable;
import ui.services.ProgramService;
import javafx.event.Event;

import java.awt.*;
import java.util.*;
import java.io.File;
import java.util.List;

public class SemulatorController {


    private final ProgramService programService = new ProgramService();
    private int maxDegree = 0;
    private int debuggerLevel = 0;
    private static final PseudoClass PC_ROW_HIGHLIGHT = PseudoClass.getPseudoClass("row-highlighted");
    private static final PseudoClass PC_ROW_HIGHLIGHT_FINISH = PseudoClass.getPseudoClass("row-highlightedfinished");
    private final IntegerProperty highlightedRowIndex = new SimpleIntegerProperty(-1);




    // Top bar
    @FXML private Label lblFilePath, lblSummary;
    @FXML private Spinner<Integer> spnDegree;
    @FXML private Label lblMaxDegree;

    @FXML private VBox  inputsContainer;

    // ===== Instructions (TreeTableView) =====
    @FXML private TreeTableView<InstructionDTO> trvInstructions;
    @FXML private TreeTableColumn<InstructionDTO, String> colIndex, colType, colLabel, colCycles, colInstruction;

    @FXML private TreeTableColumn<InstructionDTO, Void>   colExpandFrom;

    @FXML private ListView<String> lstHistoryChain;

    @FXML private Button btnStepOver;
    @FXML private Button btnStepBack;
    @FXML private Button btnDebug;
    @FXML private Button btnRun;
    @FXML private Button btnStop;

    // Right panel
    @FXML private TableView<VarRow> tblVariables;
    @FXML private TableColumn<VarRow, String> colVarName, colVarType, colVarValue;
    @FXML private TextField txtInput1, txtInput2, txtCyclesCap, txtOutput;
    @FXML private CheckBox chkFastMode;
    @FXML private Label lblCycles;
    @FXML private ProgressBar prgExecution;

    // History
    @FXML private TableView<HistoryRow> tblHistory;
    @FXML private TableColumn<HistoryRow, String> colRunTime, colRunInputs, colRunY, colRunCycles, colRunNotes;

    // Status bar
    @FXML private Label lblEngineState, lblSelection, lblFps;




    /* ===== Demo models (כמו שהיה) ===== */
    public static class VarRow {
        public final String name, type, value;
        public VarRow(String name, String type, String value) { this.name=name; this.type=type; this.value=value; }
        public String getName(){ return name; } public String getType(){ return type; } public String getValue(){ return value; }
    }
    public static class HistoryRow {
        public final String time, inputs, y, cycles, notes;
        public HistoryRow(String time, String inputs, String y, String cycles, String notes) {
            this.time=time; this.inputs=inputs; this.y=y; this.cycles=cycles; this.notes=notes;
        }
        public String getTime(){ return time; } public String getInputs(){ return inputs; }
        public String getY(){ return y; } public String getCycles(){ return cycles; } public String getNotes(){ return notes; }
    }

    @FXML
    private void initialize() {

        btnStepOver.setDisable(true);
        btnStepBack.setDisable(true);
        btnStop.setDisable(true);

        /* ===== Instructions tree: value factories ===== */
        // TreeTableView משתמש ב-TreeItemPropertyValueFactory עם שמות ה-getters במודל InstructionDTO
        colIndex.setCellValueFactory(new TreeItemPropertyValueFactory<>("displayIndex"));
        colType.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        colLabel.setCellValueFactory(new TreeItemPropertyValueFactory<>("label"));
        colCycles.setCellValueFactory(new TreeItemPropertyValueFactory<>("cycles"));
        colInstruction.setCellValueFactory(new TreeItemPropertyValueFactory<>("command"));

        colExpandFrom.setCellFactory(col -> new TreeTableCell<InstructionDTO, Void>() {
            private final Button btn = new Button("Show Expand");

            {
                btn.setOnAction(e -> {
                    TreeTableRow<InstructionDTO> row = getTreeTableRow();
                    if (row == null) return;
                    InstructionDTO dto = row.getItem();
                    TreeItem<InstructionDTO> item = row.getTreeItem();
                    if (dto != null && item != null) {
                        onShowExpand(dto, item, row);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                TreeTableRow<InstructionDTO> row = getTreeTableRow();
                InstructionDTO dto = (row == null) ? null : row.getItem();

                boolean showButton = dto != null && !"0".equals(dto.getFather());
                setGraphic(showButton ? btn : null);
                setText(null);
            }
        });





        // TreeTableView בסיס
        if (trvInstructions != null) {
            trvInstructions.setShowRoot(false);
            // בחירה → עדכון סטטוס/היסטוריה
            trvInstructions.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null && newSel.getValue() != null) {
                    InstructionDTO sel = newSel.getValue();
                    lblSelection.setText("#" + sel.getDisplayIndex() + " " + sel.getCommand());
                    lstHistoryChain.getItems().add(sel.getCommand());
                }

            });
            trvInstructions.setSelectionModel(null);
        }

        spnDegree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxDegree, 0));

        trvInstructions.setRowFactory(tv -> {
            TreeTableRow<InstructionDTO> row = new TreeTableRow<>() {
                @Override
                protected void updateItem(InstructionDTO item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        pseudoClassStateChanged(PC_ROW_HIGHLIGHT, false);
                        pseudoClassStateChanged(PC_ROW_HIGHLIGHT_FINISH, false);
                        setStyle("");
                        return;
                    }

                    int sel = highlightedRowIndex.get();
                    boolean isThisRow = getIndex() == sel;

                    int lastVisibleIndex = trvInstructions.getExpandedItemCount() - 1; // ✅
                    boolean isLastVisible = sel >= 0 && sel == lastVisibleIndex;

                    pseudoClassStateChanged(PC_ROW_HIGHLIGHT, isThisRow);
                    pseudoClassStateChanged(PC_ROW_HIGHLIGHT_FINISH, isThisRow && isLastVisible);
                }
            };

            highlightedRowIndex.addListener((obs, oldV, newV) -> {
                int sel = (newV == null) ? -1 : newV.intValue();
                boolean isThisRow = !row.isEmpty() && row.getIndex() == sel;

                int lastVisibleIndex = trvInstructions.getExpandedItemCount() - 1; // ✅
                boolean isLastVisible = sel >= 0 && sel == lastVisibleIndex;

                row.pseudoClassStateChanged(PC_ROW_HIGHLIGHT, isThisRow);
                row.pseudoClassStateChanged(PC_ROW_HIGHLIGHT_FINISH, isThisRow && isLastVisible);

                if (!isThisRow) row.setStyle("");
            });

            row.indexProperty().addListener((o, ov, nv) -> {
                int sel = highlightedRowIndex.get();
                boolean isThisRow = !row.isEmpty() && row.getIndex() == sel;

                int lastVisibleIndex = trvInstructions.getExpandedItemCount() - 1; // ✅
                boolean isLastVisible = sel >= 0 && sel == lastVisibleIndex;

                row.pseudoClassStateChanged(PC_ROW_HIGHLIGHT, isThisRow);
                row.pseudoClassStateChanged(PC_ROW_HIGHLIGHT_FINISH, isThisRow && isLastVisible);
                if (!isThisRow) row.setStyle("");
            });

            return row;
        });



        // demo list

        // variables
        colVarName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colVarType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        colVarValue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getValue()));


        // history
        colRunTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTime()));
        colRunInputs.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInputs()));
        colRunY.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getY()));
        colRunCycles.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCycles()));
        colRunNotes.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNotes()));
        tblHistory.setItems(FXCollections.observableArrayList(
                new HistoryRow("12:01", "A=5,B=0", "7", "6", "demo run")
        ));

        // summary & status
        lblSummary.setText("Instructions: | B/S: 0/0 | Cycles: 0");
        lblCycles.setText("0");
        prgExecution.setProgress(0);
        lblEngineState.setText("Idle");


    }

    /* ===== File load & tree refresh ===== */
    public void onLoadFile(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(lblFilePath.getScene().getWindow());
        if (selectedFile == null) {
            lblFilePath.setText("File Not Found");
            return;
        }

        lblFilePath.setText(selectedFile.getAbsolutePath());
        programService.loadXml(selectedFile);          // טען מנוע/תוכנית
        setInstructions(0);                            // רענון עץ
        setMaxDegree(programService.getMaxDegree());
        tblVariables.setItems(FXCollections.observableArrayList(programService.getVariables()));
        setVarsLabels();

    }

    // בקונטרולר

    private void setVarsLabels() {
        inputsContainer.getChildren().clear();
        var vars = programService.getVariables();
        boolean hasVars = !vars.isEmpty();

        inputsContainer.setVisible(hasVars);
        inputsContainer.setManaged(hasVars);

        if (!hasVars) return;

        for (int i = 0; i < vars.size(); i++) {
            TextField tf = new TextField();
            tf.setPromptText(vars.get(i).getName().toUpperCase());
            inputsContainer.getChildren().add(tf);
        }
    }


    private void setInstructions(int degree) {
        programService.loadExpasionByDegree(degree);
        List<InstructionDTO> dtos = programService.getProgram().getInstructionDTOs();
        int sumOfBInstructions=0;
        int sumOfSInstructions=0;
        int sumOfCycles=0;
        for (InstructionDTO dto : dtos) {
            sumOfCycles += Integer.parseInt(dto.getCycles());
            if (dto.getType().toUpperCase() == "B"){
                sumOfBInstructions++;
            }
            else {
                sumOfSInstructions++;
            }
        }

        // root מוסתר עם ילדים "שטוחים" (אפשר להחליף להיררכיה אמיתית אם תספק Father/Children)
        TreeItem<InstructionDTO> root = new TreeItem<>(null);
        for (InstructionDTO dto : dtos) {
            root.getChildren().add(new TreeItem<>(dto));
        }
        trvInstructions.setRoot(root);

        // עדכון summary קטן
        lblSummary.setText("Program loaded"+programService.getProgramName()+ "| Instructions: " + dtos.size() +"| B/S: " +sumOfBInstructions+ "/" +sumOfSInstructions +"| Cycles: "+sumOfCycles );
    }

    private void setMaxDegree(int max) {
        this.maxDegree = Math.max(0, max);
        spnDegree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxDegree, 0));
        lblMaxDegree.setText("/ " + maxDegree);
    }

    /* ===== Expand/Collapse by degree ===== */
    public void onCollapse(ActionEvent e) {
        spnDegree.getValueFactory().setValue(spnDegree.getValue() - 1);
        debuggerLevel=0;
        setInstructions(spnDegree.getValue());
    }

    public void onExpand(ActionEvent e) {
        spnDegree.getValueFactory().setValue(spnDegree.getValue() + 1);
        debuggerLevel=0;
        setInstructions(spnDegree.getValue());
    }

    /* ===== Execution demo ===== */
    public void onRun(ActionEvent e) {
        applyVarsValues();
        Long y = programService.executeProgram(spnDegree.getValue());
        txtOutput.setText(String.valueOf(y));
        lblCycles.setText(String.valueOf(programService.getCycles()));
        tblVariables.setItems(FXCollections.observableArrayList(programService.getVariables()));
        programService.resetMaps();
        setVarsLabels();
        prgExecution.setProgress(1.0);
    }
    public void onDebug(ActionEvent e){;
    btnStepOver.setDisable(false);
    btnStepBack.setDisable(false);
    btnStop.setDisable(false);
    btnRun.setDisable(true);
    btnDebug.setDisable(true);


    }
    public void onStepOver(ActionEvent e){
        lblEngineState.setText("Step over");
        if (debuggerLevel==0)
        {
            applyVarsValues();
        }
        debuggerLevel++;
        Long y = programService.executeProgramDebugger(spnDegree.getValue(),debuggerLevel);
        txtOutput.setText(String.valueOf(y));
        lblCycles.setText(String.valueOf(programService.getCycles()));
        tblVariables.setItems(FXCollections.observableArrayList(programService.getVariables()));
        highlightRow(programService.getCurrentInstructionIndex());
        setVarsLabels();
        prgExecution.setProgress(1.0);
    }

    public void onStepBack(ActionEvent e){
        lblEngineState.setText("Step back");
        if (debuggerLevel==0)
        {
            applyVarsValues();
        }
        debuggerLevel--;
        Long y = programService.executeProgramDebugger(spnDegree.getValue(),debuggerLevel);
        txtOutput.setText(String.valueOf(y));
        lblCycles.setText(String.valueOf(programService.getCycles()));
        tblVariables.setItems(FXCollections.observableArrayList(programService.getVariables()));

        highlightRow(programService.getCurrentInstructionIndex());

        setVarsLabels();
        prgExecution.setProgress(1.0);
}
    public void onStop(ActionEvent e)    {
        btnStepOver.setDisable(true);
        btnStepBack.setDisable(true);
        btnRun.setDisable(false);
        btnDebug.setDisable(false);
        setInstructions(spnDegree.getValue());
        debuggerLevel=0;


        lblEngineState.setText("Stopped");
        prgExecution.setProgress(0); }
    public void onShowStats(ActionEvent e){ lblEngineState.setText("Showing stats"); }
    public void onRerun(ActionEvent e)   { prgExecution.setProgress(0.3); }
    public void onExport(ActionEvent e)  { lblEngineState.setText("Exported CSV (demo)"); }
    public void onReRun(ActionEvent actionEvent) {
        btnRun.setDisable(true);
        onRun(actionEvent);
        btnRun.setDisable(false);
    }


    public void applyVarsValues() {
        List<Long> varsValues= new ArrayList<>();
        for (Node node : inputsContainer.getChildren()) {
            if (node instanceof TextField tf) {
                String value = tf.getText();
                if (value != null && !value.isEmpty()) {
                    varsValues.add(Long.parseLong(value));
                }
                else {
                    varsValues.add(0L);
                }

            }
        }
        programService.loadVars(varsValues);
    }






    private void onShowExpand(InstructionDTO parentDto, TreeItem<InstructionDTO> parentItem, TreeTableRow<InstructionDTO> parentRow) {
        boolean isOpen = !parentItem.getChildren().isEmpty();

        if (isOpen) {
            parentItem.getChildren().clear();
            parentItem.setExpanded(false);
            parentRow.getStyleClass().remove("expanded-parent");
            lblEngineState.setText("Collapse: #" + parentDto.getDisplayIndex());
            return;
        }

        List<InstructionDTO> sub = programService.getExpansionFor(parentDto); // החזר רשימה רלוונטית
        if (sub == null || sub.isEmpty()) {
            lblEngineState.setText("No expansion for #" + parentDto.getDisplayIndex());
            return;
        }

        for (InstructionDTO child : sub) {
            parentItem.getChildren().add(new TreeItem<>(child));
        }
        parentItem.setExpanded(true);

        if (!parentRow.getStyleClass().contains("expanded-parent")) {
            parentRow.getStyleClass().add("expanded-parent");
        }

        lblEngineState.setText("Expand: #" + parentDto.getDisplayIndex() + " (" + sub.size() + " rows)");
    }

    // קרא כשאתה רוצה לצבוע שורה מס' i:
    public void highlightRow(int i) {
        highlightedRowIndex.set(i);
        trvInstructions.scrollTo(i);            // רשות
        trvInstructions.refresh();              // להחיל מיידית
    }

    // להסרה:
    public void clearRowHighlight() {
        highlightedRowIndex.set(-1);
        trvInstructions.refresh();
    }


}
