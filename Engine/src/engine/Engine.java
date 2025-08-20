package engine;

import Logic.DTO.ProgramDTO;
import Program.Program;
import Program.ProgramLoadException;
import Logic.execution.ProgramExecutorImpl;
import Logic.expansion.ExpanderExecute;
import semulator.ReadSemulatorXml;

import java.io.File;
import java.util.List;

public class Engine {

    // -------------------- Fields --------------------
    private ReadSemulatorXml readSem;
    private final Program program;
    private final ProgramDTO programDTO;
    private Boolean isLoaded = false;
    private final ProgramExecutorImpl programExecutor;
    private ExpanderExecute expanderExecute;

    // -------------------- Constructor --------------------
    public Engine(File file) {
        try {
            readSem = new ReadSemulatorXml(file);
            if (!readSem.checkLabelValidity()){
                throw new ProgramLoadException("Label validation failed");
            } else {
                isLoaded = true;
            }
        } catch (Exception e) {
            throw e;
        }

        program = new Program();
        program.loadProgram(readSem);

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
        program.loadInputVars(input);
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
