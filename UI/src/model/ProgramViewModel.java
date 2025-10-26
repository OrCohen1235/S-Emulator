package model;

import javafx.beans.property.*;

public class ProgramViewModel {
    private final StringProperty programName = new SimpleStringProperty();
    private final StringProperty uploader = new SimpleStringProperty();
    private final IntegerProperty instructionCount = new SimpleIntegerProperty();
    private final IntegerProperty maxLevel = new SimpleIntegerProperty();
    private final IntegerProperty runCount = new SimpleIntegerProperty();
    private final DoubleProperty avgCost = new SimpleDoubleProperty();

    public ProgramViewModel(String programName, String uploader, int instructionCount,
                            int maxLevel, int runCount, double avgCost) {
        this.programName.set(programName);
        this.uploader.set(uploader);
        this.instructionCount.set(instructionCount);
        this.maxLevel.set(maxLevel);
        this.runCount.set(runCount);
        this.avgCost.set(avgCost);
    }

    // Properties
    public StringProperty programNameProperty() { return programName; }
    public StringProperty uploaderProperty() { return uploader; }
    public IntegerProperty instructionCountProperty() { return instructionCount; }
    public IntegerProperty maxLevelProperty() { return maxLevel; }
    public IntegerProperty runCountProperty() { return runCount; }
    public DoubleProperty avgCostProperty() { return avgCost; }

    // Getters
    public String getProgramName() { return programName.get(); }
    public String getUploader() { return uploader.get(); }
    public int getInstructionCount() { return instructionCount.get(); }
    public int getMaxLevel() { return maxLevel.get(); }
    public int getRunCount() { return runCount.get(); }
    public double getAvgCost() { return avgCost.get(); }

    // Setters (for updates)
    public void setProgramName(String name) { this.programName.set(name); }
    public void setUploader(String uploader) { this.uploader.set(uploader); }
    public void setInstructionCount(int count) { this.instructionCount.set(count); }
    public void setMaxLevel(int level) { this.maxLevel.set(level); }
    public void setRunCount(int count) { this.runCount.set(count); }
    public void setAvgCost(double cost) { this.avgCost.set(cost); }

    public void updateFrom(ProgramViewModel other) {
        this.programName.set(other.getProgramName());
        this.uploader.set(other.getUploader());
        this.instructionCount.set(other.getInstructionCount());
        this.maxLevel.set(other.getMaxLevel());
        this.runCount.set(other.getRunCount());
        this.avgCost.set(other.getAvgCost());
    }
}