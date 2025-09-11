package logic.dto;

import logic.instructions.Instruction;
import logic.instructions.binstruction.BaseInstruction;
import logic.instructions.sinstruction.VariableArgumentInstruction;
import logic.variable.Variable;
import logic.variable.VariableType;
import program.Program;
import program.ProgramView;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProgramDTO {

    private final Program program;

    public ProgramDTO(Program program) {
        this.program = Objects.requireNonNull(program, "program must not be null");
    }

    /* ========== Meta ========== */

    public String getProgramName() {
        return program.getNameOfProgram();
    }

    public List<String> getLabels() {
        List<String> labels = program.getLabels();
        return labels == null ? List.of() : List.copyOf(labels);
    }

    /* ========== Inputs (VariableType.INPUT) ========== */

    public List<String> getXVariables() {
        // רשימת כל ההוראות במבט הנוכחי (original/expanded)
        List<Instruction> list = Optional.ofNullable(program.view())
                .map(ProgramView.InstructionsView::list)
                .orElseGet(List::of);

        // משתנה ישיר על ההוראה
        Stream<String> base = list.stream()
                .map(instr -> Optional.ofNullable(instr.getVar())
                        .filter(v -> v.getType() == VariableType.INPUT)
                        .map(Variable::getRepresentation)
                        .orElse(null))
                .filter(Objects::nonNull);

        Stream<String> fromInterface = list.stream()
                .filter(VariableArgumentInstruction.class::isInstance)
                .map(VariableArgumentInstruction.class::cast)
                .map(vai -> Optional.ofNullable(vai.getVariableArgument())
                        .filter(v -> v.getType() == VariableType.INPUT)
                        .map(Variable::getRepresentation)
                        .orElse(null))
                .filter(Objects::nonNull);

        return Stream.concat(base, fromInterface)
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    /* ========== Instructions → InstructionDTO ========== */

    public List<InstructionDTO> getInstructionDTOs(){
        List<Instruction> list = program.getInstructions();
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
        Instruction instruction = program.getInstructions().get(index-1);
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


    public Program getProgram() {
        return program;
    }

    public List<String> getAllVariables() {
        // רשימת כל ההוראות במבט הנוכחי (original/expanded)
        List<Instruction> list = Optional.ofNullable(program.view())
                .map(ProgramView.InstructionsView::list)
                .orElseGet(List::of);

        // משתנה ישיר על ההוראה
        Stream<String> base = list.stream()
                .map(instr -> Optional.ofNullable(instr.getVar())
                        .map(Variable::getRepresentation)
                        .orElse(null))
                .filter(Objects::nonNull);

        Stream<String> fromInterface = list.stream()
                .filter(VariableArgumentInstruction.class::isInstance)
                .map(VariableArgumentInstruction.class::cast)
                .map(vai -> Optional.ofNullable(vai.getVariableArgument())
                        .map(Variable::getRepresentation)
                        .orElse(null))
                .filter(Objects::nonNull);

        return Stream.concat(base, fromInterface)
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }
}
