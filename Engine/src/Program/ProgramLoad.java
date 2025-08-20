package Program;

import Logic.Instructions.BInstruction.Decrease;
import Logic.Instructions.BInstruction.Increase;
import Logic.Instructions.BInstruction.JumpNotZero;
import Logic.Instructions.BInstruction.Neutral;
import Logic.Instructions.Instruction;
import Logic.Instructions.InstructionData;
import Logic.Instructions.SInstruction.*;
import Logic.label.Label;
import Logic.label.LabelImpl;
import Logic.variable.Variable;
import Logic.variable.VariableImpl;
import Logic.variable.VariableType;
import semulator.ReadSemulatorXml;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class ProgramLoad {
    private Program program;

    public ProgramLoad(Program program) {
        this.program = program;
    }

    public void loadProgram(ReadSemulatorXml read) {
        Objects.requireNonNull(read, "ReadSemulatorXml must not be null\n");

        try {
            program.setNameOfProgram(Objects.requireNonNull(read.getProgramName(), "Program name must not be null\n"));

            var sInstructions = Objects.requireNonNull(
                    read.getSInstructionList(),
                    "S-instruction list must not be null\n"
            );

            try{
                Instruction[] built = sInstructions.stream()
                        .map(this::createInstruction)
                        .toArray(Instruction[]::new);

                program.setInstructions(built);
            } catch (ProgramLoadException e) {
                throw new ProgramLoadException(e.getMessage());
            }

            program.setY(0L);

        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ProgramLoadException("Failed to load program '" + read.getProgramName() + "': " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ProgramLoadException("Unexpected error while loading program '" + read.getProgramName() + "'\n", e);
        }
    }

    public void loadInputVars(List<Long> varsInput) {
        Objects.requireNonNull(varsInput, "Variable Input must not be null\n");

        IntStream.range(0, varsInput.size())
                .forEach(i -> {
                    Long v = Objects.requireNonNull(varsInput.get(i),
                            "Input variable at index " + i + " is null");

                    if (v < 0) {
                        throw new IllegalArgumentException(
                                "Input variable x" + (i + 1) +
                                        " must be 0 or a natural number, but was " + v);
                    }

                    Variable x = new VariableImpl(VariableType.INPUT, i + 1);
                    program.setXVariablesToMap(x, v);
                });
    }

    // ==================== Parsing S-Instruction -> Instruction ====================
    public String getArgument(semulator.SInstruction inst) {
        return inst.getSInstructionArguments()
                .getSInstructionArgument()
                .getFirst()
                .getValue();
    }

    private String getArgumentByName(semulator.SInstruction inst, String name) {
        return inst.getSInstructionArguments().getSInstructionArgument().stream()
                .filter(a -> name.equals(a.getName()))
                .map(a -> a.getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing argument: " + name));
    }

    public Instruction createInstruction(semulator.SInstruction inst) {
        Variable newVar  = new VariableImpl(inst.getSVariable());

        Label newLabel = Optional.ofNullable(inst.getSLabel())
                .map(LabelImpl::new)
                .orElse(new LabelImpl("EMPTY"));

        try {
            return switch (InstructionData.fromName(inst.getName())) {
                case INCREASE -> new Increase(program, newVar, newLabel);
                case DECREASE -> new Decrease(program, newVar, newLabel);
                case NEUTRAL  -> new Neutral(program, newVar, newLabel);

                case JUMP_NOT_ZERO -> {
                    Label jumpLabel = new LabelImpl(getArgument(inst));
                    yield new JumpNotZero(program, newVar, jumpLabel, newLabel);
                }
                case ASSIGNMENT -> {
                    Variable assignmentVariable = new VariableImpl(getArgument(inst));
                    yield new Assignment(program, newVar, assignmentVariable, newLabel);
                }
                case CONSTANT_ASSIGNMENT -> {
                    Long constant = Long.parseLong(getArgument(inst));
                    yield new ConstantAssignment(program, newVar, constant, newLabel);
                }
                case GOTO_LABEL -> {
                    Label jumpLabel = new LabelImpl(getArgument(inst));
                    yield new GotoLabel(program, jumpLabel, newLabel);
                }
                case JUMP_ZERO -> {
                    Label jumpLabel = new LabelImpl(getArgument(inst));
                    yield new JumpZero(program, newVar, jumpLabel, newLabel);
                }
                case ZERO_VARIABLE -> new ZeroVariable(program, newVar, newLabel);
                case JUMP_EQUAL_CONSTANT -> {
                    Label jumpLabel = new LabelImpl(getArgumentByName(inst, "JEConstantLabel"));
                    Long constant   = Long.parseLong(getArgumentByName(inst, "constantValue"));
                    yield new JumpEqualConstant(program, newVar, jumpLabel, constant, newLabel);
                }
                case JUMP_EQUAL_VARIABLE -> {
                    Label jumpLabel   = new LabelImpl(getArgumentByName(inst, "JEVariableLabel"));
                    Variable variable = new VariableImpl(getArgumentByName(inst, "variableName"));
                    yield new JumpEqualVariable(program, newVar, jumpLabel, variable, newLabel);
                }
            };
        } catch (IllegalArgumentException e) {
            throw new ProgramLoadException("Failed to load program '" + inst.getName() + "': " + e.getMessage(), e);
        }
    }

}
