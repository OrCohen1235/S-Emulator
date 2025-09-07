
package ui.services;

import logic.dto.EngineDTO;
import logic.dto.InstructionDTO;
import logic.dto.ProgramDTO;
import program.ProgramLoadException;
import ui.SemulatorController;

import java.io.File;
import java.util.*;
import java.nio.file.Path;

public class ProgramService {
    private EngineDTO engine;
    private ProgramDTO program;

    /** טוען XML ומעדכן את המצב הפנימי. זורק ProgramLoadException אם יש בעיה. */
    public void loadXml(File xmlPath) throws ProgramLoadException {

        EngineDTO probe = new EngineDTO(xmlPath.toString());
        if (!probe.getLoaded()) {
            throw new ProgramLoadException("XML not valid for application semantics.");
        }
        this.engine = new EngineDTO(xmlPath.toString());
        this.program = engine.getProgramDTO();
    }

    public int getMaxDegree() {
        engine.loadExpansion();
        return engine.getMaxDegree();
    }

    public void loadExpasionByDegree(int degree){
        if (degree > 0) {
            program.setProgramViewToExpanded();
        }
        else {
            program.setProgramViewToOriginal();
        }
        engine.loadExpansionByDegree(degree);
    }

    public boolean hasProgram() { return program != null; }
    public ProgramDTO getProgram() { return program; }
    public EngineDTO getEngine() { return engine; }

    public List<InstructionDTO> getExpansionFor(InstructionDTO parent) {
        return program.getExpandDTO(parent.getDisplayIndex());
    }

    public List<SemulatorController.VarRow> getVariables(){
        List<SemulatorController.VarRow> rows = new ArrayList<>();
        for (String var : program.getVariables()) {
            SemulatorController.VarRow varRow= new SemulatorController.VarRow(var.toUpperCase(),"INPUT", program.getVarValue(var));
            rows.add(varRow);
        }
        return rows;
    }

    public List<SemulatorController.VarRow> getVariablesEND() {
        List<SemulatorController.VarRow> rows = new ArrayList<>();
        Map<String, Long> values = program.getVariablesValues();
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            if (!Objects.equals(entry.getKey(), "y")) {
                String varName = entry.getKey().toUpperCase();
                String type;
                if (Objects.equals(entry.getKey().charAt(0), 'x')) {
                    type= "INPUT";  // This is hardcoded; adjust if needed
                }
                else {
                    type = "WORK";
                }
                String valueStr = String.valueOf(entry.getValue());


                SemulatorController.VarRow varRow = new SemulatorController.VarRow(varName, type, valueStr);
                rows.add(varRow);
            }
        }
        return rows;
    }

    public void loadVars(List<Long> vars){
        engine.loadInputVars(vars);
    }

    public long executeProgram(int degree) {
        if (degree > 0) {
            program.setProgramViewToExpanded();
        }
        else {
            program.setProgramViewToOriginal();
        }
        return engine.runProgramExecutor(degree);
    }

    public long executeProgramDebugger(int degree, int level) {
        if (degree > 0) {
            program.setProgramViewToExpanded();
        }
        else {
            program.setProgramViewToOriginal();
        }
        return engine.runProgramExecutorDebugger(level);
    }

    public int getCurrentInstructionIndex(){
        return engine.getCurrentInsructionIndex();
    }

    public void resetMaps(){
        program.resetMapVariables();
    }

    public int getCycles(){
       return engine.getSumOfCycles();
    }

    public String getProgramName(){
        return program.getProgramName();
    }

    // בהמשך נוסיף כאן:
    // run(RunParams p) → RunResult
    // expand(int degree) → ExpandResult
    // history(), variables() וכו'
}
