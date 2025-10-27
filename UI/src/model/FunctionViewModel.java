package model;

import javafx.beans.property.*;

public class FunctionViewModel {
    private final StringProperty functionName = new SimpleStringProperty();
    private final StringProperty uploadProgramName = new SimpleStringProperty();
    private final StringProperty uploader = new SimpleStringProperty();
    private final IntegerProperty instructionCount = new SimpleIntegerProperty();
    private final IntegerProperty maxDegree = new SimpleIntegerProperty();

    public FunctionViewModel(String functionName, String uploadProgramName, String uploader, int instructionCount, int maxDegree) {
        this.functionName.set(functionName);
        this.uploadProgramName.set(uploadProgramName);
        this.uploader.set(uploader);
        this.instructionCount.set(instructionCount);
        this.maxDegree.set(maxDegree);
    }

    // Properties
    public StringProperty FunctionNameProperty() { return functionName; }
    public StringProperty UploadProgramNameProperty() { return uploadProgramName; }
    public StringProperty uploaderProperty() { return uploader; }
    public IntegerProperty instructionCountProperty() { return instructionCount; }
    public IntegerProperty MaxDegreeProperty() { return maxDegree; }


    // Getters
    public String getProgramName() { return functionName.get(); }
    public String getUploadProgramName() { return uploadProgramName.get(); }
    public String getUploader() { return uploader.get(); }
    public int getInstructionCount() { return instructionCount.get(); }
    public int getMaxDegree() { return maxDegree.get(); }


    // Setters (for updates)
    public void setProgramName(String name) { this.functionName.set(name); }
    public void setUploadProgramName(String name) { this.uploadProgramName.set(name); }
    public void setUploader(String uploader) { this.uploader.set(uploader); }
    public void setInstructionCount(int count) { this.instructionCount.set(count); }
    public void setMaxDegree(int level) { this.maxDegree.set(level); }

    public void updateFrom(FunctionViewModel other) {
        this.functionName.set(other.getProgramName());
        this.uploadProgramName.set(other.getUploadProgramName());
        this.uploader.set(other.getUploader());
        this.instructionCount.set(other.getInstructionCount());
        this.maxDegree.set(other.getMaxDegree());

    }
}