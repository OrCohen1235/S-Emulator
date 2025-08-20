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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Program {

    // ==================== Fields ====================
    private String nameOfProgram;
    private List<Instruction> instructions = new ArrayList<Instruction>();
    private Map<Variable, Long> xVariables = new LinkedHashMap();
    private Map<Variable, Long> zVariables = new LinkedHashMap();
    private Map<Variable, Long> y          = new LinkedHashMap();
    private List<Instruction> expandInstructionsByDegree = new ArrayList<Instruction>();
    private int maxDegree = 0;

    private final ProgramView views =
            new ProgramView(() -> instructions, () -> expandInstructionsByDegree);

    public void useOriginalView() { views.useOriginal(); }

    public void useExpandedView() { views.useExpanded(); }

    public ProgramView.InstructionsView view() { return views.active(); }

    public String getMode(){
        if (views.mode() == ProgramView.Mode.ORIGINAL) {
            return "ORIGINAL";
        }
        else if (views.mode() == ProgramView.Mode.EXPANDED) {
            return "EXPANDED";
        }
        return "";
    }

    public Instruction getActiveInstruction(int index) {
        return view().getInstructionByIndex(index);
    }

    public int GetIndexByInstruction(Instruction inst) {
        return view().getIndexByInstruction(inst);
    }

    public Instruction getInstructionByLabelActive(Label label) {
        return view().getInstructionByLabel(label);
    }


    // ==================== Load / Init ====================
    public void loadProgram(ReadSemulatorXml read) {
        Objects.requireNonNull(read, "ReadSemulatorXml must not be null");

        try {
            nameOfProgram = Objects.requireNonNull(read.getProgramName(), "Program name must not be null");

            var sInstructions = Objects.requireNonNull(
                    read.getSInstructionList(),
                    "S-instruction list must not be null"
            );

        try{
        instructions.addAll(
                read.getSInstructionList().stream()
                        .map(this::createInstruction)
                        .toList()
        );
        } catch (ProgramLoadException e) {
            throw new ProgramLoadException(e.getMessage());
        }

        y.put(Variable.RESULT, 0L);

        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ProgramLoadException("Failed to load program '" + read.getProgramName() + "': " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ProgramLoadException("Unexpected error while loading program '" + read.getProgramName() + "'", e);
        }
    }

    public void loadInputVars(List<Long> varsInput) {
        Objects.requireNonNull(varsInput, "varsInput must not be null");

        IntStream.range(0, varsInput.size())
                .forEach(i -> {
                    Long v = Objects.requireNonNull(varsInput.get(i),
                            "Input variable at index " + i + " is null");

                    if (v < 0) {
                        throw new IllegalArgumentException(
                                "Input variable x" + (i + 1) +
                                        " must be 0 or a natural number, but was " + v
                        );
                    }

                    Variable x = new VariableImpl(VariableType.INPUT, i + 1);
                    xVariables.put(x, v);
                });
    }

    public void setInstructions(Instruction... instructions) {
        this.instructions.addAll(Arrays.asList(instructions));
    }

    // ==================== Basic getters/setters ====================
    public String getNameOfProgram() { return nameOfProgram; }

    public List<Instruction> getInstructions() { return instructions; }

    public void setMaxDegree(int maxDegree) { this.maxDegree = maxDegree; }

    // ==================== Expansion (flattened list) ====================
    public List<Instruction> getExpandInstructionsByDegree() {
        return expandInstructionsByDegree;
    }

    public void setExpandInstructionsByDegree(List<Instruction> instructions) {
        this.expandInstructionsByDegree.clear();
        this.expandInstructionsByDegree.addAll(instructions);
    }

    public Instruction getInstruction(int index) {
        return instructions.get(index);
    }

    public Instruction getInstructionByDegree(int index) {
        return expandInstructionsByDegree.get(index);
    }

    // ==================== Lookups by label / index ====================

    // ==================== Variables (get/set) ====================
    public Long getXVariablesFromMap(Variable key) {
        return xVariables.computeIfAbsent(key, k -> 0L);
    }

    public Long getZVariablesFromMap(Variable key) {
        return zVariables.computeIfAbsent(key, k -> 0L);
    }

    public void setXVariablesToMap(Variable keyVal, Long returnVal) {
        xVariables.put(keyVal, returnVal);
    }

    public void setZVariablesToMap(Variable keyVal, Long returnVal) {
        zVariables.put(keyVal, returnVal);
    }

    public Long getY() {
        return y.get(Variable.RESULT);
    }

    public void setY(Long value) {
        this.y.put(Variable.RESULT, value);
    }

    public void resetMapVariables() {
        zVariables.clear();
        setY(0L);
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
            case INCREASE -> new Increase(this, newVar, newLabel);
            case DECREASE -> new Decrease(this, newVar, newLabel);
            case NEUTRAL  -> new Neutral(this, newVar, newLabel);

            case JUMP_NOT_ZERO -> {
                Label jumpLabel = new LabelImpl(getArgument(inst));
                yield new JumpNotZero(this, newVar, jumpLabel, newLabel);
            }
            case ASSIGNMENT -> {
                Variable assignmentVariable = new VariableImpl(getArgument(inst));
                yield new Assignment(this, newVar, assignmentVariable, newLabel);
            }
            case CONSTANT_ASSIGNMENT -> {
                Long constant = Long.parseLong(getArgument(inst));
                yield new ConstantAssignment(this, newVar, constant, newLabel);
            }
            case GOTO_LABEL -> {
                Label jumpLabel = new LabelImpl(getArgument(inst));
                yield new GotoLabel(this, jumpLabel, newLabel);
            }
            case JUMP_ZERO -> {
                Label jumpLabel = new LabelImpl(getArgument(inst));
                yield new JumpZero(this, newVar, jumpLabel, newLabel);
            }
            case ZERO_VARIABLE -> new ZeroVariable(this, newVar, newLabel);
            case JUMP_EQUAL_CONSTANT -> {
                Label jumpLabel = new LabelImpl(getArgumentByName(inst, "JEConstantLabel"));
                Long constant   = Long.parseLong(getArgumentByName(inst, "constantValue"));
                yield new JumpEqualConstant(this, newVar, jumpLabel, constant, newLabel);
            }
            case JUMP_EQUAL_VARIABLE -> {
                Label jumpLabel   = new LabelImpl(getArgumentByName(inst, "JEVariableLabel"));
                Variable variable = new VariableImpl(getArgumentByName(inst, "variableName"));
                yield new JumpEqualVariable(this, newVar, jumpLabel, variable, newLabel);
            }
        };
        } catch (IllegalArgumentException e) {
            throw new ProgramLoadException("Failed to load program '" + inst.getName() + "': " + e.getMessage(), e);
        }
    }

    // ==================== Aggregated variables view ====================
    public Map<String, Long> getVariablesValues() {
        Map<String, Long> result = new LinkedHashMap<>();

        Long yVal = getY();
        result.put(Variable.RESULT.getRepresentation(), yVal != null ? yVal : 0L);

        xVariables.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> parseIndex(e.getKey().getRepresentation(), 'x')))
                .forEach(e -> result.put(e.getKey().getRepresentation(),
                        e.getValue() != null ? e.getValue() : 0L));

        zVariables.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> parseIndex(e.getKey().getRepresentation(), 'z')))
                .forEach(e -> result.put(e.getKey().getRepresentation(),
                        e.getValue() != null ? e.getValue() : 0L));

        return result;
    }

    private int parseIndex(String name, char prefix) {
        if (name != null && name.length() > 1 && name.charAt(0) == prefix) {
            try {
                return Integer.parseInt(name.substring(1));
            } catch (NumberFormatException ignored) {}
        }
        return Integer.MAX_VALUE;
    }

    public List<String> getLabels() {
        List<String> names = new ArrayList<>();
        view().stream().forEach(instr -> {
            String lab = instr.getLabel().getLabelRepresentation();
            if (lab != null && !lab.isEmpty() && !"EXIT".equalsIgnoreCase(lab)) {
                names.add(lab);
            }
        });
        return sortLabelsByNumber(names);
    }

    public static List<String> sortLabelsByNumber(List<String> labels) {
        List<String> sorted = new ArrayList<>(labels);
        final Pattern LABEL = Pattern.compile("^L(\\d+)$", Pattern.CASE_INSENSITIVE);

        sorted.sort(Comparator.comparingInt(s -> {
            if (s == null) return Integer.MAX_VALUE;
            Matcher m = LABEL.matcher(s.trim());
            return m.matches() ? Integer.parseInt(m.group(1)) : Integer.MAX_VALUE;
        }));

        sorted.add("EXIT");
        return sorted;
    }
}
