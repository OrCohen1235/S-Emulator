package program;

import jaxbsprogram.ReadSemulatorXml;
import logic.execution.ProgramExecutorImpl;
import logic.expansion.ExpanderExecute;
import logic.function.Function;
import logic.instructions.Instruction;
import logic.instructions.JumpInstruction;
import logic.label.Label;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Program {

    // ==================== Fields ====================
    private String nameOfProgram;                                     // Program name
    private final List<Instruction> instructions = new ArrayList<>(); // Original instruction list
    private final List<Function> functions = new ArrayList<>();
    private Map<Variable, Long> xVariables = new LinkedHashMap<>();     // INPUT variables (xN -> value)
    private Map<Variable, Long> zVariables = new LinkedHashMap<>();     // WORK variables (zN -> value)
    private Map<Variable, Long> y = new LinkedHashMap<>();     // Output/result variable (Y)
    private List<Instruction> expandInstructionsByDegree = new ArrayList<>();
    private List<Instruction> expandInstructionsByDegreeHelper = new ArrayList<>();// Flattened view by degree
    private int maxDegree = -1;                                        // Cached max expansion degree
    private final ProgramView views = new ProgramView(() -> instructions, () -> expandInstructionsByDegree); // View switcher
    private Boolean isMainProgram = false;

    private final ProgramLoad programLoad;
    private final ProgramExecutorImpl programExecutor;
    private final ExpanderExecute expanderExecute;

    public Program(ReadSemulatorXml readSem,Boolean isMainProgram) {
        programLoad = new ProgramLoad(this);
        programLoad.loadProgram(readSem);
        programExecutor = new ProgramExecutorImpl(this);
        expanderExecute = new ExpanderExecute(this);
        this.isMainProgram = isMainProgram;
    }

    public Program() {
        programLoad = new ProgramLoad(this);
        programExecutor = new ProgramExecutorImpl(this);
        expanderExecute = new ExpanderExecute(this);

    }
    public void useOriginalView() {
        views.useOriginal();
    } // Activate original instructions

    public void useExpandedView() {
        views.useExpanded();
    } // Activate expanded/flattened view

    public ProgramView.InstructionsView view() {
        return views.active();
    } // Current active view

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

    public Function getFunctionByName(String name) {
        for (Function f : functions) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return functions.get(0);
    }

    public void addSingleFunction(Function f){
        if (!functions.contains(f)) {
            functions.add(f);
        }
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

    public void clearAndSetInstructions(List<Instruction> lst) {
        this.instructions.clear();
        this.instructions.addAll(lst); // Append initial instructions
    }

    public void setFunctions(Function... functions) {
        this.functions.addAll(Arrays.asList(functions));
    }

    public void setFunctions(List<Function> functions) {
        this.functions.addAll(functions);
    }

    // ==================== Basic getters/setters ====================
    public String getName() {
        return nameOfProgram;
    }

    public List<Instruction> getActiveInstructions() {
        if (views.mode() == ProgramView.Mode.EXPANDED) {
            return expandInstructionsByDegree;
        } else {
            return instructions;
        }
    }

    public List<Instruction> getOriginalInstructions() {
        return instructions;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    } // Cache for UI/queries

    public void setNameOfProgram(String nameOfProgram) {
        this.nameOfProgram = nameOfProgram;
    }


    // ==================== Expansion (flattened list) ====================

    public void setExpandInstructionsByDegree(Collection<Instruction> instructions) {
        this.expandInstructionsByDegree.clear();
        this.expandInstructionsByDegree.addAll(instructions); // Replace flattened list
    }

    public void setExpandInstructionsByDegreeHelper(Collection<Instruction> instructions) {
        this.expandInstructionsByDegreeHelper.clear();
        this.expandInstructionsByDegreeHelper.addAll(instructions); // Replace flattened list
    }

    public List<Instruction> getExpandInstructionsByDegreeHelper() {
        return expandInstructionsByDegreeHelper;
    }

    public int getIndexHelper(Instruction instruction) {
        for (int i = 0; i < expandInstructionsByDegreeHelper.size(); i++) {
            if (expandInstructionsByDegreeHelper.get(i).equals(instruction)) {
                return i + 1;
            }
        }
        return 0;
    }

    public List<Function> getFunctions() {
        return functions;
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
        for (Variable xvar: xVariables.keySet()){
            xVariables.put(xvar, 0L);
        }
        setY(0L); // Reset all variables to 0
    }

    public void resetFunctions(){
        for (Function f: functions) {
            f.resetMapVariables();
        }
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

    public int getMaxWorkIndex() {
        return zVariables.size() + 1;
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

            if ("Exit".equalsIgnoreCase(lab2)) {
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
        if (hasExit) {
            sorted.add("EXIT");
        }             // Append EXIT at end if present
        return sorted;
    }

    public Boolean getIsMainProgram(){
        return isMainProgram;
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

    public Variable getKeyFromMapsByString(String name) {
        String newname= Character.toUpperCase(name.charAt(0))+name.substring(1);
        switch (Character.toUpperCase(name.charAt(0))) {
            case 'X': {
                for (Variable xVar : xVariables.keySet()) {
                    if (xVar.getRepresentation().equals(newname)) {
                        return xVar;
                    }
                }
            }
            case 'Y': {
                return y.keySet().iterator().next();
            }
            case 'Z': {
                for (Variable zVar : zVariables.keySet()) {
                    if (zVar.getRepresentation().equals(newname)) {
                        return zVar;
                    }
                }
            }
            default:
                return null;
        }
    }


    public Long getValueFromMapsByString(String name) {
        String newname=Character.toUpperCase(name.charAt(0))+name.substring(1);
        switch (newname.charAt(0)) {
            case 'X': {
                for (Variable xVar : xVariables.keySet()) {
                    if (xVar.getRepresentation().equals(newname)) {
                        return xVariables.get(xVar);
                    }
                }
            }
            case 'Y': {
                return getY();
            }
            case 'Z': {
                for (Variable zVar : zVariables.keySet()) {
                    if (zVar.getRepresentation().equals(newname)) {
                        return zVariables.get(zVar);
                    }
                }
            }
            default:
                return 0L;
        }
    }

    public void setValueToMapsByString(String name) {
        String newname= Character.toUpperCase(name.charAt(0))+name.substring(1);
        Variable var = new VariableImpl(newname);
        switch (newname.charAt(0)) {
            case 'X': {
                    if (!xVariables.containsKey(var)) {
                        setXVariablesToMap(var, 0L);
                    }
                    break;

            }
            case 'Y': {
                setY(0L);
                break;
            }
            case 'Z': {
                if (!zVariables.containsKey(var)) {
                    setZVariablesToMap(var, 0L);
                }
                break;
                }
        }
    }

    public List<String> getXVarsFromXMap(){
        List<String> xvars = new ArrayList<>();
        for (Variable var : xVariables.keySet()){
            xvars.add(var.getRepresentation());
        }
        return xvars;
    }

    public List<Long> getXVarsValuesFromXMap(){
        List<Long> xvars = new ArrayList<>();
        for (Variable var : xVariables.keySet()){
            xvars.add(xVariables.get(var));
        }
        return xvars;
    }

    public void setValuesToXMap(List<Long> vars) {
        if (vars == null) {
            throw new IllegalArgumentException("Input vars cannot be null");
        }

        if (!xVariables.isEmpty()) {
            int i = 0;
            for (Variable var : xVariables.keySet()) {
                Long value = (i < vars.size() && vars.get(i) != null) ? vars.get(i) : 0L;
                xVariables.put(var, value);
                i++;
            }
            while (i<vars.size()) {
                Variable x = new VariableImpl(VariableType.INPUT, i + 1);
                setXVariablesToMap(x, vars.get(i));
                i++;
            }
        } else {
            programLoad.loadInputVars(vars);
        }
    }

    public Map<Variable, Long> getxVariables() {
        return xVariables;
    }

    public Map<Variable, Long> getzVariables() {
        return zVariables;
    }

    public ProgramLoad getProgramLoad() {
        return programLoad;
    }

    public ProgramExecutorImpl getProgramExecutor() {
        return programExecutor;
    }

    public ExpanderExecute getExpanderExecute() {
        return expanderExecute;
    }

}





