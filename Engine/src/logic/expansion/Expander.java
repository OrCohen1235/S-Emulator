package logic.expansion;

import logic.instructions.binstruction.Decrease;
import logic.instructions.binstruction.Increase;
import logic.instructions.binstruction.JumpNotZero;
import logic.instructions.binstruction.Neutral;
import logic.instructions.Instruction;
import logic.instructions.sinstruction.*;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

public class Expander {

    private final ExpansionContext context;
    private final Program program;

    public Expander(ExpansionContext ctx) {
        this.context = ctx;
        this.program = context.getProgram();
    }

    public List<Instruction> expand(Instruction inst) {
        return switch (inst.getInstructionData()) {
            case ASSIGNMENT -> expandAssignment((Assignment) inst);
            case CONSTANT_ASSIGNMENT ->  expandConstantAssignment((ConstantAssignment) inst);
            case GOTO_LABEL ->  expandGotoLabel((GotoLabel) inst);
            case JUMP_EQUAL_CONSTANT  -> expandJumpEqualsConstant((JumpEqualConstant) inst);
            case JUMP_EQUAL_VARIABLE ->  expandJumpEqualsVariable((JumpEqualVariable) inst);
            case JUMP_ZERO ->  expandJumpZero((JumpZero) inst);
            case ZERO_VARIABLE ->   expandZeroVariable((ZeroVariable) inst);
            case JUMP_NOT_ZERO, INCREASE, DECREASE, NEUTRAL -> List.of(inst);
        };

    }

    private List<Instruction> expandAssignment(Assignment instruction) {
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();
        Variable v1 = instruction.getAssignedVariable();
        Variable z1 = context.getFreshWorkVal();
        Label L1 = context.getFreshLabel(), L2 = context.getFreshLabel(), L3 = context.getFreshLabel();

        return List.of(
                new ZeroVariable(program, v, instructionLabel),
                new JumpNotZero(program, v1, L1, FixedLabel.EMPTY),
                new GotoLabel(program, L3, FixedLabel.EMPTY),

                new Decrease(program, v1, L1),
                new Increase(program, z1, FixedLabel.EMPTY),
                new JumpNotZero(program, v1, L1, FixedLabel.EMPTY),

                new Decrease(program, z1, L2),
                new Increase(program, v,FixedLabel.EMPTY),
                new Increase(program, v1, FixedLabel.EMPTY),
                new JumpNotZero(program, z1, L2, FixedLabel.EMPTY),

                new Neutral(program, v, L3)
        );
    }

    private List<Instruction> expandConstantAssignment(ConstantAssignment instruction){
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();
        Long constant = instruction.getConstantValue();

        List<Instruction> out = new ArrayList<>();
        out.add(new ZeroVariable(program, v, instructionLabel));

        out.addAll(
                java.util.stream.LongStream.range(0, constant)
                        .mapToObj(i -> new Increase(program, v, FixedLabel.EMPTY))
                        .toList()
        );

        return out;
    }

    private List<Instruction> expandGotoLabel(GotoLabel instruction) {
        Variable z1 = context.getFreshWorkVal();
        Label jumpLabel = instruction.calculateInstruction();
        Label instructionLabel = instruction.getLabel();


        return List.of(
                new Increase(program, z1, instructionLabel),
                new JumpNotZero(program, z1, jumpLabel, FixedLabel.EMPTY)
        );
    }

    private List<Instruction> expandJumpEqualsConstant(JumpEqualConstant instruction) {
        Variable v         = instruction.getVar();
        Label    jumpLabel = instruction.getJumpLabel();
        Long     constantL = instruction.getConstantValue();
        Label    instrLbl  = instruction.getLabel();

        if (constantL == null || constantL < 0) {
            throw new IllegalArgumentException("In command expandJumpEquals constant constant must be non-negative.\n");
        }
        long constant = constantL;

        Variable z1 = context.getFreshWorkVal();
        Label L1 = context.getFreshLabel();

        List<Instruction> out = new ArrayList<>();

        // z1 <- V
        out.add(new Assignment(program, z1, v, instrLbl));

        // K iterations: IF z1 = 0 GOTO L1; z1 <- z1 - 1
        Optional.of(constant)
                .filter(c -> c > 0)
                .ifPresent(c ->
                        LongStream.range(0, c)
                                .forEach(i -> {
                                    out.add(new JumpZero(program, z1, L1, FixedLabel.EMPTY));
                                    out.add(new Decrease(program, z1, FixedLabel.EMPTY));
                                })
                );

        // After loop: IF z1 != 0 GOTO L1
        out.add(new JumpNotZero(program, z1, L1, FixedLabel.EMPTY));

        // equal → GOTO L
        out.add(new GotoLabel(program, jumpLabel, FixedLabel.EMPTY));

        // L1: anchor (not equal)
        out.add(new Neutral(program, Variable.RESULT, L1));

        return out;
    }


    private List<Instruction> expandJumpEqualsVariable(JumpEqualVariable instruction) {
        Variable v         = instruction.getVar();
        Variable v1        = instruction.getVariableName();
        Label   instrLabel = instruction.getLabel();
        Label   jumpLabel  = instruction.getJumpLabel();

        Variable z1 = context.getFreshWorkVal();
        Variable z2 = context.getFreshWorkVal();
        Label L1 = context.getFreshLabel();
        Label L2 = context.getFreshLabel();
        Label L3 = context.getFreshLabel();

        return List.of(
                // z1 <- V, z2 <- V'
                new Assignment(program, z1, v,  instrLabel),
                new Assignment(program, z2, v1, FixedLabel.EMPTY),

                // L2: IF z1 = 0 GOTO L3
                new Neutral(program, Variable.RESULT, L2),
                new JumpZero(program, z1, L3, FixedLabel.EMPTY),

                // IF z2 = 0 GOTO L1
                new JumpZero(program, z2, L1, FixedLabel.EMPTY),

                // z1 <- z1 - 1 ; z2 <- z2 - 1 ; GOTO L2
                new Decrease(program, z1, FixedLabel.EMPTY),
                new Decrease(program, z2, FixedLabel.EMPTY),
                new GotoLabel(program, L2, FixedLabel.EMPTY),

                // L3: IF z2 = 0 GOTO L (equal) else GOTO L1 (not equal)
                new Neutral(program, Variable.RESULT, L3),
                new JumpZero(program, z2, jumpLabel, L1),

                new Neutral(program, Variable.RESULT, L1)
        );
    }


    private List<Instruction> expandJumpZero(JumpZero instruction) {
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();
        Label jumpLabel = instruction.getJumpLabel();
        Label L1 = context.getFreshLabel();


        return List.of(
                new JumpNotZero(program, v, L1, instructionLabel),
                new GotoLabel(program, jumpLabel, FixedLabel.EMPTY),
                new Neutral(program, Variable.RESULT, L1)
        );
    }

    private List<Instruction> expandZeroVariable(ZeroVariable instruction) {
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();


        Label L1 = Optional.ofNullable(instructionLabel)
                .filter(l -> {
                    String t = l.getLabelRepresentation();
                    return t.matches("(?i)L\\d+") && Integer.parseInt(t.substring(1)) >= 1;
                }).orElseGet(context::getFreshLabel);

        return List.of(
                new Decrease(program, v, L1),
                new JumpNotZero(program, v, L1)
        );
    }

}