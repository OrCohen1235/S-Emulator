package ui.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ui.model.HistoryRow;
import ui.model.VarRow;
import ui.services.HistoryService;

import java.io.IOException;

public class HistoryController {
    @FXML private TableView<HistoryRow> historyTable;
    @FXML private TableColumn<HistoryRow, String> colRunNumber, colY, colDegree, colCycles;

    @FXML private Button btnShow, btnReRun;

    private HistoryService historyService;

    @FXML private void initialize() {
        colRunNumber.setCellValueFactory(cellData -> cellData.getValue().runNumberProperty());
        colY.setCellValueFactory(cellData -> cellData.getValue().yProperty());
        colDegree.setCellValueFactory(cellData -> cellData.getValue().degreeProperty());
        colCycles.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());

        chooseHistoryRow();
        unChooseHistoryRow();
    }

    public void init(HistoryService historyService) {
        this.historyService = historyService;
        if (historyTable != null) {
            historyTable.setItems(historyService.getHistory());
        } else {
            javafx.application.Platform.runLater(() ->
                    historyTable.setItems(historyService.getHistory())
            );
        }
    }

    @FXML public void onShow(ActionEvent actionEvent) {
        HistoryRow row = historyTable.getSelectionModel().getSelectedItem();
        if (row == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/viewFXML/varsTable.fxml"));
            Parent content = loader.load();

            VarsTableController ctrl = loader.getController();
            ObservableList<VarRow> vars = row.getVars();
            ctrl.setItems(vars);

            Scene scene = new Scene(content, 420, 480);
            Stage stage = new Stage(StageStyle.DECORATED);

            stage.setTitle("Variables Status");
            stage.initOwner(historyTable.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(true);
            stage.setScene(scene);

            stage.showAndWait();

        } catch (IOException e){
            new Alert(Alert.AlertType.ERROR,
                    "Failed to open variables dialog:\n" + e.getMessage()).showAndWait();
        }
    }

    @FXML public void onRerun(ActionEvent actionEvent) {
    }

    private void chooseHistoryRow() {
        historyTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = (newSel != null);
            btnShow.setDisable(!hasSelection);
            btnReRun.setDisable(!hasSelection);
        });
    }

    private void unChooseHistoryRow() {
        historyTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, ev -> {
                javafx.scene.Node t = (javafx.scene.Node) ev.getTarget();
                if (isAncestorOf(t, historyTable)) return;
                if (isAncestorOf(t, btnShow) || isAncestorOf(t, btnReRun)) return;

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

}