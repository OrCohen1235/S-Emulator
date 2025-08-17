package Logic.Instructions.SInstruction;

import Logic.Instructions.Instruction;
import Logic.Instructions.InstructionData;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

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
