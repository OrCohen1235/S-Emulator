package Logic.Instructions.SInstruction;

import Logic.Instructions.Instruction;
import Logic.Instructions.InstructionData;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

public class GotoLabel extends Instruction implements SyntheticInstruction {
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
    public int getDegree() { return 0; }

    @Override
    public int calcCycles() { return InstructionData.GOTO_LABEL.getCycles(); }

    @Override
    public Label calculateInstruction() {
        return gotoLabel;
    }

    public String getCommand() {
        return "GOTO "+gotoLabel.getLabelRepresentation();
    }
}
