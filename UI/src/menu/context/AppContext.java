package menu.context;

import Logic.DTO.ProgramDTO;
import engine.Engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AppContext {
    public final Scanner in;
    public Engine engine;
    public ProgramDTO programDTO;
    public int runDegreeATM = 0;
    public final List<HistoryContext> historyContext = new ArrayList<>();
    public int historySize = 0;

    public AppContext(Scanner in) { this.in = in; }

    public boolean hasProgram() { return engine != null && engine.getLoaded(); }
}
