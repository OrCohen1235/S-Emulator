package Logic.BInstruction;
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
        return InstructionData.DECREASE.getCycles();
    }

    @Override
    public Label calculateInstruction() {
        Long returnVal = super.getVarValueFromMap();
        if (returnVal != 0) {
            returnVal--;
            setVarValueInMap(returnVal);
        }
        return FixedLabel.EMPTY;
    }
}
