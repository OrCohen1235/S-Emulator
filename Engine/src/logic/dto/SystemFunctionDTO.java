package logic.dto;

import users.SystemFunction;

public class SystemFunctionDTO {

    private String functionName;
    private String uploaderProgramName;
    private String uploaderUserName;
    private int numberOfInstructions;
    private int maxDegree;

    // --- בנאי רגיל ---
    public SystemFunctionDTO(String functionName,
                             String uploaderProgramName,
                             String uploaderUserName,
                             int numberOfInstructions,
                             int maxDegree) {
        this.functionName = functionName;
        this.uploaderProgramName = uploaderProgramName;
        this.uploaderUserName = uploaderUserName;
        this.numberOfInstructions = numberOfInstructions;
        this.maxDegree = maxDegree;
    }

    // --- 🔹 בנאי חדש שממיר מ-SystemFunction ל-DTO ---
    public SystemFunctionDTO(SystemFunction systemFunction) {
        this.functionName = systemFunction.getFunctionName();
        this.uploaderProgramName = systemFunction.getUploaderProgramName();
        this.uploaderUserName = systemFunction.getUploaderUserName();
        this.numberOfInstructions = systemFunction.getNumberOfInstructions();
        this.maxDegree = systemFunction.getMaxDegree();
    }

    // --- Getters ---
    public String getFunctionName() {
        return functionName;
    }

    public String getUploaderProgramName() {
        return uploaderProgramName;
    }

    public String getUploaderUserName() {
        return uploaderUserName;
    }

    public int getNumberOfInstructions() {
        return numberOfInstructions;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    // --- Setters ---
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void setUploaderProgramName(String uploaderProgramName) {
        this.uploaderProgramName = uploaderProgramName;
    }

    public void setUploaderUserName(String uploaderUserName) {
        this.uploaderUserName = uploaderUserName;
    }

    public void setNumberOfInstructions(int numberOfInstructions) {
        this.numberOfInstructions = numberOfInstructions;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }
}
