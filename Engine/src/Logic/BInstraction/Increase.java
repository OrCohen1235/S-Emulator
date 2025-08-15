package Logic.BInstraction;

import Logic.Instruction;
import Logic.InstructionData;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

public class Increase extends Instruction implements BaseInstruction {

    public Increase(Program program, Variable variable, Label label) {
        super(program, InstructionData.DECREASE, variable, label);
    }
    public Increase(Program program, Variable variable) {
        super(program,InstructionData.DECREASE, variable, FixedLabel.EMPTY);
    }
    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int calcCycles() {
        return 1;
    }

    @Override
    public Label calculateInstraction() {
        Long returnVal = super.getVarValueFromMap();
        returnVal = returnVal + 1;
        setVarValueFromMap(returnVal);

        return FixedLabel.EMPTY;
    }
}
