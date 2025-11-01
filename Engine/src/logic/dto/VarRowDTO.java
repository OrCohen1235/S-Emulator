package logic.dto;

public class VarRowDTO {
    private String name;
    private String type;
    private String value;
    private boolean changed;

    // Default constructor for Gson
    public VarRowDTO() {
    }

    public VarRowDTO(model.VarRow varRow) {
        this.name = varRow.getName();
        this.type = varRow.getType();
        this.value = varRow.getValue();
        this.changed = varRow.isChanged();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isChanged() {
        return changed;
    }

    // Convert back to VarRow
    public model.VarRow toVarRow() {
        model.VarRow row = new model.VarRow(name, type, value);
        row.setChanged(changed);
        return row;
    }
}