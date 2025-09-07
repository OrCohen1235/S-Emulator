package ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VarRow {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty value = new SimpleStringProperty();

    public VarRow(String name, String type, String value) {
        this.name.set(name); this.type.set(type); this.value.set(value);
    }
    public StringProperty nameProperty() { return name; }
    public StringProperty typeProperty() { return type; }
    public StringProperty valueProperty() { return value; }

    public String getName() {
        return "";
    }
}
