package menu.context;

import logic.dto.EngineDTO;
import logic.dto.ProgramDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class AppContext {
    private final Scanner in;
    private EngineDTO engineDTO;
    private ProgramDTO programDTO;
    private int runDegreeATM = 0;
    private final List<HistoryContext> historyContext = new ArrayList<>();
    private int historySize = 0;

    public AppContext(Scanner in) { this.in = in; }

    public boolean hasProgram() {
        return Optional.ofNullable(engineDTO)
                .map(EngineDTO::getLoaded)
                .orElse(false);
    }


    public Scanner getIn() {
        return in;
    }

    public void setEngine(EngineDTO engine) {
        this.engineDTO = engine;
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
        return runDegreeATM;
    }

    public void setRunDegreeATM(int runDegreeATM) {
        this.runDegreeATM = runDegreeATM;
    }

    public List<HistoryContext> getHistoryContext() {
        return historyContext;
    }

    public int getHistorySize() {
        return historySize;
    }

    public void setHistorySize(int historySize) {
        this.historySize = historySize;
    }
}
