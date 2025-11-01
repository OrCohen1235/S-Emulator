package model;

import javafx.beans.property.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import viewmodel.Architecture;

import java.util.ArrayList;
import java.util.List;

public class HistoryRow {

    private final IntegerProperty runNumber = new SimpleIntegerProperty();
    private final BooleanProperty mainProgram = new SimpleBooleanProperty();
    private final StringProperty nameOrUserString = new SimpleStringProperty();
    private final ObjectProperty<Architecture> architecture = new SimpleObjectProperty<>();
    private final IntegerProperty degree = new SimpleIntegerProperty();
    private final LongProperty y = new SimpleLongProperty();
    private final LongProperty cycles = new SimpleLongProperty();
    private List<Long> statingInput =  new ArrayList();
    private final ObservableList<VarRow> vars = FXCollections.observableArrayList();

    public HistoryRow(int runNumber,
                      boolean mainProgram,
                      String nameOrUserString,
                      Architecture architecture,
                      int degree,
                      long y,
                      long cycles,
                      List<VarRow> vars,
                      List<Long> statingInput) {
        this.runNumber.set(runNumber);
        this.mainProgram.set(mainProgram);
        this.nameOrUserString.set(nameOrUserString);
        this.architecture.set(architecture);
        this.degree.set(degree);
        this.y.set(y);
        this.cycles.set(cycles);
        this.vars.addAll(vars);
        this.statingInput.addAll(statingInput);
    }

    // בנאי עזר אם מגיע מחרוזת ארכיטקטורה (משתמש ב-Architecture.parse)
    public HistoryRow(int runNumber,
                      boolean mainProgram,
                      String nameOrUserString,
                      String architectureStr,
                      int degree,
                      long y,
                      long cycles,
                      List<VarRow> vars,
                      List<Long> statingInput) {
        this(runNumber, mainProgram, nameOrUserString, Architecture.parse(architectureStr), degree, y, cycles,vars,statingInput);
    }

    // --- Properties (ל-TableView) ---
    public IntegerProperty runNumberProperty() { return runNumber; }
    public BooleanProperty mainProgramProperty() { return mainProgram; }
    public StringProperty nameOrUserStringProperty() { return nameOrUserString; }
    public ObjectProperty<Architecture> architectureProperty() { return architecture; }
    public IntegerProperty degreeProperty() { return degree; }
    public LongProperty yProperty() { return y; }
    public LongProperty cyclesProperty() { return cycles; }

    // --- Getters ---
    public int getRunNumber() { return runNumber.get(); }
    public boolean isMainProgram() { return mainProgram.get(); }
    public String getNameOrUserString() { return nameOrUserString.get(); }
    public Architecture getArchitecture() { return architecture.get(); }
    public int getDegree() { return degree.get(); }
    public long getY() { return y.get(); }
    public long getCycles() { return cycles.get(); }

    // --- Setters ---
    public void setRunNumber(int value) { runNumber.set(value); }
    public void setMainProgram(boolean value) { mainProgram.set(value); }
    public void setNameOrUserString(String value) { nameOrUserString.set(value); }
    public void setArchitecture(Architecture value) { architecture.set(value); }
    public void setArchitecture(String architectureStr) { architecture.set(Architecture.parse(architectureStr)); }
    public void setDegree(int value) { degree.set(value); }
    public void setY(long value) { y.set(value); }
    public void setCycles(long value) { cycles.set(value); }

    public List<Long> getStatingInput() {
        return statingInput;
    }

    public void setStatingInput(List<Long> statingInput) {
        this.statingInput = statingInput;
    }

    public ObservableList<VarRow> getVars() {
        return vars;
    }

    public HistoryRow(List<Long>  statingInput) {
        this.statingInput.addAll(statingInput);
    }

    public void setAllRemainingHistory(List<VarRow> vars){
        this.vars.setAll(vars);
    }


}