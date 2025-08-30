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

    private final Program program; // Holds reference to the Program instance

    public ProgramDTO(Program program) {
        this.program = program;
    }

    public String getProgramName() {
        return program.getNameOfProgram(); // Return program name
    }

    public List<String> getLabels() {
        return program.getLabels(); // Return all labels from program
    }

    public List<String> getVariables() {
        var list = program.view().list();

        // Collect variables of type INPUT
        var base = list.stream()
                .map(instr -> Optional.ofNullable(instr.getVar())
                        .filter(v -> v.getType() == VariableType.INPUT)
                        .map(Variable::getRepresentation)
                        .orElse(null))
                .filter(Objects::nonNull);

        // Collect variables from VariableArgumentInstruction
        var fromInterface = list.stream()
                .filter(VariableArgumentInstruction.class::isInstance)
                .map(VariableArgumentInstruction.class::cast)
                .map(vai -> Optional.ofNullable(vai.getVariableArgument())
                        .filter(v -> v.getType() == VariableType.INPUT)
                        .map(Variable::getRepresentation)
                        .orElse(null))
                .filter(Objects::nonNull);

        // Merge and return distinct variables
        return Stream.concat(base, fromInterface)
                .distinct()
                .toList();
    }

    public List<String> getListOfExpandCommands() {
        final boolean expanded = "EXPANDED".equals(program.getMode()); // Check if program is expanded
        final AtomicInteger idx = new AtomicInteger(1);

        List<Instruction> flattened = Optional.of(program)
                .map(Program::view)
                .map(ProgramView.InstructionsView::list)
                .orElseGet(Collections::emptyList);

        // Format each instruction depending on mode
        return flattened.stream()
                .map(instr -> expanded
                        ? getSingleCommandAndFather(idx.getAndIncrement(), instr, instr.getIndexFatherLocation())
                        : getSingleCommand(idx.getAndIncrement(), instr))
                .collect(Collectors.toList());
    }

    public String getSingleCommand(int index, Instruction instr) {
        String type = instr instanceof BaseInstruction ? "B" : "S"; // B = binary instruction, S = single
        String label = instr.getLabel().getLabelRepresentation();
        String command = instr.getCommand();
        int cycles = instr.getCycles();
        return String.format("#%d (%s) [%-3.5s] %s (%d)",
                index, type, label, command, cycles); // Format without father reference
    }

    public String getSingleCommandAndFather(int index, Instruction instr, int fatherIndex) {
        String type = instr instanceof BaseInstruction ? "B" : "S";
        String label = instr.getLabel().getLabelRepresentation();
        String command = instr.getCommand();
        int cycles = instr.getCycles();

        String current = String.format("#%d (%s) [%-3.5s] %s (%d)",
                index, type, label, command, cycles);

        return Optional.ofNullable(instr.getFather())
                .map(father -> String.format("%s >>> %s",
                        current,
                        getSingleCommandAndFather(instr.getIndexFatherLocation(), father, index)))
                .orElse(current);
    }


    public Map<String,Long> getVarsValues() {
        return program.getVariablesValues(); // Return current variable values
    }

    public void resetMapVariables() {
        program.resetMapVariables(); // Reset variable values to default
    }

    public void setProgramViewToOriginal(){
        program.useOriginalView(); // Switch to original program view
    }

    public void setProgramViewToExpanded(){
        program.useExpandedView(); // Switch to expanded program view
    }
}
