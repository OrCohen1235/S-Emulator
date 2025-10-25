package users;

import engine.Engine;
import logic.dto.ProgramDTO;

import java.util.Map;

public class SystemProgramsManager {
    private final String name;
    private final String uploaderUsername;
    private final int numberOfInstructions;
    private final Engine engine;
    private final ProgramDTO programDTO;

    private int runCount;
    private double totalCreditsCost;

    public SystemProgramsManager(String uploaderUsername, Engine engine) {
        this.name = engine.getProgramDTO().getProgramName();
        this.uploaderUsername = uploaderUsername;
        this.numberOfInstructions = engine.getProgramDTO().getInstructionDTOs().size();
        this.engine = engine;
        this.programDTO = engine.getProgramDTO();
        this.runCount = 0;
        this.totalCreditsCost = 0.0;
    }

    // Getters
    public String getName() { return name; }
    public String getUploaderUsername() { return uploaderUsername; }
    public Engine getEngine() { return engine; }
    public ProgramDTO getProgramDTO() { return programDTO; }

    // נתונים מה-ProgramDTO
    public int getInstructionCount() {
        // ← תקן את זה - תלוי במה שיש ב-ProgramDTO שלך
        return numberOfInstructions;
    }

    public int getMaxLevel() {
        return programDTO.getMaxDegree();
    }

    public int getRunCount() {
        return runCount;
    }

    public double getAverageCost() {
        return runCount > 0 ? totalCreditsCost / runCount : 0.0;
    }

    // פעולות
    public void addRun(double creditsCost) {
        runCount++;
        totalCreditsCost += creditsCost;
    }

    @Override
    public String toString() {
        return String.format("Program{name='%s', uploader='%s', instructions=%d, maxLevel=%d, runs=%d, avgCost=%.2f}",
                name, uploaderUsername, getInstructionCount(), getMaxLevel(), runCount, getAverageCost());
    }
}