package engine;

import Logic.DTO.ProgramDTO;
import Program.*;
import Program.ProgramLoadException;
import Logic.execution.ProgramExecutorImpl;
import Logic.expansion.ExpanderExecute;
import semulator.ReadSemulatorXml;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class Engine {

    // -------------------- Fields --------------------
    private ReadSemulatorXml readSem;
    private final Program program;
    private final ProgramLoad programLoad;
    private final ProgramDTO programDTO;
    private Boolean isLoaded = false;
    private final ProgramExecutorImpl programExecutor;
    private ExpanderExecute expanderExecute;

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
    public void setLoaded(Boolean isLoaded) { this.isLoaded = isLoaded; }
    public ProgramDTO getProgramDTO() { return programDTO; }

    // -------------------- Run / Inputs --------------------
    public Long runProgramExecutor(int degree) {
        return programExecutor.run();
    }

    public void loadInputVars(List<Long> input) {
        programLoad.loadInputVars(input);
    }


    // -------------------- Cycles --------------------

    public ExpanderExecute getExpanderExecute() {
        return expanderExecute;
    }

    /** Better name; keeps logic identical. */
    public void resetSumOfCycles() {
        programExecutor.setSumOfCycles(0);
    }

    public int getSumOfCycles() {
        return programExecutor.getSumOfCycles();
    }


    /** Alias used elsewhere (e.g., Menu). */
    public void setSumOfCycles() { resetSumOfCycles(); }



}
