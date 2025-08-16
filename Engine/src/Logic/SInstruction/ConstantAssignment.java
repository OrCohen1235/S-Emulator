package Logic.SInstruction;

import Logic.Instruction;
import Logic.InstructionData;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

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
    public int getDegree() { return 0; }

    @Override
    public int calcCycles() { return InstructionData.CONSTANT_ASSIGNMENT.getCycles(); }

    @Override
    public Label calculateInstruction() {
        super.setVarValueInMap(constantValue);
        return FixedLabel.EMPTY;
    }
}
