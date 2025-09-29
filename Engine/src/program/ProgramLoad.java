package program;


import generated.*;
import logic.function.Function;
import logic.instructions.binstruction.Decrease;
import logic.instructions.binstruction.Increase;
import logic.instructions.binstruction.JumpNotZero;
import logic.instructions.binstruction.Neutral;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.sinstruction.*;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;
import jaxbsprogram.ReadSemulatorXml;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ProgramLoad {
    private final Program program; // Target program to populate
    private Set<Variable> variables = new HashSet<>();
    private Set<Variable> inputVariables = new HashSet<>();

    public ProgramLoad(Program program) {
        this.program = program;
    }

    public void loadProgram(ReadSemulatorXml read) {
        Objects.requireNonNull(read, "ReadSemulatorXml must not be null\n"); // Defensive null-check

        try {
            program.setNameOfProgram(Objects.requireNonNull(read.getProgramName(), "Program name must not be null\n"));

            var sInstructions = Objects.requireNonNull(
                    read.getSInstructionList(),
                    "S-instruction list must not be null\n"
            );

            var sFunctions = read.getSFunctionList();


            try {
                if (!sFunctions.isEmpty()) {
                    for (SFunction sFunction : sFunctions) {
                        Function f = createFunction(sFunction);
                        program.addSingleFunction(f);
                    }
                    for (Function f : program.getFunctions()) {
                        f.setFunctions(program.getFunctions());
                    }
                }
                Instruction[] instructionBuilt = sInstructions.stream()
                        .map(this::createInstruction) // Parse each SInstruction into an Instruction
                        .toArray(Instruction[]::new);
                program.setInstructions(instructionBuilt); // Load all parsed instructions
            } catch (ProgramLoadException e) {
                throw new ProgramLoadException(e.getMessage());
            }


            program.initAllVariablesToZero(variables);

        } catch (IllegalArgumentException | NullPointerException e) {
            // Wrap parsing/validation errors with program context
            throw new ProgramLoadException("Failed to load program '" + read.getProgramName() + "': " + e.getMessage(), e);
        } catch (RuntimeException e) {
            // Fallback for unexpected runtime issues
            throw new ProgramLoadException(e.getMessage() + "\n" + "Cant Load: " + read.getProgramName() + " File");
        }
    }



    public void loadInputVars(List<Long> varsInput) {
        Objects.requireNonNull(varsInput, "Variable Input must not be null\n"); // Defensive null-check

        IntStream.range(0, varsInput.size())
                .forEach(i -> {
                    Long v = Objects.requireNonNull(varsInput.get(i),
                            "Input variable at index " + i + " is null"); // Disallow null entries

                    Optional.of(v)
                            .filter(val -> val >= 0) // Enforce natural numbers (including 0)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Input variable x" + (i + 1) +
                                            " must be 0 or a natural number, but was " + v));

                    // Map x{i+1} -> value in program state
                    Variable x = new VariableImpl(VariableType.INPUT, i + 1);
                    program.setXVariablesToMap(x, v);
                });
    }

    // ==================== Parsing S-Instruction -> Instruction ====================
    public String getArgument(SInstruction inst) {
        // Return first positional argument value
        return inst.getSInstructionArguments()
                .getSInstructionArgument()
                .getFirst()
                .getValue();
    }

    private String getArgumentByName(SInstruction inst, String name) {
        // Return named argument value or fail if missing
        return inst.getSInstructionArguments().getSInstructionArgument().stream()
                .filter(a -> name.equals(a.getName()))
                .map(SInstructionArgument::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing argument: " + name));
    }

    public Instruction createInstruction(SInstruction inst) {
        Variable newVar  = new VariableImpl(inst.getSVariable()); // Target variable for instruction
        variables.add(newVar);
        if (newVar.getType() == VariableType.INPUT) {
            inputVariables.add(newVar);
        }

        Label newLabel = Optional.ofNullable(inst.getSLabel())
                .map(LabelImpl::new)
                .orElse(new LabelImpl("EMPTY")); // Default label if none provided

        try {
            // Map instruction name to concrete Instruction instance
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
                    variables.add(assignmentVariable);
                    if (assignmentVariable.getType() == VariableType.INPUT) {
                        inputVariables.add(newVar);
                    }
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
                    variables.add(variable);
                    if (variable.getType() == VariableType.INPUT) {
                        inputVariables.add(newVar);
                    }
                    yield new JumpEqualVariable(program, newVar, jumpLabel, variable, newLabel);
                }
                case QUOTE -> {
                    String name = getArgumentByName(inst, "functionName");
                    String argument = getArgumentByName(inst, "functionArguments");
                    Quote quote = new Quote(program,newVar,newLabel,name,argument);
                    List<String> argus = quote.extractXVariables();
                    for (String argu : argus) {
                        variables.add(new VariableImpl(argu));
                        inputVariables.add(new VariableImpl(argu));
                    }
                    yield quote;
                }
                case JUMP_EQUAL_FUNCTION -> {
                    Label jumpLabel = new LabelImpl(getArgumentByName(inst, "JEFunctionLabel"));
                    String name=getArgumentByName(inst, "functionName");
                    String argument=getArgumentByName(inst, "functionArguments");
                    yield new JumpEqualFunction(program,newVar,newLabel,name,argument,jumpLabel);
                }
            };
        } catch (IllegalArgumentException e) {
            // Include instruction name to help locate the failing node
            throw new ProgramLoadException("Failed to load program '" + inst.getName() + "': " + e.getMessage(), e);
        }
    }

    private Function createFunction(SFunction sFunction) {
        Objects.requireNonNull(sFunction, "Function must not be null");
        List<Instruction> instructionList = new ArrayList<>();
        String name = sFunction.getName();
        String userName=sFunction.getUserString();
        List<SInstruction> sInstructionsList= sFunction.getSInstructions().getSInstruction();

        for (SInstruction inst : sInstructionsList) {
            instructionList.add(createInstruction(inst));
        }
        Function function = new Function(name,userName,instructionList);
        return function;
    }

   /* public void loadStartedVars(){
        List<String> lst = getAllVariables();
        for (String var : lst) {
            program.setValueToMapsByString(var);
        }
    }*/

    public List<String> getAllVariables() {
        List<Instruction> instructions = Optional.ofNullable(program.view())
                .map(ProgramView.InstructionsView::list)
                .orElseGet(List::of);

        Set<String> variableNames = new HashSet<>();

        for (Instruction instruction : instructions) {
            if (instruction.getVar() != null) {
                variableNames.add(instruction.getVar().getRepresentation());
            }

            if (instruction instanceof VariableArgumentInstruction vai) {
                if (vai.getVariableArgument() != null) {
                    variableNames.add(vai.getVariableArgument().getRepresentation());
                }
            }
        }
        return new ArrayList<>(variableNames);
    }

    public List<Variable> getInputVariables() {
        List<Variable> variables = new ArrayList<>(inputVariables);
        return variables;
    }


}
