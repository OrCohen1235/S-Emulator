package logic.dto;

import logic.instructions.binstruction.BaseInstruction;
import logic.instructions.Instruction;
import logic.instructions.sinstruction.VariableArgumentInstruction;
import logic.variable.Variable;
import program.*;
import logic.variable.VariableType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProgramDTO {

    private final Program program;

    public ProgramDTO(Program program) {
        this.program = program;
    }

    public String getProgramName() {
        return program.getNameOfProgram();
    }

    public List<String> getLabels() {
        return program.getLabels();
    }
    public List<String> getVariables() {
        var list = program.view().list();

        var base = list.stream()
                .map(instr -> Optional.ofNullable(instr.getVar())
                        .filter(v -> v.getType() == VariableType.INPUT)
                        .map(Variable::getRepresentation)
                        .orElse(null))
                .filter(Objects::nonNull);

        var fromInterface = list.stream()
                .filter(VariableArgumentInstruction.class::isInstance)
                .map(VariableArgumentInstruction.class::cast)
                .map(vai -> Optional.ofNullable(vai.getVariableArgument())
                        .map(Variable::getRepresentation)
                        .orElse(null))
                .filter(Objects::nonNull);

        return Stream.concat(base, fromInterface)
                .distinct()
                .toList();
    }


    public List<String> getListOfExpandCommands() {
        final boolean expanded = "EXPANDED".equals(program.getMode());
        final AtomicInteger idx = new AtomicInteger(1);

        List<Instruction> flattened = Optional.of(program)
                .map(Program::view)
                .map(ProgramView.InstructionsView::list)
                .orElseGet(Collections::emptyList);

        return flattened.stream()
                .map(instr -> expanded
                        ? getSingleCommandAndFather(idx.getAndIncrement(), instr, instr.getIndexFatherLocation())
                        : getSingleCommand(idx.getAndIncrement(), instr))
                .collect(Collectors.toList());
    }
    public String getSingleCommand(int index,Instruction instr) {
        String type = instr instanceof BaseInstruction ? "B" : "S";
        String label = instr.getLabel().getLabelRepresentation();
        String command = instr.getCommand();
        int cycles = instr.getCycles();
        return String.format("#%d (%s) [%-3.5s] %s (%d)",
                index, type, label, command, cycles);
    }

    public String getSingleCommandAndFather(int index, Instruction instr, int fatherIndex) {
        String type = instr instanceof BaseInstruction ? "B" : "S";
        String label = instr.getLabel().getLabelRepresentation();
        String command = instr.getCommand();
        int cycles = instr.getCycles();

        return Optional.ofNullable(instr.getFather())
                .map(father -> String.format("%s <<< #%d (%s) [%-3.5s] %s (%d)",
                        getSingleCommandAndFather(instr.getIndexFatherLocation(), father, index),
                        index, type, label, command, cycles))
                .orElseGet(() -> String.format("#%d (%s) [%-3.5s] %s (%d)",
                        index, type, label, command, cycles));
    }


    public Map<String,Long> getVarsValues() {
        return program.getVariablesValues();
    }

    public void resetMapVariables() {
        program.resetMapVariables();
    }

    public void setProgramViewToOriginal(){
        program.useOriginalView();
    }

    public void setProgramViewToExpanded(){
        program.useExpandedView();
    }
}
