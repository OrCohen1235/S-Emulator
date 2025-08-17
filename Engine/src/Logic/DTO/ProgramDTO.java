package Logic.DTO;

import Logic.Instructions.BInstruction.BaseInstruction;
import Logic.Program;
import Logic.label.Label;
import Logic.variable.Variable;
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
        Set<String> argsNames = new LinkedHashSet<>();
        program.getInstrutions().forEach(instr -> {
            argsNames.add(instr.getVar().getRepresentation());
        });
        return new ArrayList<>(argsNames);
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

    public List<String> getCommands() {
        int numberOfCommands = program.getInstrutions().size();
        List<String> commands = new ArrayList<>(numberOfCommands);
        for (int number =0; number < numberOfCommands; number++) {
            String type = (program.getInstrutions().get(number) instanceof BaseInstruction) ? "B" : "S";
            String label = program.getInstrutions().get(number).getLabel().getLabelRepresentation();
            String command = program.getInstrutions().get(number).getCommand();
            int cycles = program.getInstrutions().get(number).getCycles();
            String result = String.format("# %d (%s) [%s] %s (%d)",
                    number+1, type, label, command, cycles);
            commands.add(result);
        }
        return commands;
    }

    public int getMaxDegree()
    {
        return 10; // return max Degree
    }


}
