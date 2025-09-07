package ui.cells;

import javafx.scene.control.*;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeItem;
import logic.dto.InstructionDTO;
import ui.services.ProgramService;

import java.util.function.Supplier;

public class ExpandButtonCell extends TreeTableCell<InstructionDTO, Void> {
    private final Button btn = new Button("Show Expand");
    private final Supplier<ProgramService> serviceSupplier;
    private final TreeTableView<InstructionDTO> table;

    public ExpandButtonCell(Supplier<ProgramService> serviceSupplier, TreeTableView<InstructionDTO> table) {
        this.serviceSupplier = serviceSupplier;
        this.table = table;
        btn.setOnAction(e -> {
            TreeTableRow<InstructionDTO> row = getTreeTableRow();
            if (row == null) return;
            InstructionDTO dto = row.getItem();
            TreeItem<InstructionDTO> item = row.getTreeItem();
            if (dto != null && item != null) toggleExpand(dto, item, row);
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) { setGraphic(null); return; }
        InstructionDTO dto = (getTreeTableRow() == null) ? null : getTreeTableRow().getItem();
        boolean show = dto != null && !"0".equals(dto.getFather());
        setGraphic(show ? btn : null);
    }

    private void toggleExpand(InstructionDTO parentDto, TreeItem<InstructionDTO> parentItem, TreeTableRow<InstructionDTO> parentRow) {
        if (!parentItem.getChildren().isEmpty()) {
            parentItem.getChildren().clear();
            parentItem.setExpanded(false);
            parentRow.getStyleClass().remove("expanded-parent");
            return;
        }
        var sub = serviceSupplier.get().getExpansionFor(parentDto);
        if (sub == null || sub.isEmpty()) return;
        for (InstructionDTO child : sub) parentItem.getChildren().add(new TreeItem<>(child));
        parentItem.setExpanded(true);
        if (!parentRow.getStyleClass().contains("expanded-parent")) parentRow.getStyleClass().add("expanded-parent");
    }
}
