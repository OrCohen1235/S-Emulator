package menu.context;

import logic.dto.EngineDTO;
import logic.dto.ProgramDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class AppContext {
    private final Scanner in;                    // Shared input scanner for user interactions
    private EngineDTO engineDTO;                 // Current engine wrapper (program + execution)
    private ProgramDTO programDTO;               // Current program DTO for querying/printing
    private int runDegreeATM = 0;                // Chosen expansion degree for the current run
    private final List<HistoryContext> historyContext = new ArrayList<>(); // Execution history
    private int historySize = 0;                 // Cached size of history list

    public AppContext(Scanner in) { this.in = in; } // Inject input source

    public boolean hasProgram() {
        return Optional.ofNullable(engineDTO)
                .map(EngineDTO::getLoaded)
                .orElse(false);                  // True iff an engine is present and loaded
    }

    public Scanner getIn() {
        return in;                               // Expose scanner to actions
    }

    public void setEngine(EngineDTO engine) {
        this.engineDTO = engine;                 // Back-compat alias for setEngineDTO
    }

    public ProgramDTO getProgramDTO() {
        return programDTO;
    }

    public EngineDTO getEngineDTO() {
        return engineDTO;
    }

    public void setProgramDTO(ProgramDTO programDTO) {
        this.programDTO = programDTO;
    }

    public void setEngineDTO(EngineDTO engineDTO) {
        this.engineDTO = engineDTO;
    }

    public int getRunDegreeATM() {
        return runDegreeATM;                     // Current degree selection
    }

    public void setRunDegreeATM(int runDegreeATM) {
        this.runDegreeATM = runDegreeATM;
    }

    public List<HistoryContext> getHistoryContext() {
        return historyContext;                   // Mutable list of past runs
    }

    public int getHistorySize() {
        return historySize;
    }

    public void setHistorySize(int historySize) {
        this.historySize = historySize;
    }
}
