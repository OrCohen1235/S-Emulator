package logic.instructions.sinstruction;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.JumpInstruction;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Optional;

public class JumpEqualConstant extends Instruction implements SyntheticInstruction, JumpInstruction {
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

    @Override
    public Label getJumpLabel() {
        return jeConstantLabel;
    }
}
