package program;

import logic.instructions.Instruction;
import logic.instructions.JumpInstruction;
import logic.label.Label;
import logic.variable.Variable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Program {

    // ==================== Fields ====================
    private String nameOfProgram;                                     // Program name
    private final List<Instruction> instructions = new ArrayList<>(); // Original instruction list
    private Map<Variable, Long> xVariables = new LinkedHashMap<>();     // INPUT variables (xN -> value)
    private Map<Variable, Long> zVariables = new LinkedHashMap<>();     // WORK variables (zN -> value)
    private Map<Variable, Long> y          = new LinkedHashMap<>();     // Output/result variable (Y)
    private List<Instruction> expandInstructionsByDegree = new ArrayList<>(); // Flattened view by degree
    private int maxDegree = -1;                                        // Cached max expansion degree
    private final ProgramView views = new ProgramView(() -> instructions, () -> expandInstructionsByDegree); // View switcher

    public void useOriginalView() { views.useOriginal(); } // Activate original instructions

    public void useExpandedView() { views.useExpanded(); } // Activate expanded/flattened view

    public ProgramView.InstructionsView view() { return views.active(); } // Current active view

    public String getMode() {
        return Optional.ofNullable(views.mode())
                .map(mode -> switch (mode) {
                    case ORIGINAL -> "ORIGINAL";
                    case EXPANDED -> "EXPANDED";
                })
                .orElse("");
    }

    public Instruction getActiveInstruction(int index) {
        return view().getInstructionByIndex(index); // Get instruction from active view
    }

    public int getIndexByInstruction(Instruction inst) {
        return view().getIndexByInstruction(inst); // Index lookup in active view
    }

    public Instruction getInstructionByLabelActive(Label label) {
        return view().getInstructionByLabel(label); // Label lookup in active view
    }

    public int getSizeOfInstructions() {
        return view().getSizeOfListInstructions(); // Size of active instruction list
    }

    // ==================== Load / Init ====================

    public void setInstructions(Instruction... instructions) {
        this.instructions.addAll(Arrays.asList(instructions)); // Append initial instructions
    }

    // ==================== Basic getters/setters ====================
    public String getNameOfProgram() { return nameOfProgram; }

    public List<Instruction> getInstructions() { return instructions; }

    public int getMaxDegree(){
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) { this.maxDegree = maxDegree; } // Cache for UI/queries

    public void setNameOfProgram(String nameOfProgram) {
        this.nameOfProgram = nameOfProgram;
    }


    // ==================== Expansion (flattened list) ====================

    public void setExpandInstructionsByDegree(Collection<Instruction> instructions) {
        this.expandInstructionsByDegree.clear();
        this.expandInstructionsByDegree.addAll(instructions); // Replace flattened list
    }

    // ==================== Variables (get/set) ====================
    public Long getXVariablesFromMap(Variable key) {
        return xVariables.computeIfAbsent(key, k -> 0L); // Default to 0 if missing
    }

    public Long getZVariablesFromMap(Variable key) {
        return zVariables.computeIfAbsent(key, k -> 0L); // Default to 0 if missing
    }

    public void setXVariablesToMap(Variable keyVal, Long returnVal) {
        xVariables.put(keyVal, returnVal); // Set xN value
    }

    public void setZVariablesToMap(Variable keyVal, Long returnVal) {
        zVariables.put(keyVal, returnVal); // Set zN value
    }

    public Long getY() {
        return y.get(Variable.RESULT); // Read Y (may be null)
    }

    public void setY(Long value) {
        this.y.put(Variable.RESULT, value); // Write Y
    }

    public void resetMapVariables() {
        zVariables.clear();
        xVariables.clear();
        setY(0L); // Reset all variables to 0
    }

    // ==================== Aggregated variables view ====================
    public Map<String, Long> getVariablesValues() {
        Map<String, Long> result = new LinkedHashMap<>();

        Long yVal = getY();
        result.put(Variable.RESULT.getRepresentation(), yVal != null ? yVal : 0L); // Y first

        xVariables.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> parseIndex(e.getKey().getRepresentation(), 'x')))
                .forEach(e -> result.put(e.getKey().getRepresentation(),
                        e.getValue() != null ? e.getValue() : 0L)); // x1,x2,... in order

        zVariables.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> parseIndex(e.getKey().getRepresentation(), 'z')))
                .forEach(e -> result.put(e.getKey().getRepresentation(),
                        e.getValue() != null ? e.getValue() : 0L)); // z1,z2,... in order

        return result;
    }

    private int parseIndex(String name, char prefix) {
        return Optional.ofNullable(name)
                .filter(n -> n.length() > 1 && n.charAt(0) == prefix)
                .map(n -> {
                    try {
                        return Integer.parseInt(n.substring(1)); // Extract numeric suffix
                    } catch (NumberFormatException e) {
                        return null; // Non-numeric → push to end
                    }
                })
                .orElse(Integer.MAX_VALUE);
    }


    public List<String> getLabels() {
        boolean hasExit = false; // Tracks if EXIT label exists
        List<String> names = new ArrayList<>();

        for (var instr : view().list()) {
            String lab = instr.getLabel().getLabelRepresentation();
            String lab2 = "";

            if (instr instanceof JumpInstruction) {
                lab2 = ((JumpInstruction) instr).getJumpLabel().getLabelRepresentation(); // Target label
            }

            if ("Exit".equalsIgnoreCase(lab2)){
                hasExit = true;
            }

            if (lab == null || lab.isEmpty()) continue;

            if ("EXIT".equalsIgnoreCase(lab)) {
                hasExit = true;
            } else {
                names.add(lab); // Collect non-EXIT labels
            }
        }

        List<String> sorted = sortLabelsByNumber(names); // Sort L1,L2,...
        if (hasExit) { sorted.add("EXIT"); }             // Append EXIT at end if present
        return sorted;
    }


    public static List<String> sortLabelsByNumber(Collection<String> labels) {
        List<String> sorted = new ArrayList<>(labels);
        final Pattern LABEL = Pattern.compile("^L(\\d+)$", Pattern.CASE_INSENSITIVE);
        sorted.sort(Comparator.comparingInt(s -> {
            if (s == null) return Integer.MAX_VALUE;
            Matcher m = LABEL.matcher(s.trim());
            return m.matches() ? Integer.parseInt(m.group(1)) : Integer.MAX_VALUE; // Non L# go last
        }));

        return sorted;
    }

}
