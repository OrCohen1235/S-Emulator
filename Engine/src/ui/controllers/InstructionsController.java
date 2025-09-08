package ui.controllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import logic.dto.InstructionDTO;
import ui.cells.RowHighlighter;
import ui.services.ProgramService;
import ui.viewmodel.InstructionsViewModel;

import java.util.List;

public class InstructionsController {

    @FXML private TreeTableView<InstructionDTO> trvInstructions;
    @FXML private TreeTableColumn<InstructionDTO, String> colIndex, colType, colLabel, colCycles, colInstruction;
    @FXML private TreeTableColumn<InstructionDTO, Void>   colExpandFrom;  // ← עמודת הכפתור

    private final InstructionsViewModel vm = new InstructionsViewModel();
    private ProgramService programService;
    private RootController parent;

    private static final PseudoClass PC_ROW_HL  = PseudoClass.getPseudoClass("row-highlighted");
    private static final PseudoClass PC_ROW_END = PseudoClass.getPseudoClass("row-highlightedfinished");
    private final IntegerProperty highlightedRowIndex = new SimpleIntegerProperty(-1);
    private RowHighlighter rowHighlighter = new RowHighlighter();

    @FXML
    private void initialize() {
        // עמודות-דאטה
        trvInstructions.setShowRoot(false);
        colIndex.setCellValueFactory(new TreeItemPropertyValueFactory<>("displayIndex"));
        colType.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        colLabel.setCellValueFactory(new TreeItemPropertyValueFactory<>("label"));
        colCycles.setCellValueFactory(new TreeItemPropertyValueFactory<>("cycles"));
        colInstruction.setCellValueFactory(new TreeItemPropertyValueFactory<>("command"));


        // אנחנו מסמנים ידנית, לא משתמשים ב־SelectionModel
        trvInstructions.setSelectionModel(null);

        // RowFactory לצביעה לפי highlightedRowIndex
        trvInstructions.setRowFactory(tv -> {
            TreeTableRow<InstructionDTO> row = new TreeTableRow<>() {
                @Override
                protected void updateItem(InstructionDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    applyPseudoClasses(this);
                }
            };
            highlightedRowIndex.addListener((obs, ov, nv) -> applyPseudoClasses(row));
            row.indexProperty().addListener((o, ov, nv) -> applyPseudoClasses(row));
            return row;
        });

        // עמודת "Show Expand" (כפתור בכל שורה מתאימה)
        if (colExpandFrom != null) {
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
                    if (empty) { setGraphic(null); setText(null); return; }
                    TreeTableRow<InstructionDTO> row = getTreeTableRow();
                    InstructionDTO dto = (row == null) ? null : row.getItem();
                    boolean show = dto != null && !"0".equals(dto.getFather());
                    setGraphic(show ? btn : null);
                    setText(null);
                }
            });
        }
    }

    /** יקרא מה-RootController אחרי יצירת הקונטרולר. */
    public void setParent(RootController parent) {
        this.parent = parent;
        this.programService = parent.getProgramService();
    }

    /** מרענן את הטבלה לפי degree נוכחי. */
    public void refresh(int degree) {
        if (programService == null && parent != null) {
            programService = parent.getProgramService();
        }
        if (programService == null) return;

        vm.reloadInstructions(programService, degree);
        trvInstructions.setRoot(vm.getRoot());
        trvInstructions.refresh();

        if (parent != null) {
            parent.updateSummary(vm.buildSummary(programService));
        }

        // שמירת ההדגשה בתחום חוקי

        if (highlightedRowIndex.get() >= 0 && trvInstructions.getRoot() != null) {
            int last = trvInstructions.getExpandedItemCount() - 1;
            highlightedRowIndex.set(last >= 0 ? Math.min(highlightedRowIndex.get(), last) : -1);
        }
    }

    // ===== Expand/Collapse =====
    private void onShowExpand(InstructionDTO parentDto,
                              TreeItem<InstructionDTO> parentItem,
                              TreeTableRow<InstructionDTO> parentRow) {

        boolean isOpen = !parentItem.getChildren().isEmpty();
        if (isOpen) {
            parentItem.getChildren().clear();
            parentItem.setExpanded(false);
            parentRow.getStyleClass().remove("expanded-parent");
            return;
        }

        if (programService == null) return;
        List<InstructionDTO> sub = programService.getExpansionFor(parentDto);
        if (sub == null || sub.isEmpty()) return;

        for (InstructionDTO child : sub) {
            parentItem.getChildren().add(new TreeItem<>(child));
        }
        parentItem.setExpanded(true);
        if (!parentRow.getStyleClass().contains("expanded-parent")) {
            parentRow.getStyleClass().add("expanded-parent");
        }
    }

    // ===== סימון שורות =====
    private void applyPseudoClasses(TreeTableRow<InstructionDTO> row) {
        boolean empty = row.isEmpty() || row.getItem() == null;
        if (empty) {
            row.pseudoClassStateChanged(PC_ROW_HL, false);
            row.pseudoClassStateChanged(PC_ROW_END, false);
            row.setStyle("");
            return;
        }
        int sel = highlightedRowIndex.get();
        boolean isThisRow = row.getIndex() == sel;
        int lastVisible = (trvInstructions.getRoot() == null) ? -1 : trvInstructions.getExpandedItemCount() - 1;
        boolean isLastVisible = sel >= 0 && sel == lastVisible;

        row.pseudoClassStateChanged(PC_ROW_HL,  isThisRow);
        row.pseudoClassStateChanged(PC_ROW_END, isThisRow && isLastVisible);
    }

    public void highlightRow(int i) {
        if (trvInstructions.getRoot() == null) return;
        int last = trvInstructions.getExpandedItemCount()-1;
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

    public int getInstructionCount() {
        return trvInstructions.getExpandedItemCount();
    }
}
