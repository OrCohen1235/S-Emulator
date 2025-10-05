package controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import model.VarRow;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VarsTableController {
    @FXML private TableView<VarRow> tblVars;
    @FXML private TableColumn<VarRow,String> colVarName, colVarType, colVarValue;

    private boolean debugger = false;

    private static final PseudoClass CHANGED = PseudoClass.getPseudoClass("changed");

    private final ObservableList<VarRow> notNewList = FXCollections.observableArrayList();
    public ObservableList<VarRow> getNotNewList() { return notNewList; }

    @FXML
    private void initialize() {
        colVarName.setCellValueFactory(c -> c.getValue().nameProperty());
        colVarType.setCellValueFactory(c -> c.getValue().typeProperty());
        colVarValue.setCellValueFactory(c -> c.getValue().valueProperty());

        if (tblVars.getItems() == null) {
            tblVars.setItems(FXCollections.observableArrayList());
        }

        tblVars.setRowFactory(tv -> new TableRow<VarRow>() {
            private VarRow current;

            private final ChangeListener<Boolean> changedListener = (obs, ov, nv) -> {
                updateHighlight(this, current);
                if (debugger && nv != null && nv) {
                    ensureRowVisible(tblVars, current);
                }
            };

            private final ChangeListener<String> valueListener = (obs, ov, nv) -> {
                if (current == null) return;
                current.setChanged(true);
                updateHighlight(this, current);
                if (debugger) {
                    ensureRowVisible(tblVars, current);
                }
            };

            @Override
            protected void updateItem(VarRow item, boolean empty) {
                super.updateItem(item, empty);

                if (current != null) {
                    current.changedProperty().removeListener(changedListener);
                    current.valueProperty().removeListener(valueListener);
                }

                current = empty ? null : item;

                if (current != null) {
                    current.changedProperty().addListener(changedListener);
                    current.valueProperty().addListener(valueListener);
                }

                updateHighlight(this, current);
            }
        });
    }

    private void updateHighlight(TableRow<VarRow> row, VarRow item) {
        boolean highlight = item != null && debugger && item.isChanged();
        row.pseudoClassStateChanged(CHANGED, highlight);
    }

    public void setItems(ObservableList<VarRow> vars) {
        ObservableList<VarRow> items = tblVars.getItems();
        if (items == null) {
            items = FXCollections.observableArrayList();
            tblVars.setItems(items);
        }

        Map<String, VarRow> byName = items.stream()
                .collect(Collectors.toMap(VarRow::getName, Function.identity(), (a,b)->a, LinkedHashMap::new));

        notNewList.clear();

        VarRow firstChanged = null;

        for (VarRow incoming : vars) {
            VarRow existing = byName.get(incoming.getName());

            if (existing != null) {
                String oldVal = existing.getValue();
                String newVal = incoming.getValue();

                if (!Objects.equals(existing.getType(), incoming.getType())) {
                    existing.typeProperty().set(incoming.getType());
                }

                if (!Objects.equals(oldVal, newVal)) {
                    existing.valueProperty().set(newVal);
                    existing.setChanged(true);
                    if (firstChanged == null) firstChanged = existing;
                } else {
                    notNewList.add(existing);
                    existing.setChanged(false);
                }
            } else {
                VarRow copy = new VarRow(incoming.getName(), incoming.getType(), incoming.getValue());
                copy.setChanged(true);
                items.add(copy);
                if (firstChanged == null) firstChanged = copy;
            }
        }

        tblVars.refresh();

        if (debugger && firstChanged != null) {
            ensureRowVisible(tblVars, firstChanged);
        }
    }

    public void clearHighlights() {
        if (tblVars.getItems() != null) {
            for (VarRow r : tblVars.getItems()) r.setChanged(false);
        }
        tblVars.refresh();
    }

    public void setdebugger(boolean debugging) {
        this.debugger = debugging;
        clearHighlights();
        tblVars.refresh();
    }

    private void ensureRowVisible(TableView<VarRow> table, VarRow item) {
        if (item == null) return;

        Platform.runLater(() -> {
            table.scrollTo(item);
            int idx = table.getItems().indexOf(item);
            if (idx >= 0) {
                table.getFocusModel().focus(idx);
            }

            Platform.runLater(() -> table.scrollTo(item));
        });
    }

    public void clearVarsTable() {
        tblVars.getItems().clear();
        debugger=false;
    }
}
