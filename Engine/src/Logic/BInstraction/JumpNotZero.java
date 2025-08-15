package Logic.BInstraction;

import Logic.Instruction;
import Logic.InstructionData;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

public class JumpNotZero extends Instruction implements BaseInstruction {
    private Label jnzlabel;

    public JumpNotZero(Program program, Variable variable, Label jnzlabel, Label label) {
        super(program, InstructionData.JUMP_NOT_ZERO, variable, label);
        this.jnzlabel = jnzlabel;
    }

    public JumpNotZero (Program program, Variable variable, Label jnzlabel) {
        super(program, InstructionData.JUMP_NOT_ZERO, variable,FixedLabel.EMPTY);
        this.jnzlabel = jnzlabel;
    }

    @Override
    public int getDegree() {
        return 0;
    }

    @Override
    public int calcCycles() {
        return 2;
    }

    @Override
    public Label calculateInstraction() {
        if (super.getVarValueFromMap() != 0) {
            return jnzlabel;
        }
        return FixedLabel.EMPTY;
    }
}
