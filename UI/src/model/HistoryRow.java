package model;

import javafx.beans.property.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import viewmodel.Architecture;

public class HistoryRow {

    // 1) מס' הריצה (מתחיל מ־1)
    private final IntegerProperty runNumber = new SimpleIntegerProperty();

    // 2) האם זו תוכנית ראשית או פונקציית עזר
    private final BooleanProperty mainProgram = new SimpleBooleanProperty();

    // 3) שם תוכנית או user string של פונקציה
    private final StringProperty nameOrUserString = new SimpleStringProperty();

    // 4) סוג הארכיטקטורה
    private final ObjectProperty<Architecture> architecture = new SimpleObjectProperty<>();

    // 5) דרגת ההרצה
    private final IntegerProperty degree = new SimpleIntegerProperty();

    // 6) ערכו של y בגמר הריצה
    private final LongProperty y = new SimpleLongProperty();

    // 7) כמות המחזורים שההרצה צרכה
    private final LongProperty cycles = new SimpleLongProperty();

    public HistoryRow(int runNumber,
                      boolean mainProgram,
                      String nameOrUserString,
                      Architecture architecture,
                      int degree,
                      long y,
                      long cycles) {
        this.runNumber.set(runNumber);
        this.mainProgram.set(mainProgram);
        this.nameOrUserString.set(nameOrUserString);
        this.architecture.set(architecture);
        this.degree.set(degree);
        this.y.set(y);
        this.cycles.set(cycles);
    }

    // בנאי עזר אם מגיע מחרוזת ארכיטקטורה (משתמש ב-Architecture.parse)
    public HistoryRow(int runNumber,
                      boolean mainProgram,
                      String nameOrUserString,
                      String architectureStr,
                      int degree,
                      long y,
                      long cycles) {
        this(runNumber, mainProgram, nameOrUserString, Architecture.parse(architectureStr), degree, y, cycles);
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
}
