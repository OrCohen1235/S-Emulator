package logic.instructions.sinstruction;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.JumpInstruction;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;

public class GotoLabel extends Instruction implements SyntheticInstruction, JumpInstruction {
    private final Label gotoLabel;

    public GotoLabel (Program program, Label gotoLabel, Label label) {
        super(program, InstructionData.GOTO_LABEL, label);
        this.gotoLabel = gotoLabel;
    }

    public GotoLabel (Program program, Label gotoLabel) {
        super(program, InstructionData.GOTO_LABEL, FixedLabel.EMPTY);
        this.gotoLabel = gotoLabel;
    }

    @Override
    public int calcCycles() { return InstructionData.GOTO_LABEL.getCycles(); }

    @Override
    public Label calculateInstruction() {
        return gotoLabel;
    }

    public String getCommand() {
        return "GOTO "+gotoLabel.getLabelRepresentation();
    }

    @Override
    public Label getJumpLabel() {
        return gotoLabel;
    }
}
