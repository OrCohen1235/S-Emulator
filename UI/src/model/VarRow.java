package model;

import javafx.beans.property.*;

public class VarRow {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty value = new SimpleStringProperty();

    private final BooleanProperty changed = new SimpleBooleanProperty(false);

    public VarRow(String name, String type, String value) {
        this.name.set(name);
        this.type.set(type);
        this.value.set(value);
    }

    public StringProperty nameProperty()  { return name; }
    public StringProperty typeProperty()  { return type; }
    public StringProperty valueProperty() { return value; }

    public String getName()   { return name.get(); }
    public String getType()   { return type.get(); }
    public String getValue()  { return value.get(); }

    // changed flag
    public BooleanProperty changedProperty() { return changed; }
    public boolean isChanged()               { return changed.get(); }
    public void setChanged(boolean v)        { changed.set(v); }
}
