package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.HistoryRow;
import services.HistoryService;
import viewmodel.Architecture;

public class HistoryController {

    @FXML private TableView<HistoryRow> historyTable;

    // טיפוסים מעודכנים לפי ה-Properties החדשים
    @FXML private TableColumn<HistoryRow, Number> colRunNumber;
    @FXML private TableColumn<HistoryRow, Boolean> colMainProgram;
    @FXML private TableColumn<HistoryRow, String> colNameOrUserString;
    @FXML private TableColumn<HistoryRow, Architecture> colArchitecture;
    @FXML private TableColumn<HistoryRow, Number> colDegree;
    @FXML private TableColumn<HistoryRow, Number> colY;
    @FXML private TableColumn<HistoryRow, Number> colCycles;

    @FXML private Button btnShowStatus; // כבר לא בשימוש בפועל
    @FXML private Button btnReRun;

    private HistoryService historyService;
    private RootController parent;
    private ExecutionController executionController;

    @FXML
    private void initialize() {
        // ValueFactories תואמים ל-Property המתאים
        colRunNumber.setCellValueFactory(cd -> cd.getValue().runNumberProperty());
        colMainProgram.setCellValueFactory(cd -> cd.getValue().mainProgramProperty());
        colNameOrUserString.setCellValueFactory(cd -> cd.getValue().nameOrUserStringProperty());
        colArchitecture.setCellValueFactory(cd -> cd.getValue().architectureProperty());
        colDegree.setCellValueFactory(cd -> cd.getValue().degreeProperty());
        colY.setCellValueFactory(cd -> cd.getValue().yProperty());
        colCycles.setCellValueFactory(cd -> cd.getValue().cyclesProperty());

        // הצגה קריאה ל-main/aux
        colMainProgram.setCellFactory(col -> new TableCell<HistoryRow, Boolean>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "תוכנית ראשית" : "פונקציית עזר"));
            }
        });

        // הצגת ארכיטקטורה: אפשר name() או toString()
        colArchitecture.setCellFactory(col -> new TableCell<HistoryRow, Architecture>() {
            @Override protected void updateItem(Architecture item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name()); // או item.toString()
            }
        });

        chooseHistoryRow();
        unChooseHistoryRow();

        // אין יותר תצוגת משתנים — נשאיר את הכפתור כבוי תמיד או נסיר ב-FXML
        if (btnShowStatus != null) btnShowStatus.setDisable(true);
    }

    public void init(HistoryService historyService, RootController parent, ExecutionController executionController) {
        this.historyService = historyService;
        if (historyTable != null) {
            historyTable.setItems(historyService.getHistory());
        } else {
            javafx.application.Platform.runLater(() ->
                    historyTable.setItems(historyService.getHistory())
            );
        }
        this.parent = parent;
        this.executionController = executionController;
    }

    // הכפתור הזה כבר לא רלוונטי (אין VarRow במודל החדש)
    @FXML
    public void onShowStatus() {
        // אופציונלי: הצגת הודעה ידידותית או פשוט לא לעשות כלום
        new Alert(Alert.AlertType.INFORMATION,
                "הצגת משתנים כבר לא זמינה עבור היסטוריה זו.").showAndWait();
    }

    @FXML
    public void onRerun() {
        HistoryRow row = historyTable.getSelectionModel().getSelectedItem();
        if (row == null) return;

        // עדיין מעדכנים את הדרגה במסך הראשי, כמו קודם
        if (parent != null) {
            parent.setSpnDegree(row.getDegree());
        }

        // הפעלת ריצה/דיבוג מחדש
        if (executionController != null) {
            executionController.NewRunOrDebugChoiceFromReRunButton();

            // בעבר העברנו startingInput מההיסטוריה — זה כבר לא קיים במודל החדש.
            // אם בעתיד תרצה להזין קלט מחדש, שלוף אותו ממקור אחר (למשל HistoryService).
            // executionController.setPendingRerunInputs(...);
        }
    }

    private void chooseHistoryRow() {
        historyTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = (newSel != null);
            // btnShowStatus אינו נחוץ — נשאיר כבוי
            btnReRun.setDisable(!hasSelection);
        });
    }

    private void unChooseHistoryRow() {
        historyTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, ev -> {
                javafx.scene.Node t = (javafx.scene.Node) ev.getTarget();
                if (isAncestorOf(t, historyTable)) return;
                if (isAncestorOf(t, btnShowStatus) || isAncestorOf(t, btnReRun)) return;

                historyTable.getSelectionModel().clearSelection();
            });
        });

        historyTable.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                historyTable.getSelectionModel().clearSelection();
            }
        });
    }

    private boolean isAncestorOf(javafx.scene.Node target, javafx.scene.Node candidateAncestor) {
        javafx.scene.Node n = target;
        while (n != null) {
            if (n == candidateAncestor) return true;
            n = n.getParent();
        }
        return false;
    }

    public void clearHistory() {
        historyTable.getItems().clear();
    }
}
