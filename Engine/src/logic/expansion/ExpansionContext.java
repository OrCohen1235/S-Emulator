package logic.expansion;

import program.Program;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.Map;

public class ExpansionContext {
    private final Program program; // Owning program context
    private int nextWorkIdx;       // Counter for WORK variables (zN)
    private int nextLabelIdx;// Counter for generated labels (LN)

    public ExpansionContext(Program program, int startWorkIdx, int startLabelIdx) {
        this.program = program;
        this.nextWorkIdx = startWorkIdx;
        this.nextLabelIdx = startLabelIdx;
    }

    public Program getProgram() {
        return program; // Expose program reference
    }

    public Variable getFreshWorkVal() {
        return new VariableImpl(VariableType.WORK, nextWorkIdx++); // Create z{n+1}
    }

    public Label getFreshLabel() {
        return new LabelImpl("L" + (nextLabelIdx++)); // Create L{n+1}
    }

    public int getNextWorkIdx() {
        return nextWorkIdx;
    }

    public int getNextLabelIdx() {
        return nextLabelIdx;
    }

    public void setNextWorkIdx(int nextWorkIdx) {
        this.nextWorkIdx = nextWorkIdx;
    }

    public void setNextLabelIdx(int nextLabelIdx) {
        this.nextLabelIdx = nextLabelIdx;
    }
}
