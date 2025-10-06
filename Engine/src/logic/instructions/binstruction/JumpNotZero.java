package logic.instructions.binstruction;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.JumpInstruction;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Optional;

public class JumpNotZero extends Instruction implements BaseInstruction, JumpInstruction {
    private final Label jnzlabel;

    public JumpNotZero (Program program, Variable variable, Label jnzlabel, Label label) {
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
        return InstructionData.JUMP_NOT_ZERO.getCycles();
    }

    @Override
    public Label calculateInstruction() {
        Long check = super.getVarValueFromMap();
        return Optional.ofNullable(super.getVarValueFromMap()).
                filter(v -> v != 0L).
                map(v ->jnzlabel).
                orElse(FixedLabel.EMPTY);
    }

    public String getCommand() {
        if (super.getVar().getRepresentation() == null){
            int x=0;
        }
        return "IF " +super.getVar().getRepresentation()+ " != 0 " +" GOTO "+ jnzlabel.getLabelRepresentation();
    }

    @Override
    public Label getJumpLabel() {
        return jnzlabel;
    }
}
