package menu.context;

import java.util.List;

public class HistoryContext {
    private int numberofPrograms;
    private int degree;
    private List<Long> xValues;
    private Long finalResult;
    private int finalCycles;

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

    public int getNumberofPrograms() {
        return numberofPrograms;
    }

    public void setNumberofPrograms(int numberofPrograms) {
        this.numberofPrograms = numberofPrograms;
    }
}
