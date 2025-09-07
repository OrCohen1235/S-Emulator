package ui.cells;

import javafx.css.PseudoClass;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import logic.dto.InstructionDTO;
import ui.viewmodel.DebugSessionViewModel;

public class RowHighlighter {
    private static final PseudoClass PC_ROW_HIGHLIGHT =
            PseudoClass.getPseudoClass("row-highlighted");
    private static final PseudoClass PC_ROW_HIGHLIGHT_FINISH =
            PseudoClass.getPseudoClass("row-highlightedfinished");

    public static void install(TreeTableView<InstructionDTO> table, DebugSessionViewModel vm) {
        table.setRowFactory(tv -> {
            TreeTableRow<InstructionDTO> row = new TreeTableRow<>() {
                @Override
                protected void updateItem(InstructionDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    applyState(this, table, vm);
                }
            };

            vm.highlightedRowIndexProperty().addListener((o, ov, nv) -> applyState(row, table, vm));
            row.indexProperty().addListener((o, ov, nv) -> applyState(row, table, vm));
            return row;
        });

        table.getRoot().expandedProperty().addListener((o, ov, nv) ->
                vm.lastVisibleIndexProperty().set(table.getExpandedItemCount() - 1));
    }

    private static void applyState(TreeTableRow<InstructionDTO> row, TreeTableView<InstructionDTO> table, DebugSessionViewModel vm) {
        if (row.isEmpty()) {
            row.pseudoClassStateChanged(PC_ROW_HIGHLIGHT, false);
            row.pseudoClassStateChanged(PC_ROW_HIGHLIGHT_FINISH, false);
            row.setStyle("");
            return;
        }
        int sel = vm.highlightedRowIndexProperty().get();
        boolean isThisRow = row.getIndex() == sel;
        int lastVisibleIndex = table.getExpandedItemCount() - 1;
        boolean isLastVisible = sel >= 0 && sel == lastVisibleIndex;

        row.pseudoClassStateChanged(PC_ROW_HIGHLIGHT, isThisRow);
        row.pseudoClassStateChanged(PC_ROW_HIGHLIGHT_FINISH, isThisRow && isLastVisible);
        if (!isThisRow) row.setStyle("");
    }
}
