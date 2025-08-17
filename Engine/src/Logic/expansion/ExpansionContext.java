package Logic.expansion;

import Logic.Program;
import Logic.label.Label;
import Logic.label.LabelImpl;
import Logic.variable.Variable;
import Logic.variable.VariableImpl;
import Logic.variable.VariableType;

public class ExpansionContext {
    private final Program program;
    private int nextWorkIdx;
    private int nextInputIdx;
    private int nextLabelIdx;

    public ExpansionContext(Program program, int startWorkIdx, int startLabelIdx) {
        this.program = program;
        this.nextWorkIdx = startWorkIdx;
        this.nextLabelIdx = startLabelIdx;
    }

    public Program program() { return program; }

    public Variable freshWork() {
        return new VariableImpl(VariableType.WORK, ++nextWorkIdx); // z{n+1}
    }

    public Variable freshInput() {
        return new VariableImpl(VariableType.INPUT, ++nextInputIdx); // x{n+1}
    }

    public Label freshLabel() {
        return new LabelImpl("L" + (++nextLabelIdx));              //
    }

}