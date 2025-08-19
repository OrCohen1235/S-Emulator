package engine;

import Logic.DTO.ProgramDTO;
import Logic.Program;
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
            isLoaded = true;
        } catch (Exception e) {
            // intentionally swallowed per original logic
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
        if (degree == 0) {
            return programExecutor.run();
        } else {
            return programExecutor.runByDegree();
        }
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

    // ---- Backwards-compat wrappers
    /** Legacy name kept for compatibility. */
    @Deprecated
    public void ResetSumOfCycles() { resetSumOfCycles(); }

    /** Alias used elsewhere (e.g., Menu). */
    public void setSumOfCycles() { resetSumOfCycles(); }


}
