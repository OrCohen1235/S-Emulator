package Logic.expansion;

import Logic.Instructions.BInstruction.Decrease;
import Logic.Instructions.BInstruction.Increase;
import Logic.Instructions.BInstruction.JumpNotZero;
import Logic.Instructions.BInstruction.Neutral;
import Logic.Instructions.Instruction;
import Logic.Instructions.SInstruction.Assignment;
import Logic.Instructions.SInstruction.ConstantAssignment;
import Logic.Instructions.SInstruction.GotoLabel;
import Logic.Instructions.SInstruction.ZeroVariable;
import Logic.Program;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class Expander {

    private final ExpansionContext context;
    Program program;

    Expander(ExpansionContext ctx) {
        this.context = ctx;
        this.program = context.program();
    }

    private List<Instruction> expandAssignment(Assignment instruction) {
        Variable v = instruction.getVar();
        Variable v1 = instruction.getAssignedVariable();
        Variable z1 = context.freshWork();
        Label L1 = context.freshLabel(), L2 = context.freshLabel(), L3 = context.freshLabel();

        return List.of(
                new ZeroVariable(program, v, instruction.getLabel()),
                new JumpNotZero(program, v1, L1, FixedLabel.EMPTY),
                new GotoLabel(program, v, L3, FixedLabel.EMPTY),

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
        Long constant = instruction.getConstantValue();

        List<Instruction> out = new ArrayList<>();
        new ZeroVariable(program, v, instruction.getLabel());

        out.addAll(
                java.util.stream.LongStream.range(0, constant)
                        .mapToObj(i -> new Increase(program, v, FixedLabel.EMPTY))
                        .toList()
        );

        return out;
    }

    private List<Instruction> expandGotoLabel(GotoLabel instruction) {
        Variable z1 = context.freshWork();
        Label jumpLabel = instruction.calculateInstruction();
        Label instructionLabel = instruction.getLabel();

        return List.of(
                new Increase(program, z1, instructionLabel),
                new JumpNotZero(program, z1, jumpLabel, FixedLabel.EMPTY)
        );
    }

}