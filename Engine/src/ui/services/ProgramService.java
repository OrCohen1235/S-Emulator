package ui.services;

import logic.dto.EngineDTO;
import logic.dto.InstructionDTO;
import logic.dto.ProgramDTO;
import program.ProgramLoadException;
import ui.model.VarRow;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


public class ProgramService {
    private EngineDTO engine;
    private ProgramDTO program;
    private HistoryService history;


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

    public List<VarRow> getVariables() {
        List<VarRow> rows = new ArrayList<>();
        for (String var : program.getXVariables()) {
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

    public List<String> getAllVarsAndLables(){
        List<String> vars = new ArrayList<>();
        vars.addAll(program.getAllVariables());
        vars = sortListFromYtoZ(vars);
        vars.addAll(program.getLabels());
        return vars;
    }

    public List<VarRow> getAllVars() {
        List<VarRow> rows = new ArrayList<>();
        List<String> vars = program.getAllVariables();
        for (String var : sortListFromYtoZ(vars)) {
            switch (var.charAt(0)) {
                case 'X':
                    rows.add(new VarRow(var, "INPUT", program.getVarValue(var)));
                    break;
                case 'Y':
                    rows.add(new VarRow(var, "OUTPUT", program.getVarValue(var)));
                    break;
                case 'Z':
                    rows.add(new VarRow(var, "WORK", program.getVarValue(var)));
                    break;
            }
        }

        return rows;
    }


    public List<String> sortListFromYtoZ(List<String> toSort){
        List<String> xVars = new ArrayList<>();
        List<String> zVars = new ArrayList<>();

        for (String var : toSort) {
            switch (var.charAt(0)) {
                case 'X':
                    xVars.add(var);
                    break;
                case 'Y':
                    break;
                case 'Z':
                    zVars.add(var);
                    break;
            }
        }
        sortXNumerically(xVars);
        sortXNumerically(zVars);
        xVars.addFirst("Y");
        xVars.addAll(zVars);
        return xVars;

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

        long executeOutput = engine.runProgramExecutor(degree);

        history.addHistory(executeOutput, degree, engine.getSumOfCycles(), getVariablesEND());

        return executeOutput;
    }

    public long executeProgramDebugger(int degree, int level) {
        if (degree > 0) {
            program.setProgramViewToExpanded();
        } else {
            program.setProgramViewToOriginal();
        }

        long executeOutput = engine.runProgramExecutorDebugger(level);

        if(isFinishedDebugging()){
            history.addHistory(executeOutput, degree, engine.getSumOfCycles(), getVariablesEND());
        }
        return executeOutput;
    }

    public void addHistory(int degree,long y){
        history.addHistory(y, degree, engine.getSumOfCycles(), getVariablesEND());
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

    public void resetCycles(){
        engine.resetSumOfCycles();
    }

    public int getCycles() {
        return engine.getSumOfCycles();
    }

    public int getCyclesDebugger() {
        return engine.getSumOfCyclesDebugger();
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

    public void setHistory(HistoryService history) {
        this.history = history;
    }


    public static void sortXNumerically(List<String> items) {
        items.sort(Comparator.comparingInt(s -> Integer.parseInt(s.substring(1))));
    }

}
