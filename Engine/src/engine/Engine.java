package engine;

import Logic.DTO.ProgramDTO;
import Logic.Instructions.BInstruction.BaseInstruction;
import Logic.Instructions.Instruction;
import Logic.Instructions.SInstruction.SyntheticInstruction;
import Logic.Program;
import Logic.execution.ProgramExecutorImpl;
import Logic.expansion.Expander;
import Logic.expansion.ExpansionContext;
import semulator.ReadSemulatorXml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Engine {

    // -------------------- Fields --------------------
    private ReadSemulatorXml readSem;
    private final Program program;
    private final ProgramDTO programDTO;
    private Boolean isLoaded = false;
    private final ProgramExecutorImpl programExecutor;
    private ExpansionContext expansionContext;
    private Expander expander;

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

        // uses programDTO labels to compute next fresh label index

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

    // -------------------- Expansion: compute degrees (full) --------------------
    /** Computes expansion tree and degrees for all instructions (no list is returned). */




    // -------------------- Cycles --------------------

    public int getSumOfCycles() {
        return programExecutor.getSumOfCycles();
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

    public void resetZMapVariables() {
        program.resetZMapVariables();
    }

}
