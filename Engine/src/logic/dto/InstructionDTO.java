package logic.dto;

//import org.glassfish.jaxb.runtime.v2.schemagen.xmlschema.List;


public class InstructionDTO {
    private final int displayIndex;
    private final String type;
    private final String label;
    private final String command;
    private int cycles;
    private int father;
    private final String instructionName;
    private final String architecture;

    public InstructionDTO(int displayIndex, String type, String label, String command, int cycles,int father, String instructionName,  String architecture) {
        this.displayIndex = displayIndex;
        this.type = type;
        this.label = label;
        this.command = command;
        this.cycles = cycles;
        this.father = father;
        this.instructionName = instructionName;
        this.architecture = architecture;
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

    public String getArchitecture() { return architecture; }

    public String getCommand() {
        return command;
    }

    public String getCycles() {
        return String.valueOf(cycles);
    }

    public String getInstructionName() {
        return instructionName;
    }

    public String getFather() {
        return String.valueOf(father);
    }

    public void setFather(int father) {
        this.father = father;
    }
}