package ui.services;

import logic.dto.EngineDTO;
import logic.dto.InstructionDTO;
import logic.dto.ProgramDTO;
import program.ProgramLoadException;
import ui.model.VarRow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProgramService {
    private EngineDTO engine;
    private ProgramDTO program;

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

    public void loadExpasionByDegree(int degree) {
        if (degree > 0) {
            program.setProgramViewToExpanded();
        } else {
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

    public List<InstructionDTO> getInstructionsDTO()
    {
        return program.getInstructionDTOs();
    }

    /** משתנים בתחילת הריצה (קלטים), בתצוגת טבלה. */
    public List<VarRow> getVariables() {
        List<VarRow> rows = new ArrayList<>();
        for (String var : program.getVariables()) {
            rows.add(new VarRow(var.toUpperCase(), "INPUT", program.getVarValue(var)));
        }
        return rows;
    }

    public List<VarRow> getVariablesEND() {
        List<VarRow> rows = new ArrayList<>();
        Map<String, Long> values = program.getVariablesValues();
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            String name = entry.getKey();
            String varName = name.toUpperCase();
            String type;
            if (!name.isEmpty() && Character.toLowerCase(name.charAt(0)) == 'x') {
                type = "INPUT";
            } else if (Character.toLowerCase(name.charAt(0)) == 'y')
                type = "OUTPUT";
            else
                type = "WORK";


            String valueStr = String.valueOf(entry.getValue());
            rows.add(new VarRow(varName, type, valueStr));
        }

        return rows;
    }

    public void loadVars(List<Long> vars) {
        engine.loadInputVars(vars);
    }

    public long executeProgram(int degree) {
        if (degree > 0) {
            program.setProgramViewToExpanded();
        } else {
            program.setProgramViewToOriginal();
        }
        return engine.runProgramExecutor(degree);
    }

    public long executeProgramDebugger(int degree, int level) {
        if (degree > 0) {
            program.setProgramViewToExpanded();
        } else {
            program.setProgramViewToOriginal();
        }
        return engine.runProgramExecutorDebugger(level);
    }

    public int getCurrentInstructionIndex() {
        return engine.getCurrentInsructionIndex();
    }

    public Map<String, Long> getVariablesValues() {
        return program.getVariablesValues();
    }


    public void resetMaps() {
        program.resetMapVariables();
    }

    public int getCycles() {
        return engine.getSumOfCycles();
    }

    public String getProgramName() {
        return program.getProgramName();
    }

    public Boolean isFinishedDebugging() {
        return engine.isFinishedDebugging();
    }

    public void resetDebugger() {
        engine.resetDebugger();
    }

    // בהמשך נוסיף כאן:
    // run(RunParams p) → RunResult
    // expand(int degree) → ExpandResult
    // history(), variables() וכו'
}
