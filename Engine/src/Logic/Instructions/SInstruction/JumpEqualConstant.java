package Logic.Instructions.SInstruction;

import Logic.Instructions.Instruction;
import Logic.Instructions.InstructionData;
import Program.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

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
    public int calcCycles() { return InstructionData.JUMP_EQUAL_CONSTANT.getCycles(); }

    @Override
    public Label calculateInstruction() {
        return Optional.ofNullable(super.getVarValueFromMap()).
                filter(v -> v.equals(constantValue)).
                map(v -> jeConstantLabel).
                orElse(FixedLabel.EMPTY);
    }

    public String getCommand() {
        return "IF " +super.getVar().getRepresentation()+ " = "+constantValue +" GOTO "+ jeConstantLabel.getLabelRepresentation();
    }

    public Long getConstantValue() {
        return constantValue;
    }

    public Label getJeConstantLabel() {
        return jeConstantLabel;
    }
}
