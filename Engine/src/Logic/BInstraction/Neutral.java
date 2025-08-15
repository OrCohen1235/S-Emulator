package Logic.BInstraction;

import Logic.Instruction;
import Logic.InstructionData;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

public class Neutral extends Instruction implements BaseInstruction {

    public Neutral(Program program, Variable variable, Label label) {
        super(program, InstructionData.DECREASE, variable, label);
    }
    public Neutral(Program program, Variable variable) {
        super(program,InstructionData.DECREASE, variable, FixedLabel.EMPTY);
    }
    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int calcCycles() {
        return 0;
    }

    @Override
    public Label calculateInstraction() {
        return FixedLabel.EMPTY;
    }
}
