package logic.dto;
import model.HistoryRow;
import java.util.*;
import java.util.stream.Collectors;

public class HistoryRowDTO {
    private int runNumber;
    private boolean mainProgram;
    private String nameOrUserString;
    private String architecture;
    private int degree;
    private long y;
    private long cycles;
    private List<VarRowDTO> vars;
    private List<Long> startingInput;

    // Default constructor for Gson
    public HistoryRowDTO() {
    }

    public HistoryRowDTO(HistoryRow row) {
        this.runNumber = row.getRunNumber();
        this.mainProgram = row.isMainProgram();
        this.nameOrUserString = row.getNameOrUserString();
        this.architecture = row.getArchitecture() != null ? row.getArchitecture().name() : null;
        this.degree = row.getDegree();
        this.y = row.getY();
        this.cycles = row.getCycles();
        this.vars = row.getVars() != null
                ? row.getVars().stream().map(VarRowDTO::new).collect(Collectors.toList())
                : null;
        this.startingInput = row.getStatingInput();
    }

    public List<Long> getStartingInput() {
        return startingInput;
    }

    public int getRunNumber() {
        return runNumber;
    }

    public boolean isMainProgram() {
        return mainProgram;
    }

    public String getNameOrUserString() {
        return nameOrUserString;
    }

    public String getArchitecture() {
        return architecture;
    }

    public int getDegree() {
        return degree;
    }

    public long getY() {
        return y;
    }

    public long getCycles() {
        return cycles;
    }

    public List<VarRowDTO> getVars() {
        return vars;
    }

}