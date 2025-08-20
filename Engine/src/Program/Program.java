package Program;

import Logic.Instructions.Instruction;
import Logic.label.Label;
import Logic.variable.Variable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void setInstructions(Instruction... instructions) {
        this.instructions.addAll(Arrays.asList(instructions));
    }

    // ==================== Basic getters/setters ====================
    public String getNameOfProgram() { return nameOfProgram; }

    public List<Instruction> getInstructions() { return instructions; }

    public void setMaxDegree(int maxDegree) { this.maxDegree = maxDegree; }

    public void setNameOfProgram(String nameOfProgram) {
        this.nameOfProgram = nameOfProgram;
    }


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
