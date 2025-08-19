package menu.context;

import Logic.DTO.ProgramDTO;
import engine.Engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class AppContext {
    private final Scanner in;
    private Engine engine;
    private ProgramDTO programDTO;
    private int runDegreeATM = 0; //what is runDegreeATM?
    private final List<HistoryContext> historyContext = new ArrayList<>();
    private int historySize = 0;

    public AppContext(Scanner in) { this.in = in; }

    public boolean hasProgram() { return engine != null && engine.getLoaded(); }

    public Scanner getIn() {
        return in;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public ProgramDTO getProgramDTO() {
        return programDTO;
    }

    public void setProgramDTO(ProgramDTO programDTO) {
        this.programDTO = programDTO;
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
