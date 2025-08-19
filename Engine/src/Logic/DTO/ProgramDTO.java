package Logic.DTO;

import Logic.Instructions.BInstruction.BaseInstruction;
import Logic.Instructions.Instruction;
import Logic.Instructions.SInstruction.SyntheticInstruction;
import Logic.Program;
import Logic.label.Label;
import Logic.variable.Variable;
import Logic.variable.VariableType;
import semulator.ReadSemulatorXml;

import java.io.File;
import java.util.*;

public class ProgramDTO {
    private Program program;

    public ProgramDTO(Program program) {
        this.program = program;

    }
    public String getProgramName() {
        return program.getNameOfProgram();
    }

    public List<String> getVariables() {
        Set<String> names = new LinkedHashSet<>();
        program.getInstrutions().forEach(instr -> {
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




    public List<String> getLabels()
    {
        List<String> argsLabelsNames=new ArrayList<String>();
        program.getInstrutions().forEach(instr -> {
            if (instr.getLabel().getLabelRepresentation() != "EXIT" && instr.getLabel().getLabelRepresentation() != "") {
                argsLabelsNames.add(instr.getLabel().getLabelRepresentation());
            }
        });
        argsLabelsNames.add("EXIT");
        return argsLabelsNames;
    }
    public List<String> getCommands(List<Instruction> listToPrint)
    {
        return getCommandsFromList(listToPrint);
    }

    public List<String> getCommandsFromList(List<Instruction> instrList) {
        int numberOfCommands = instrList.size();
        List<String> commands = new ArrayList<>(numberOfCommands);
        for (int number =0; number < numberOfCommands; number++) {
            String result = getSingleCommandAndFather(number, instrList.get(number));
            commands.add(result);
        }
        return commands;
    }

    public String getSingleCommand(int index,Instruction instr) {
        String type = instr instanceof BaseInstruction ? "B" : "S";
        String label = instr.getLabel().getLabelRepresentation();
        String command = instr.getCommand();
        int cycles = instr.getCycles();
        return String.format("#%d (%s) [%-3.5s] %s (%d)",
                index+1, type, label, command, cycles);

    }
    public String getSingleCommandAndFather(int index,Instruction instr) {
        String type = instr instanceof BaseInstruction ? "B" : "S";
        String label = instr.getLabel().getLabelRepresentation();
        String command = instr.getCommand();
        int cycles = instr.getCycles();
        if (instr.getFather() == null) {
            return String.format("#%d (%s) [%-3.5s] %s (%d)",
                    index+1, type, label, command, cycles);
        }
        else {
            return String.format("%s <<< #%d (%s) [%-3.5s] %s (%d)  ",
                    getSingleCommandAndFather(1+index,instr.getFather()), 1+index, type, label, command, cycles);
        }
    }

    public Map<String,Long> getVarsValues() {
        return program.getVariablesValues();
    }
}
