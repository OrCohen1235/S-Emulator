package Logic.Instructions.BInstruction;

import Logic.Instructions.Instruction;
import Logic.Instructions.InstructionData;
import Program.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

public class Neutral extends Instruction implements BaseInstruction {

    public Neutral(Program program, Variable variable, Label label) {
        super(program, InstructionData.NEUTRAL, variable, label);
    }
    public Neutral(Program program, Variable variable) {
        super(program,InstructionData.NEUTRAL, variable, FixedLabel.EMPTY);
    }

    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int calcCycles() {
        return InstructionData.NEUTRAL.getCycles();
    }

    @Override
    public Label calculateInstruction() {
        return FixedLabel.EMPTY;
    }

    public String getCommand() {
        return super.getVar().getRepresentation()+ " <- " +super.getVar().getRepresentation();
    }
}
