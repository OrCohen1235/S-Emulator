package logic.dto;

import logic.instructions.binstruction.BaseInstruction;
import logic.instructions.Instruction;
import program.*;
import logic.variable.VariableType;

import java.util.*;

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
        Set<String> names = new LinkedHashSet<>();
        program.getInstructions().forEach(instr -> {
            var v = instr.getVar();
            if (v != null && v.getType() == VariableType.INPUT) {
                String rep = v.getRepresentation();
                if (rep != null) {
                    names.add(rep);
                }
            }
        });
        return new ArrayList<>(names);
    }


    public List<String> getListOfExpandCommands() {
        List<String> prints = new ArrayList<>();
        List<Instruction> flattened;

        flattened = program.view().list();
        int index = 1;
        int fatherIndex=1;
        for (Instruction i : flattened) {
            if (Objects.equals(program.getMode(), "EXPANDED")) {
                prints.add(getSingleCommandAndFather(index, i,i.getIndexFatherLocation()));
            } else {
                prints.add(getSingleCommand(index, i));
            }
            index++;
        }
        return prints;
    }

    public String getSingleCommand(int index,Instruction instr) {
        String type = instr instanceof BaseInstruction ? "B" : "S";
        String label = instr.getLabel().getLabelRepresentation();
        String command = instr.getCommand();
        int cycles = instr.getCycles();
        return String.format("#%d (%s) [%-3.5s] %s (%d)",
                index, type, label, command, cycles);

    }
    public String getSingleCommandAndFather(int index,Instruction instr,int fatherIndex) {
        String type = instr instanceof BaseInstruction ? "B" : "S";
        String label = instr.getLabel().getLabelRepresentation();
        String command = instr.getCommand();
        int cycles = instr.getCycles();
        if (instr.getFather() == null) {
            return String.format("#%d (%s) [%-3.5s] %s (%d)",
                    index, type, label, command, cycles);
        }
        else {
            return String.format("%s <<< #%d (%s) [%-3.5s] %s (%d)  ",
                    getSingleCommandAndFather(instr.getIndexFatherLocation(),instr.getFather(),index), index, type, label, command, cycles);
        }
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
