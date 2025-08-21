package logic.instructions.sinstruction;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Optional;

public class JumpZero extends Instruction implements SyntheticInstruction {

    private final Label jnzlabel;

    public JumpZero (Program program, Variable variable, Label jnzlabel, Label label) {
        super(program, InstructionData.JUMP_ZERO, variable, label);
        this.jnzlabel = jnzlabel;
    }

    public JumpZero (Program program, Variable variable, Label jnzlabel) {
        super(program, InstructionData.JUMP_ZERO, variable, FixedLabel.EMPTY);
        this.jnzlabel = jnzlabel;
    }

    @Override
    public int calcCycles() { return InstructionData.JUMP_ZERO.getCycles(); }

    @Override
    public Label calculateInstruction() {
        return Optional.ofNullable(super.getVarValueFromMap()).
                filter(v -> v == 0L).map(v -> jnzlabel).orElse(FixedLabel.EMPTY);
    }

    public String getCommand() {
        return "IF " +super.getVar().getRepresentation()+ " = 0" +" GOTO "+ jnzlabel.getLabelRepresentation();
    }

    public Label getJnzlabel() {
        return jnzlabel;
    }
}
