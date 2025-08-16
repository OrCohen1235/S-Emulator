package Logic.SInstruction;

import Logic.Instruction;
import Logic.InstructionData;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

import java.util.Optional;

public class JumpEqualVariable extends Instruction implements SyntheticInstruction {
    private final Label jeVariableLabel;
    private final Variable variableName;

    public JumpEqualVariable (Program program, Variable variable, Label jeVariableLabel, Variable variableName, Label label) {
        super(program, InstructionData.JUMP_EQUAL_VARIABLE, variable, label);
        this.jeVariableLabel = jeVariableLabel;
        this.variableName = variableName;
    }

    public JumpEqualVariable (Program program, Variable variable, Label jeVariableLabel, Variable variableName) {
        super(program, InstructionData.JUMP_EQUAL_VARIABLE, variable, FixedLabel.EMPTY);
        this.jeVariableLabel = jeVariableLabel;
        this.variableName = variableName;
    }
    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int calcCycles() { return InstructionData.JUMP_EQUAL_VARIABLE.getCycles(); }

    @Override
    public Label calculateInstruction() {
        return Optional.ofNullable(super.getVarValueFromMap()).
                filter(v -> v.equals(super.getVariableValueFromMap(variableName))).
                map(v -> jeVariableLabel).
                orElse(FixedLabel.EMPTY);
    }
}
