package logic.dto;

import logic.expansion.ExpanderExecute;
import logic.function.Function;
import logic.instructions.Instruction;
import logic.instructions.binstruction.BaseInstruction;
import logic.instructions.sinstruction.Quote;
import logic.instructions.sinstruction.VariableArgumentInstruction;
import logic.variable.Variable;
import logic.variable.VariableType;
import program.Program;
import program.ProgramView;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProgramDTO {
    private static final Pattern X_VAR = Pattern.compile("\\b(x\\w*)\\b");
    private final Program program;

    public ProgramDTO(Program program) {
        this.program = Objects.requireNonNull(program, "program must not be null");
    }

    /* ========== Meta ========== */

    public String getProgramName() {
        return program.getName();
    }

    public List<String> getLabels() {
        List<String> labels = program.getLabels();
        return labels == null ? List.of() : List.copyOf(labels);
    }

    /* ========== Inputs (VariableType.INPUT) ========== */

    public List<String> getXVariables() {
        // רשימת כל ההוראות במבט הנוכחי (original/expanded)
        List<Instruction> list;
        ProgramView.InstructionsView view = program.view();
        if (view != null && view.list() != null) {
            list = view.list();
        } else {
            list = java.util.Collections.emptyList();
        }

        java.util.LinkedHashSet<String> uniques = new java.util.LinkedHashSet<>();

        // משתנה ישיר על ההוראה (base)
        for (Instruction instr : list) {
            Variable v = instr.getVar();
            if (v != null && v.getType() == VariableType.INPUT) {
                String rep = v.getRepresentation().toLowerCase();
                if (rep != null) {
                    uniques.add(rep);
                }
            }
            if (v!= null && v.getType() == VariableType.WORK) {
                program.getZVariablesFromMap(v);
            }
        }

        // משתנה שמגיע מ-VariableArgumentInstruction (fromInterface)
        for (Instruction instr : list) {
            if (instr instanceof VariableArgumentInstruction) {
                VariableArgumentInstruction vai = (VariableArgumentInstruction) instr;
                Variable v = vai.getVariableArgument();
                if (v != null && v.getType() == VariableType.INPUT) {
                    String rep = v.getRepresentation().toLowerCase();
                    if (rep != null) {
                        uniques.add(rep);
                    }
                }
                if (v!= null && v.getType() == VariableType.WORK) {
                    program.getZVariablesFromMap(v);
                }
            }

            if (instr instanceof Quote) {
                Quote q = (Quote) instr;
                String functionArguments = q.getFunctionArguments();
                if (functionArguments != null && !functionArguments.trim().isEmpty()) {
                    for (String val : functionArguments.split(",")) {
                        if (val == null || val.trim().isEmpty()) continue;

                        Matcher m = X_VAR.matcher(val.trim());
                        while (m.find()) {
                            String varName = m.group(1); // תיקון: group(1) במקום group()
                            if (varName != null && !varName.isEmpty()) {
                                uniques.add(varName);
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(uniques);
    }

    /* ========== Instructions → InstructionDTO ========== */

    public List<InstructionDTO> getInstructionDTOs(){
        List<Instruction> list = program.getActiveInstructions();
        List <InstructionDTO> dtos = new ArrayList<>();
        int index =1;
        for (Instruction instruction : list) {
                InstructionDTO dto =toDTO(index, instruction);
            if (instruction.getFather()!=null)
            {
                dto.setFather(instruction.getIndexFatherLocation());
            }
            index++;
            dtos.add(dto);
        }
        return dtos;


    }

    private InstructionDTO toDTO(int displayIndex, Instruction instr) {
        String type = instr instanceof BaseInstruction ? "B" : "S";
        String label = instr.getLabel() != null ? instr.getLabel().getLabelRepresentation() : "";
        String command = instr.getCommand();
        int cycles = instr.getCycles();

        return new InstructionDTO(displayIndex, type, label, command, cycles, 0);
    }

    public List<InstructionDTO> getExpandDTO(int index){
        List<InstructionDTO> dtos = new ArrayList<>();
        Instruction instruction = program.getActiveInstructions().get(index-1);
        while (instruction.getFather()!=null) {
            InstructionDTO dto = toDTO(instruction.getIndexFatherLocation(), instruction.getFather());
            dtos.add(dto);
            instruction = instruction.getFather();
        }
        return dtos;
    }

    /* ========== Vars state & view switching ========== */

    public String getVarValue(String variable) {
        Map<String, Long> map = program.getVariablesValues();
        if (map.get(variable) == null) {
            return "0";
        }
        return map.get(variable).toString();
    }

    public Map<String, Long> getVariablesValues(){
        return program.getVariablesValues();
    }



    public void resetMapVariables() {
        program.resetMapVariables();
    }

    public void setProgramViewToOriginal() {
        program.useOriginalView();
    }

    public void setProgramViewToExpanded() {
        program.useExpandedView();
    }

    public List<String> getAllVariables(){
        return program.getProgramLoad().getAllVariables();
    }

    public Program getProgram() {
        return program;
    }

    public Long runProgramExecutor(int degree) {
        return program.getProgramExecutor().run(); // Parameter "degree" currently not used
    }

    public Long runProgramExecutorDebugger(int level) {
        return program.getProgramExecutor().runDebugger(level); // Parameter "degree" currently not used
    }

    public void loadInputVars(List<Long> input) {
        for (Function func : program.getFunctions()) {
            func.getProgramLoad().loadInputVars(input);
        }
        program.getProgramLoad().loadInputVars(input); // Load input variables into program
    }

    public ExpanderExecute getExpanderExecute() {
        return program.getExpanderExecute();
    }

    public void resetSumOfCycles() {
        program.getProgramExecutor().resetSumOfCycles(); // Reset cycle counter
    }

    public int getSumOfCycles() {
        return program.getProgramExecutor().getSumOfCycles(); // Return total executed cycles
    }

    public int getSumOfCyclesDebugger() {
        int sum = program.getProgramExecutor().getSumOfCycles();
        program.getProgramExecutor().resetSumOfCycles();
        return sum; // Return total executed cycles
    }

    public int getCurrentInstructionIndex() {
        return program.getProgramExecutor().getCurrentIndex();
    }

    public boolean isFinishedDebugging() {
        return program.getProgramExecutor().getFinishDebugging();
    }

    public void resetDebugger() {
        program.getProgramExecutor().resetDebugger();
    }

    public int getMaxDegree() {
        return program.getExpanderExecute().getMaxDegree(); // Return maximum expansion degree
    }

    public void loadExpansion() {
        program.getExpanderExecute().loadExpansion(); // Load default expansion
    }

    public void loadExpansionByDegree(int degree) {
        program.getExpanderExecute().loadExpansionByDegree(degree); // Load expansion by specific degree
    }

    public List<String> getXAdditioanl(){
        return program.getXVarsFromXMap();
    }


    public void resetFunctions() {
        program.resetFunctions();
    }
}
