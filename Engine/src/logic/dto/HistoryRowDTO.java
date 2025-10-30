package logic.dto;
import model.HistoryRow;

public class HistoryRowDTO {
    private int runNumber;
    private boolean mainProgram;
    private String nameOrUserString;
    private String architecture;
    private int degree;
    private long y;
    private long cycles;

    public HistoryRowDTO(HistoryRow row) {
        this.runNumber = row.getRunNumber();
        this.mainProgram = row.isMainProgram();
        this.nameOrUserString = row.getNameOrUserString();
        this.architecture = row.getArchitecture() != null ? row.getArchitecture().name() : null;
        this.degree = row.getDegree();
        this.y = row.getY();
        this.cycles = row.getCycles();
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
}