package logic.instructions.sinstruction;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

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
