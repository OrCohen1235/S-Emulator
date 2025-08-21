package logic.instructions.sinstruction;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

public class ConstantAssignment extends Instruction implements SyntheticInstruction {
    private final Long constantValue;

    public ConstantAssignment (Program program, Variable variable, Long constantValue, Label label) {
        super(program, InstructionData.CONSTANT_ASSIGNMENT, variable, label);
        this.constantValue = constantValue;
    }

    public ConstantAssignment (Program program, Variable variable, Long constantValue) {
        super(program, InstructionData.CONSTANT_ASSIGNMENT, variable, FixedLabel.EMPTY);
        this.constantValue = constantValue;
    }

    @Override
    public int calcCycles() { return InstructionData.CONSTANT_ASSIGNMENT.getCycles(); }

    @Override
    public Label calculateInstruction() {
        super.setVarValueInMap(constantValue);
        return FixedLabel.EMPTY;
    }

    public String getCommand() {
        return super.getVar().getRepresentation()+ " <- "+constantValue;
    }

    public Long getConstantValue() {
        return constantValue;
    }

}
