package engine;

import logic.dto.ProgramDTO;
import logic.instructions.Instruction;
import program.*;
import program.ProgramLoadException;
import logic.execution.ProgramExecutorImpl;
import logic.expansion.ExpanderExecute;
import jaxbsprogram.ReadSemulatorXml;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class Engine {

    // -------------------- Fields --------------------
    private final ReadSemulatorXml readSem; // Responsible for reading XML file
    private final Program program;          // Represents the program itself
    private final ProgramLoad programLoad;  // Handles loading program into Program object
    private final ProgramDTO programDTO;    // DTO object for program data transfer
    private Boolean isLoaded = false;       // Indicates if program was successfully loaded
    private final ProgramExecutorImpl programExecutor; // Executes the program
    private final ExpanderExecute expanderExecute;     // Handles command expansion

    // -------------------- Constructor --------------------
    public Engine(String filePath) {
        try {
            readSem = new ReadSemulatorXml(filePath);
            String label = readSem.checkLabelValidity();
            if (!Objects.equals(label, "")){
                // Thrown if there is a jump to a non-existing label
                throw new ProgramLoadException("There is a jump command to label " + label + " that does not exist in the program.");
            } else {
                isLoaded = true; // Program loaded successfully
            }
        } catch (Exception e) {
            throw e; // Re-throw original exception
        }

        program = new Program();
        programLoad = new ProgramLoad(program);
        programLoad.loadProgram(readSem); // Load program from XML
        programDTO = new ProgramDTO(program);
        programExecutor = new ProgramExecutorImpl(program);
        expanderExecute = new ExpanderExecute(program);
    }

    // -------------------- Basic accessors --------------------
    public Boolean getLoaded() { return isLoaded; }

    public ProgramDTO getProgramDTO() { return programDTO; }

    // -------------------- Run / Inputs --------------------
    public Long runProgramExecutor(int degree) {
        return programExecutor.run(); // Parameter "degree" currently not used
    }

    public Long runProgramExecutorDebugger(int level) {
        return programExecutor.runDebugger(level); // Parameter "degree" currently not used
    }

    public void loadInputVars(List<Long> input) {
        programLoad.loadInputVars(input); // Load input variables into program
    }

    public ExpanderExecute getExpanderExecute() {
        return expanderExecute;
    }

    public void resetSumOfCycles() {
        programExecutor.resetSumOfCycles(); // Reset cycle counter
    }

    public int getSumOfCycles() {
        int sumCycles = programExecutor.getSumOfCycles();
        resetSumOfCycles();
        return sumCycles; // Return total executed cycles
    }

    public int getCurrentInstructionIndex() {
        return programExecutor.getCurrentIndex();
    }


}
