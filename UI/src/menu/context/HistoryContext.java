package menu.context;

import java.util.List;

public class HistoryContext {
    private int numberOfProgram;      // Sequential index of the executed program run
    private int degree;               // Expansion degree used in this run
    private List<Long> xValues;       // Input values provided by the user
    private Long finalResult;         // Final output result after execution
    private int finalCycles;          // Total cycles consumed by execution

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public List<Long> getxValues() {
        return xValues;
    }

    public void setxValues(List<Long> xValues) {
        this.xValues = xValues;
    }

    public Long getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(Long finalResult) {
        this.finalResult = finalResult;
    }

    public int getFinalCycles() {
        return finalCycles;
    }

    public void setFinalCycles(int finalCycles) {
        this.finalCycles = finalCycles;
    }

    public int getNumberOfProgram() {
        return numberOfProgram;
    }

    public void setNumberOfPrograms(int numberOfPrograms) {
        this.numberOfProgram = numberOfPrograms;
    }
}
