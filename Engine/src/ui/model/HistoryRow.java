package ui.model;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class HistoryRow {
    private final StringProperty time = new SimpleStringProperty();
    private final StringProperty inputs = new SimpleStringProperty();
    private final StringProperty y = new SimpleStringProperty();
    private final StringProperty cycles = new SimpleStringProperty();
    private final StringProperty notes = new SimpleStringProperty();

    public HistoryRow(String time, String inputs, String y, String cycles, String notes) {
        this.time.set(time); this.inputs.set(inputs); this.y.set(y);
        this.cycles.set(cycles); this.notes.set(notes);
    }
    public StringProperty timeProperty(){ return time; }
    public StringProperty inputsProperty(){ return inputs; }
    public StringProperty yProperty(){ return y; }
    public StringProperty cyclesProperty(){ return cycles; }
    public StringProperty notesProperty(){ return notes; }
}
