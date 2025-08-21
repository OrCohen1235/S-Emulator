package logic.instructions.sinstruction;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

public class Assignment extends Instruction implements SyntheticInstruction {
    private final Variable assignedVariable;

    public Assignment (Program program, Variable variable, Variable assignedVariable, Label label) {
        super(program, InstructionData.ASSIGNMENT, variable, label);
        this.assignedVariable = assignedVariable;
    }

    public Assignment (Program program, Variable variable, Variable assignedVariable) {
        super(program, InstructionData.ASSIGNMENT, variable, FixedLabel.EMPTY);
        this.assignedVariable = assignedVariable;
    }

    @Override
    public int calcCycles() { return InstructionData.ASSIGNMENT.getCycles(); }

    @Override
    public Label calculateInstruction() {
        Long assignedVariableValue = getVariableValueFromMap(assignedVariable);
        super.setVarValueInMap(assignedVariableValue);
        return FixedLabel.EMPTY;
    }

    public String getCommand() {
        return super.getVar().getRepresentation()+ " <- " +assignedVariable.getRepresentation();
    }

    public Variable getAssignedVariable() {
        return assignedVariable;
    }
}
