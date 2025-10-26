package users;

import logic.function.Function;

public class SystemFunction {
    private final String functionName;
    private final String uploaderProgramName;
    private final String uploaderUserName;
    private final int numberOfInstructions;
    private final int maxDegree;
    private Function function;

    public SystemFunction(String functionName, String uploaderProgramName, String uploaderUserName, int numberOfInstructions, int maxDegree, Function function) {
        this.functionName = functionName;
        this.uploaderProgramName = uploaderProgramName;
        this.uploaderUserName = uploaderUserName;
        this.numberOfInstructions = numberOfInstructions;
        this.maxDegree = maxDegree;
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

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
}
