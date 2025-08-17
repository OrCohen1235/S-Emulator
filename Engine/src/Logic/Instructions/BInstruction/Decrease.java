package Logic.Instructions.BInstruction;
import Logic.Instructions.Instruction;
import Logic.Program;
import Logic.Instructions.InstructionData;
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
    public String getCommand() {
        return super.getVar().getRepresentation()+ " <- " +super.getVar().getRepresentation()+ " - 1";
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
