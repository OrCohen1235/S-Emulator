package logic.expansion;

import program.Program;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

public class ExpansionContext {
    private final Program program;
    private int nextWorkIdx;
    private int nextLabelIdx;

    public ExpansionContext(Program program, int startWorkIdx, int startLabelIdx) {
        this.program = program;
        this.nextWorkIdx = startWorkIdx;
        this.nextLabelIdx = startLabelIdx;
    }

    public Program getProgram() {
        return program;
    }

    public Variable getFreshWorkVal() {
        return new VariableImpl(VariableType.WORK, ++nextWorkIdx); // z{n+1}
    }

    public Label getFreshLabel() {
        return new LabelImpl("L" + (++nextLabelIdx));              //
    }

}