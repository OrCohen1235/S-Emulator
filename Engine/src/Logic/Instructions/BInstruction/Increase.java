package Logic.Instructions.BInstruction;

import Logic.Instructions.Instruction;
import Logic.Instructions.InstructionData;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

public class Increase extends Instruction implements BaseInstruction {

    public Increase(Program program, Variable variable, Label label) {
        super(program, InstructionData.INCREASE, variable, label);
    }
    public Increase(Program program, Variable variable) {
        super(program,InstructionData.INCREASE, variable, FixedLabel.EMPTY);
    }

    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int calcCycles() {
        return InstructionData.INCREASE.getCycles();
    }

    @Override
    public Label calculateInstruction() {
        Long returnVal = super.getVarValueFromMap();
        returnVal++;
        setVarValueInMap(returnVal);

        return FixedLabel.EMPTY;
    }

    public String getCommand() {
        return super.getVar().getRepresentation()+ " <- " +super.getVar().getRepresentation()+ " +1";
    }
}
