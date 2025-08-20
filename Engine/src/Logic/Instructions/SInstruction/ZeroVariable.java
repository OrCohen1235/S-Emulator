package Logic.Instructions.SInstruction;

import Logic.Instructions.Instruction;
import Logic.Instructions.InstructionData;
import Program.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

public class ZeroVariable extends Instruction implements SyntheticInstruction {
    public ZeroVariable(Program program, Variable variable, Label label) {
        super(program, InstructionData.ZERO_VARIABLE, variable, label);
    }
    public ZeroVariable(Program program, Variable variable) {
        super(program,InstructionData.ZERO_VARIABLE, variable, FixedLabel.EMPTY);
    }

    @Override
    public int calcCycles() { return InstructionData.ZERO_VARIABLE.getCycles(); }

    @Override
    public Label calculateInstruction() {
        setVarValueInMap(0);
        return FixedLabel.EMPTY;
    }

    public String getCommand() {
        return super.getVar().getRepresentation()+ " <- 0";
    }
}
