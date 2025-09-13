package logic.dto;

import engine.Engine;

import java.util.List;

public class EngineDTO {
    private final Engine engine; // Wraps the Engine instance

    public EngineDTO(String filePath) {
        this.engine = new Engine(filePath); // Initialize Engine with XML file
    }

    public int getMaxDegree() {
        return engine.getExpanderExecute().getMaxDegree(); // Return maximum expansion degree
    }

    public boolean getLoaded (){
        return engine.getLoaded(); // Check if program was successfully loaded
    }

    public ProgramDTO getProgramDTO() {
        return engine.getProgramDTO(); // Return program DTO
    }

    public void loadExpansion() {
        engine.getExpanderExecute().loadExpansion(); // Load default expansion
    }

    public void loadExpansionByDegree(int degree) {
        engine.getExpanderExecute().loadExpansionByDegree(degree); // Load expansion by specific degree
    }

    public void loadInputVars(List<Long> input) {
        engine.loadInputVars(input); // Load input variables into program
    }

    public Long runProgramExecutor(int degree) {
        return engine.runProgramExecutor(degree); // Run program execution
    }

    public void resetSumOfCycles(){
        engine.resetSumOfCycles(); // Reset total cycles
    }

    public int getSumOfCycles(){
        return engine.getSumOfCycles(); // Return total cycles
    }

    public long runProgramExecutorDebugger(int level) {
        return engine.runProgramExecutorDebugger(level);
    }

    public int getCurrentInsructionIndex(){
        return engine.getCurrentInstructionIndex();
    }

    public boolean isFinishedDebugging(){
        return engine.isFinishedDebugging();
    }


    public void resetDebugger() {
        engine.resetDebugger();
    }

    public int getSumOfCyclesDebugger() {
        return engine.getSumOfCyclesDebugger();
    }
}
