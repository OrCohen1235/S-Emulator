package logic.dto;

import com.google.gson.annotations.Expose;
import users.SystemProgram;

public class SystemProgramDTO {
    @Expose
    public String name;
    @Expose
    public String uploaderUsername;
    @Expose
    public int instructionCount;
    @Expose
    public int maxDegree;
    @Expose
    public int runCount;
    @Expose
    public double totalCreditsCost;
    @Expose
    public double averageCost;

    public transient ProgramDTO programDTO;

    public SystemProgramDTO(SystemProgram sp) {
        this.name = sp.getName();
        this.uploaderUsername = sp.getUploaderUsername();
        this.instructionCount = sp.getInstructionCount();
        this.maxDegree = sp.getMaxDegree();
        this.runCount = sp.getRunCount();
        this.totalCreditsCost = sp.getTotalCreditsCost();
        this.averageCost = sp.getAverageCost();
        this.programDTO = sp.getProgramDTO();
    }

    // Getters
    public String getName() { return name; }
    public String getUploaderUsername() { return uploaderUsername; }
    public int getInstructionCount() { return instructionCount; }
    public int getMaxDegree() { return maxDegree; }
    public int getRunCount() { return runCount; }
    public double getTotalCreditsCost() { return totalCreditsCost; }
    public double getAverageCost() { return averageCost; }
    public ProgramDTO getProgramDTO() { return programDTO; }

    // Aliases for compatibility
    public String getProgramName() { return name; }
    public int getInstructionsCount() { return instructionCount; }
}