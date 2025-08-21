package engine;

import logic.dto.ProgramDTO;
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
    private final ReadSemulatorXml readSem;
    private final Program program;
    private final ProgramLoad programLoad;
    private final ProgramDTO programDTO;
    private Boolean isLoaded = false;
    private final ProgramExecutorImpl programExecutor;
    private final ExpanderExecute expanderExecute;

    // -------------------- Constructor --------------------
    public Engine(File file) {
        try {
            readSem = new ReadSemulatorXml(file);
            String label = readSem.checkLabelValidity();
            if (!Objects.equals(label, "")){
                throw new ProgramLoadException("There is a jump command to label " + label + " that does not exist in the program.");
            } else {
                isLoaded = true;
            }
        } catch (Exception e) {
            throw e;
        }

        program = new Program();
        programLoad = new ProgramLoad(program);
        programLoad.loadProgram(readSem);
        programDTO = new ProgramDTO(program);
        programExecutor = new ProgramExecutorImpl(program);
        expanderExecute = new ExpanderExecute(program);
    }

    // -------------------- Basic accessors --------------------
    public Boolean getLoaded() { return isLoaded; }

    public ProgramDTO getProgramDTO() { return programDTO; }

    // -------------------- Run / Inputs --------------------
    public Long runProgramExecutor(int degree) {
        return programExecutor.run();
    }

    public void loadInputVars(List<Long> input) {
        programLoad.loadInputVars(input);
    }

    public ExpanderExecute getExpanderExecute() {
        return expanderExecute;
    }

    public void resetSumOfCycles() {
        programExecutor.resetSumOfCycles();
    }

    public int getSumOfCycles() {
        return programExecutor.getSumOfCycles();
    }
}
