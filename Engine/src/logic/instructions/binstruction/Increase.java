package logic.instructions.binstruction;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

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
        return super.getVar().getRepresentation()+ " <- " +super.getVar().getRepresentation()+ " + 1";
    }
}
