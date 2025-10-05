package logic.dto;

//import org.glassfish.jaxb.runtime.v2.schemagen.xmlschema.List;
import org.glassfish.jaxb.runtime.v2.schemagen.xmlschema.List;

import java.util.*;

public class InstructionDTO {
    private final int displayIndex;
    private final String type;
    private final String label;
    private final String command;
    private int cycles;
    private int father;

    public InstructionDTO(int displayIndex, String type, String label, String command, int cycles,int father) {
        this.displayIndex = displayIndex;
        this.type = type;
        this.label = label;
        this.command = command;
        this.cycles = cycles;
        this.father = father;
    }
    public int getDisplayIndex() {
        return displayIndex;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getCommand() {
        return command;
    }

    public String getCycles() {
        return String.valueOf(cycles);
    }

    public String getFather() {
        return String.valueOf(father);
    }

    public void setFather(int father) {
        this.father = father;
    }
}
