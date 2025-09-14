package ui.controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import ui.model.VarRow;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VarsTableController {
    @FXML private TableView<VarRow> tblVars;
    @FXML private TableColumn<VarRow,String> colVarName, colVarType, colVarValue;

    // כאשר debugger=true — תתבצע צביעה וגלילה לשורות שהשתנו
    private boolean debugger = false;

    private static final PseudoClass CHANGED = PseudoClass.getPseudoClass("changed");

    // רשימת ה"לא חדשים"
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

        // rowFactory — צובע לפי item.changed && debugger
        // וגם מאזין לשינויי value כדי לזהות עריכה ידנית בעמודה ולגלול מייד.
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
                // ערך בעמודת value השתנה (למשל ע"י עריכת תא) — סמן כ-Changed
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

    /** צביעה לפי מצב changed + debugger (בלי refresh מיותר). */
    private void updateHighlight(TableRow<VarRow> row, VarRow item) {
        boolean highlight = item != null && debugger && item.isChanged();
        row.pseudoClassStateChanged(CHANGED, highlight);
    }

    /**
     * עדכון פריטים: אם ערך ה-Value השתנה — נסמן changed=true ונגלול אל השורה.
     * אם יש כמה עדכונים בבאטץ' — נגלול לראשונה שהשתנתה (ב-debug אתה ציינת שתמיד אחת משתנה).
     */
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

                // עדכון סוג (לא חובה, אבל שומר עקביות אם מגיע שינוי גם בטייפ)
                if (!Objects.equals(existing.getType(), incoming.getType())) {
                    existing.typeProperty().set(incoming.getType());
                }

                if (!Objects.equals(oldVal, newVal)) {
                    // ערך חדש בעמודת value => סמן changed וגלול
                    existing.valueProperty().set(newVal);
                    existing.setChanged(true);
                    if (firstChanged == null) firstChanged = existing;
                } else {
                    // לא חדש
                    notNewList.add(existing);
                    existing.setChanged(false);
                }
            } else {
                // שורה חדשה
                VarRow copy = new VarRow(incoming.getName(), incoming.getType(), incoming.getValue());
                copy.setChanged(true);
                items.add(copy);
                if (firstChanged == null) firstChanged = copy;
            }
        }

        // אין צורך ב-refresh עבור שינויי properties, אבל אם נוספו/הוסרו שורות — refresh עוזר.
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

    // הפעלה/כיבוי מצב דיבאג — מרענן כדי להחיל את השינוי מיד
    public void setdebugger(boolean debugging) {
        this.debugger = debugging;
        clearHighlights();
        tblVars.refresh();
    }

    /** גלילה יציבה לפי האובייקט עצמו, אחרי ה-layout; בחירה/פוקוס לשיפור עקביות. */
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
