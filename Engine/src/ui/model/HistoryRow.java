package ui.model;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class HistoryRow {
    private final StringProperty runNumber = new SimpleStringProperty();
    private final ObservableList<VarRow> vars = FXCollections.observableArrayList();
    private final StringProperty y = new SimpleStringProperty();
    private final StringProperty cycles = new SimpleStringProperty();
    private final StringProperty degree = new SimpleStringProperty();

    public HistoryRow(String runNumber, String y, String degree, String cycles, List<VarRow> vars) {
        this.vars.setAll(vars);
        this.runNumber.set(runNumber);
        this.y.set(y);
        this.cycles.set(cycles);
        this.degree.set(degree);
    }

    public StringProperty runNumberProperty() { return runNumber; }
    public StringProperty yProperty() { return y; }
    public StringProperty cyclesProperty() { return cycles; }
    public StringProperty degreeProperty() { return degree; }

    public ObservableList<VarRow> getVars() {
        return vars;
    }
}
