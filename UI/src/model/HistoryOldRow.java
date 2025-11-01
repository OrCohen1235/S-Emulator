package model;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import program.Program;

import java.util.ArrayList;
import java.util.List;

public class HistoryOldRow {
    private final StringProperty runNumber = new SimpleStringProperty();
    private final ObservableList<VarRow> vars = FXCollections.observableArrayList();
    private final StringProperty y = new SimpleStringProperty();
    private final StringProperty cycles = new SimpleStringProperty();
    private final StringProperty degree = new SimpleStringProperty();
    private final StringProperty programName = new SimpleStringProperty();
    private List<Long> statingInput =  new ArrayList();

    public HistoryOldRow(List<Long>  statingInput) {
        this.statingInput.addAll(statingInput);
    }

    public StringProperty runNumberProperty() { return runNumber; }
    public StringProperty yProperty() { return y; }
    public StringProperty cyclesProperty() { return cycles; }
    public StringProperty degreeProperty() { return degree; }
    public StringProperty programNameProperty() { return programName; }

    public ObservableList<VarRow> getVars() {
        return vars;
    }



    public int getDegree() {
        return Integer.valueOf(degree.getValue());
    }

    public List<Long> getStatingInput() {
        return statingInput;
    }

    public String getFunctionName(){ return programName.getValue(); }
}