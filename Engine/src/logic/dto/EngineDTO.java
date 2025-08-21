package logic.dto;

import engine.Engine;

import java.io.File;
import java.util.List;

public class EngineDTO {
    private Engine engine;

    public EngineDTO(File file) {
        this.engine = new Engine(file);
    }

    public int getMaxDegree() {
        return engine.getExpanderExecute().getMaxDegree();
    }

    public boolean getLoaded (){
        return engine.getLoaded();
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public ProgramDTO getProgramDTO() {
        return engine.getProgramDTO();
    }

    public void loadExpansion() {
        engine.getExpanderExecute().loadExpansion();
    }

    public void loadExpansionByDegree(int degree) {
        engine.getExpanderExecute().loadExpansionByDegree(degree);
    }

    public void loadInputVars(List<Long> input) {
        engine.loadInputVars(input);
    }

    public Long runProgramExecutor(int degree) {
        return engine.runProgramExecutor(degree);
    }

    public void resetSumOfCycles(){
        engine.resetSumOfCycles();
    }

    public int getSumOfCycles(){
        return engine.getSumOfCycles();
    }
}
