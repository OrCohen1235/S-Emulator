package model;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import program.Program;

import java.util.ArrayList;
import java.util.List;

public class HistoryRow {
    private final StringProperty runNumber = new SimpleStringProperty();
    private final ObservableList<VarRow> vars = FXCollections.observableArrayList();
    private final StringProperty y = new SimpleStringProperty();
    private final StringProperty cycles = new SimpleStringProperty();
    private final StringProperty degree = new SimpleStringProperty();
    private List<Long> statingInput =  new ArrayList();
    private Program program;

    public HistoryRow(List<Long>  statingInput) {
        this.statingInput.addAll(statingInput);
    }

    public StringProperty runNumberProperty() { return runNumber; }
    public StringProperty yProperty() { return y; }
    public StringProperty cyclesProperty() { return cycles; }
    public StringProperty degreeProperty() { return degree; }

    public ObservableList<VarRow> getVars() {
        return vars;
    }

    public void setAllRemainingHistory(Program currentFunction, String runNumber, String y, String degree, String cycles, List<VarRow> vars){
        this.program=currentFunction;
        this.vars.setAll(vars);
        this.runNumber.setValue(runNumber);
        this.y.setValue(y);
        this.cycles.setValue(cycles);
        this.degree.setValue(degree);
    }

    public int getDegree() {
        return Integer.valueOf(degree.getValue());
    }

    public List<Long> getStatingInput() {
        return statingInput;
    }

    public String getFunctionName(){
        return program.getName();
    }
}