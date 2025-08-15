package Logic.BInstraction;
import Logic.Instruction;
import Logic.Program;
import Logic.InstructionData;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

public class Decrease extends Instruction implements BaseInstruction {

    public Decrease(Program program, Variable variable, Label label) {
        super(program,InstructionData.DECREASE, variable, label);
    }
    public Decrease(Program program, Variable variable) {
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
        if (returnVal != 0) {
            returnVal = returnVal - 1;
            setVarValueFromMap(returnVal);
        }
        return FixedLabel.EMPTY;
    }
}
