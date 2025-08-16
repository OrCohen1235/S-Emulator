package Logic.SInstruction;

import Logic.Instruction;
import Logic.InstructionData;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

import java.util.Objects;
import java.util.Optional;

public class JumpEqualConstant extends Instruction implements SyntheticInstruction {
    private final Label jeConstantLabel;
    private final Long constantValue;

    public JumpEqualConstant (Program program, Variable variable, Label jeConstantLabel, Long constantValue, Label label) {
        super(program, InstructionData.JUMP_EQUAL_CONSTANT, variable, label);
        this.jeConstantLabel = jeConstantLabel;
        this.constantValue = constantValue;
    }

    public JumpEqualConstant (Program program, Variable variable, Label jeConstantLabel, Long constantValue) {
        super(program, InstructionData.JUMP_EQUAL_CONSTANT, variable, FixedLabel.EMPTY);
        this.jeConstantLabel = jeConstantLabel;
        this.constantValue = constantValue;
    }

    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int calcCycles() { return InstructionData.JUMP_EQUAL_CONSTANT.getCycles(); }

    @Override
    public Label calculateInstruction() {
        return Optional.ofNullable(super.getVarValueFromMap()).
                filter(v -> v.equals(constantValue)).
                map(v -> jeConstantLabel).
                orElse(FixedLabel.EMPTY);
    }
}
