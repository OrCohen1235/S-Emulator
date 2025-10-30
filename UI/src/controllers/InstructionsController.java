package controllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import logic.dto.InstructionDTO;
import services.ProgramService;
import viewmodel.Architecture;
import viewmodel.InstructionsViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstructionsController {


    @FXML private TreeTableView<InstructionDTO> trvInstructions;
    @FXML private TreeTableColumn<InstructionDTO, String> colIndex, colType, colLabel, colCycles, colInstruction, colArchitecture;

    @FXML private TreeTableView<InstructionDTO> trvInstructionHistory;
    @FXML private TreeTableColumn<InstructionDTO, String> colExIndex, colExType, colExLabel, colExCycles, colExInstruction , colExArchitecture;

    @FXML private Label lblSummary;

    private final InstructionsViewModel vm = new InstructionsViewModel();
    private ProgramService programService;
    private RootController parent;

    private static final PseudoClass PC_ROW_HL        = PseudoClass.getPseudoClass("row-highlighted");
    private static final PseudoClass PC_ROW_SELECTED  = PseudoClass.getPseudoClass("row-highlightedselected");
    private static final PseudoClass PC_ROW_END       = PseudoClass.getPseudoClass("row-highlightedfinished");

    private final IntegerProperty highlightedRowIndex = new SimpleIntegerProperty(-1);
    private final ObservableSet<Integer> highlightedRows = FXCollections.observableSet();

    private boolean hasExecuted = false;


    public void updateSummary(String s) { if (lblSummary != null) lblSummary.setText(s); }

    @FXML
    private void initialize() {
        trvInstructions.setShowRoot(false);
        colIndex.setCellValueFactory(new TreeItemPropertyValueFactory<>("displayIndex"));
        colType.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        colLabel.setCellValueFactory(new TreeItemPropertyValueFactory<>("label"));
        colCycles.setCellValueFactory(new TreeItemPropertyValueFactory<>("cycles"));
        colInstruction.setCellValueFactory(new TreeItemPropertyValueFactory<>("command"));
        colArchitecture.setCellValueFactory(new TreeItemPropertyValueFactory<>("architecture"));

        trvInstructionHistory.setShowRoot(false);
        colExIndex.setCellValueFactory(new TreeItemPropertyValueFactory<>("displayIndex"));
        colExType.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        colExLabel.setCellValueFactory(new TreeItemPropertyValueFactory<>("label"));
        colExCycles.setCellValueFactory(new TreeItemPropertyValueFactory<>("cycles"));
        colExInstruction.setCellValueFactory(new TreeItemPropertyValueFactory<>("command"));
        colExArchitecture.setCellValueFactory(new TreeItemPropertyValueFactory<>("architecture"));


        colCycles.setCellFactory(column -> new TreeTableCell<InstructionDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }

                TreeTableRow<InstructionDTO> row = getTableRow();
                InstructionDTO dto = (row != null) ? row.getItem() : null;

                if (dto == null) {
                    setText(item);
                    return;
                }

                String instructionName = dto.getInstructionName();
                boolean needsPlus = ("QUOTE".equals(instructionName) ||
                        "JUMP_EQUAL_FUNCTION".equals(instructionName));

                boolean hasFinished = hasExecuted || (programService != null && programService.isFinishedDebugging());

                if (!needsPlus) {
                    setText(item);
                } else {
                    setText(hasFinished ? item : item + "+");
                }
            }
        });

        if (lblSummary != null) lblSummary.setText("Instructions: 0 | B/S: 0/0");

        trvInstructions.setDisable(false);

        trvInstructions.setMouseTransparent(false);

        trvInstructions.setRowFactory(tv -> {
            TreeTableRow<InstructionDTO> row = new TreeTableRow<>() {
                @Override
                protected void updateItem(InstructionDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    applyPseudoClasses(this);
                }
            };
            row.setMouseTransparent(false);
            row.setPickOnBounds(true);

            row.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    InstructionDTO dto = row.getItem();
                    if (dto != null && programService != null) {
                        var list = programService.getExpansionFor(dto);
                        vm.onExpand(list,dto);
                        trvInstructionHistory.setRoot(vm.getExpandRoot());
                        trvInstructionHistory.refresh();
                    }
                }
            });

            highlightedRowIndex.addListener((obs, ov, nv) -> applyPseudoClasses(row));
            highlightedRows.addListener((SetChangeListener<Integer>) change -> applyPseudoClasses(row));
            row.indexProperty().addListener((o, ov, nv) -> applyPseudoClasses(row));
            return row;
        });

        trvInstructions.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            Node node = e.getPickResult().getIntersectedNode();
            while (node != null && !(node instanceof TreeTableRow)) node = node.getParent();
            if (node instanceof TreeTableRow<?> genericRow) {
                @SuppressWarnings("unchecked")
                TreeTableRow<InstructionDTO> row = (TreeTableRow<InstructionDTO>) genericRow;
                if (!row.isEmpty()) {
                    InstructionDTO dto = row.getItem();
                    if (dto != null && programService != null) {
                        var list = programService.getExpansionFor(dto);
                        vm.onExpand(list,dto);
                        trvInstructionHistory.setRoot(vm.getExpandRoot());
                        trvInstructionHistory.refresh();
                    }
                }
            }
        });
    }

    public void setParent(RootController parent) {
        this.parent = parent;
        this.programService = parent.getProgramService();
    }

    public void refresh(int degree) {
        if (programService == null && parent != null) {
            programService = parent.getProgramService();
        }
        if (programService == null) return;
        refreshHighlightRows();

        vm.reloadInstructions(programService, degree,programService.getInstructionsDTO());
        parent.setCurrentSumOfCycles(vm.getSumCycles());
        trvInstructions.setRoot(vm.getRoot());
        trvInstructionHistory.setRoot(vm.getExpandRoot());
        trvInstructions.refresh();
        trvInstructionHistory.refresh();

        updateSummary(vm.buildSummary(programService));

        if (trvInstructions.getRoot() != null) {
            int last = trvInstructions.getExpandedItemCount() - 1;
            int current = highlightedRowIndex.get();
            highlightedRowIndex.set((last >= 0 && current >= 0) ? Math.min(current, last) : (last >= 0 ? -1 : -1));

            if (last >= 0 && !highlightedRows.isEmpty()) {
                Set<Integer> clamped = new HashSet<>();
                for (Integer i : highlightedRows) {
                    if (i == null) continue;
                    int v = Math.max(0, Math.min(i, last));
                    clamped.add(v);
                }
                highlightedRows.clear();
                highlightedRows.addAll(clamped);
            } else if (last < 0) {
                highlightedRows.clear();
            }
        }

        trvInstructions.refresh();
    }

    private void applyPseudoClasses(TreeTableRow<InstructionDTO> row) {
        boolean empty = row.isEmpty() || row.getItem() == null;
        if (empty) {
            row.pseudoClassStateChanged(PC_ROW_HL,       false);
            row.pseudoClassStateChanged(PC_ROW_SELECTED, false);
            row.pseudoClassStateChanged(PC_ROW_END,      false);
            row.setStyle("");
            return;
        }

        int idx = row.getIndex();
        int sel = highlightedRowIndex.get();
        boolean isActive    = (idx == sel && sel >= 0);
        boolean isMarked    = highlightedRows.contains(idx);
        boolean lastVisible = (programService != null && programService.isFinishedDebugging());

        row.pseudoClassStateChanged(PC_ROW_HL,       isActive);
        row.pseudoClassStateChanged(PC_ROW_SELECTED, isMarked && !isActive);
        row.pseudoClassStateChanged(PC_ROW_END,      isActive && lastVisible);
    }

    public void setHighlitedRowIndexAccordingToArchitecture(String architecture) {
        // אם אין בחירה – ננקה הכול
        if (architecture == null || architecture.isBlank() || "Choose".equalsIgnoreCase(architecture)) {
            clearAllHighlightColors();
            if (parent != null) {
                parent.setArchitectureSelected(false);
                parent.setSumOfCurrentArchitecture(0); // אפס כשאין ארכיטקטורה
            }
            return;
        }

        Architecture arch = Architecture.parse(architecture);
        if (arch == null) {
            clearAllHighlightColors();
            if (parent != null) {
                parent.setArchitectureSelected(false);
                parent.setSumOfCurrentArchitecture(0);
            }
            return;
        }

        if (parent != null) {
            parent.setSumOfCurrentArchitecture(arch.getCreditsPerRun());
        }

        // רשימת אינדקסים שלא נתמכים (לצביעה אדום/ירוק)
        List<Integer> redIndexes = switch (arch) {
            case I   -> new ArrayList<>(vm.getArchitecturesI_Indexes());
            case II  -> new ArrayList<>(vm.getArchitecturesII_Indexes());
            case III -> new ArrayList<>(vm.getArchitecturesIII_Indexes());
            case IV  -> new ArrayList<>(vm.getArchitecturesIV_Indexes());
        };

        // צביעה לפי האינדקסים
        setHighLightedRowIndexesWithColors(redIndexes);

        // אם אין אדומות – הכול נתמך
        if (parent != null) parent.setArchitectureSelected(redIndexes.isEmpty());
    }




    public void highlightRow(int i) {
        if (trvInstructions.getRoot() == null) return;
        int last = trvInstructions.getExpandedItemCount() - 1;
        if (last < 0) return;
        int clamped = Math.max(0, Math.min(i, last));
        highlightedRowIndex.set(clamped);
        trvInstructions.scrollTo(clamped);
        trvInstructions.refresh();
    }

    public void clearHighlight() {
        highlightedRowIndex.set(-1);
        trvInstructions.refresh();
    }

    public void clearAllHighlights() {
        highlightedRows.clear();
        highlightedRowIndex.set(-1);
        trvInstructions.refresh();
    }

    public int getInstructionCount() {
        return trvInstructions.getExpandedItemCount();
    }

    public void setHighLightedRowIndexes(List<Integer> highLightedRowIndexes) {
        if (trvInstructions.getRoot() == null) {
            highlightedRows.clear();
            if (highLightedRowIndexes != null) highlightedRows.addAll(highLightedRowIndexes);
            return;
        }

        int last = trvInstructions.getExpandedItemCount() - 1;
        highlightedRows.clear();

        if (highLightedRowIndexes == null || last < 0) {
            trvInstructions.refresh();
            return;
        }

        for (Integer i : highLightedRowIndexes) {
            if (i == null) continue;
            int clamped = Math.max(0, Math.min(i, last));
            highlightedRows.add(clamped);
        }
        trvInstructions.refresh();
    }

    public void setHighLightedRowIndexesWithColors(List<Integer> redIndexes) {
        System.out.println("Red indexes: " + redIndexes);

        if (trvInstructions.getRoot() == null) {
            highlightedRows.clear();
            if (redIndexes != null) highlightedRows.addAll(redIndexes);
            trvInstructions.refresh();
            return;
        }
        if (redIndexes.isEmpty()){
            parent.setArchitectureSelected(true);
        }
        else {
            parent.setArchitectureSelected(false);
        }


        // ננקה קודם כל עיצובים קודמים
        trvInstructions.setRowFactory(tv -> new TreeTableRow<InstructionDTO>() {
            @Override
            protected void updateItem(InstructionDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle(""); // הסרה של צבעים קודמים
                    return;
                }
                int index = getIndex();
                if (redIndexes != null && redIndexes.contains(index)) {
                    setStyle("-fx-background-color: #ffcccc;"); // אדום בהיר
                } else {
                    setStyle("-fx-background-color: #ccffcc;"); // ירוק בהיר
                }
            }
        });

        trvInstructions.refresh();
    }


    public void refreshHighlightRows(){
        highlightedRows.clear();
        trvInstructions.refresh();
    }

    public void markAsExecuted(boolean executed) {
        this.hasExecuted = executed;
        trvInstructions.refresh();
    }

    public void clearAllHighlightColors() {
        // החזרת RowFactory לברירת מחדל (כמו ב-initialize)
        trvInstructions.setRowFactory(tv -> {
            TreeTableRow<InstructionDTO> row = new TreeTableRow<>() {
                @Override
                protected void updateItem(InstructionDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    applyPseudoClasses(this); // יחיל את הפסאודו-קלאסים / בלי צבעי אדום-ירוק
                }
            };
            row.setMouseTransparent(false);
            row.setPickOnBounds(true);

            row.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    InstructionDTO dto = row.getItem();
                    if (dto != null && programService != null) {
                        var list = programService.getExpansionFor(dto);
                        vm.onExpand(list, dto);
                        trvInstructionHistory.setRoot(vm.getExpandRoot());
                        trvInstructionHistory.refresh();
                    }
                }
            });

            highlightedRowIndex.addListener((obs, ov, nv) -> applyPseudoClasses(row));
            highlightedRows.addListener((SetChangeListener<Integer>) change -> applyPseudoClasses(row));
            row.indexProperty().addListener((o, ov, nv) -> applyPseudoClasses(row));

            return row;
        });

        highlightedRows.clear();
        highlightedRowIndex.set(-1);

        trvInstructions.refresh();
    }



}