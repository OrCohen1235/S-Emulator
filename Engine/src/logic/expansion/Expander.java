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

    private final ExpansionContext context; // Provides fresh labels/work vars and program context
    private final Program program;          // Target program to build expanded instructions for

    public Expander(ExpansionContext ctx) {
        this.context = ctx;
        this.program = context.getProgram();
    }

    public List<Instruction> expand(Instruction inst) {
        return switch (inst.getInstructionData()) {
            case ASSIGNMENT -> expandAssignment((Assignment) inst);                      // v <- v1
            case CONSTANT_ASSIGNMENT ->  expandConstantAssignment((ConstantAssignment) inst); // v <- K
            case GOTO_LABEL ->  expandGotoLabel((GotoLabel) inst);                       // unconditional jump
            case JUMP_EQUAL_CONSTANT  -> expandJumpEqualsConstant((JumpEqualConstant) inst); // if v == K goto L
            case JUMP_EQUAL_VARIABLE ->  expandJumpEqualsVariable((JumpEqualVariable) inst); // if v == v' goto L
            case JUMP_ZERO ->  expandJumpZero((JumpZero) inst);                          // if v == 0 goto L
            case ZERO_VARIABLE ->   expandZeroVariable((ZeroVariable) inst);             // v <- 0
            case JUMP_NOT_ZERO, INCREASE, DECREASE, NEUTRAL -> List.of(inst);           // already primitive
        };

    }

    private List<Instruction> expandAssignment(Assignment instruction) {
        // Implements v <- v1 using a work var z1 and three labels
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();
        Variable v1 = instruction.getAssignedVariable();
        Variable z1 = context.getFreshWorkVal();
        Label L1 = context.getFreshLabel(), L2 = context.getFreshLabel(), L3 = context.getFreshLabel();

        return List.of(
                new ZeroVariable(program, v, instructionLabel),        // v = 0
                new JumpNotZero(program, v1, L1, FixedLabel.EMPTY),    // if v1 != 0 goto L1 else goto L3
                new GotoLabel(program, L3, FixedLabel.EMPTY),

                new Decrease(program, v1, L1),                         // L1: drain v1 into z1
                new Increase(program, z1, FixedLabel.EMPTY),
                new JumpNotZero(program, v1, L1, FixedLabel.EMPTY),

                new Decrease(program, z1, L2),                         // L2: restore v1 and fill v
                new Increase(program, v,FixedLabel.EMPTY),
                new Increase(program, v1, FixedLabel.EMPTY),
                new JumpNotZero(program, z1, L2, FixedLabel.EMPTY),

                new Neutral(program, v, L3)                            // L3: anchor
        );
    }

    private List<Instruction> expandConstantAssignment(ConstantAssignment instruction){
        // Implements v <- K as: v = 0; repeat K times: v++
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();
        Long constant = instruction.getConstantValue();

        List<Instruction> out = new ArrayList<>();
        out.add(new ZeroVariable(program, v, instructionLabel));

        out.addAll(
                java.util.stream.LongStream.range(0, constant) // safe for K >= 0
                        .mapToObj(i -> new Increase(program, v, FixedLabel.EMPTY))
                        .toList()
        );

        return out;
    }

    private List<Instruction> expandGotoLabel(GotoLabel instruction) {
        // Unconditional jump via non-zero sentinel to trigger JNZ
        Variable z1 = context.getFreshWorkVal();
        Label jumpLabel = instruction.calculateInstruction();
        Label instructionLabel = instruction.getLabel();

        return List.of(
                new Increase(program, z1, instructionLabel),          // z1 = 1
                new JumpNotZero(program, z1, jumpLabel, FixedLabel.EMPTY) // jump
        );
    }

    private List<Instruction> expandJumpEqualsConstant(JumpEqualConstant instruction) {
        // Compare v to constant by copying to z1 and decrementing K times
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

        // K iterations: if z1 == 0 goto L1; else z1--
        Optional.of(constant)
                .filter(c -> c > 0)
                .ifPresent(c ->
                        LongStream.range(0, c)
                                .forEach(i -> {
                                    out.add(new JumpZero(program, z1, L1, FixedLabel.EMPTY));
                                    out.add(new Decrease(program, z1, FixedLabel.EMPTY));
                                })
                );

        // After loop: if z1 != 0 -> not equal
        out.add(new JumpNotZero(program, z1, L1, FixedLabel.EMPTY));

        // Equal → jump
        out.add(new GotoLabel(program, jumpLabel, FixedLabel.EMPTY));

        // L1: not equal anchor
        out.add(new Neutral(program, Variable.RESULT, L1));

        return out;
    }


    private List<Instruction> expandJumpEqualsVariable(JumpEqualVariable instruction) {
        // Simultaneously decrement copies z1,z2; equality iff both reach zero together
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

                // L2: loop guard
                new Neutral(program, Variable.RESULT, L2),
                new JumpZero(program, z1, L3, FixedLabel.EMPTY), // if z1==0 break

                // if z2==0 -> not equal
                new JumpZero(program, z2, L1, FixedLabel.EMPTY),

                // z1-- ; z2-- ; goto L2
                new Decrease(program, z1, FixedLabel.EMPTY),
                new Decrease(program, z2, FixedLabel.EMPTY),
                new GotoLabel(program, L2, FixedLabel.EMPTY),

                // L3: if z2==0 -> equal else goto L1
                new Neutral(program, Variable.RESULT, L3),
                new JumpZero(program, z2, jumpLabel, L1),

                new Neutral(program, Variable.RESULT, L1) // not equal anchor
        );
    }


    private List<Instruction> expandJumpZero(JumpZero instruction) {
        // if v == 0 goto L, else fall through
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();
        Label jumpLabel = instruction.getJumpLabel();
        Label L1 = context.getFreshLabel();

        return List.of(
                new JumpNotZero(program, v, L1, instructionLabel), // if v!=0 skip jump
                new GotoLabel(program, jumpLabel, FixedLabel.EMPTY),
                new Neutral(program, Variable.RESULT, L1)          // anchor
        );
    }

    private List<Instruction> expandZeroVariable(ZeroVariable instruction) {
        // Set v to zero by looping: L1: v--; if v!=0 goto L1
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();

        // Reuse valid L* label if present; otherwise allocate fresh
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
