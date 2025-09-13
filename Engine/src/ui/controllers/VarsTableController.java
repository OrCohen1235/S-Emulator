package ui.controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.model.VarRow;

public class VarsTableController {
    @FXML private TableView<VarRow> tblVars;
    @FXML private TableColumn<VarRow,String> colVarName,colVarType,colVarValue;

    private static final PseudoClass CHANGED = PseudoClass.getPseudoClass("changed");

    @FXML private void initialize() {
        colVarName.setCellValueFactory(c -> c.getValue().nameProperty());
        colVarType.setCellValueFactory(c -> c.getValue().typeProperty());
        colVarValue.setCellValueFactory(c -> c.getValue().valueProperty());
        
    }

    public void setItems(ObservableList<VarRow> vars) {
        tblVars.setItems(vars);
    }
}
