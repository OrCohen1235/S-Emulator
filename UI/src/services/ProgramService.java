package services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import logic.dto.EngineDTO;
import logic.dto.InstructionDTO;
import logic.dto.ProgramDTO;
import program.ProgramLoadException;
import model.VarRow;

import java.io.File;
import java.util.List;


public class ProgramService {
    private EngineDTO engine;
    private ProgramDTO program;
    private HistoryService history;


    public void loadXml(Path xmlPath) throws ProgramLoadException {
        try {
            String url = "http://localhost:8080/web_demo_Web/load-file";
            String boundary = "----JavaBoundary" + UUID.randomUUID();
            String CRLF = "\r\n";

            // גוף ה-multipart
            StringBuilder sb = new StringBuilder();
            sb.append("--").append(boundary).append(CRLF);
            sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(xmlPath.getFileName()).append("\"").append(CRLF);
            sb.append("Content-Type: application/xml").append(CRLF).append(CRLF);

            byte[] head = sb.toString().getBytes();
            byte[] fileBytes = Files.readAllBytes(xmlPath);
            byte[] tail = (CRLF + "--" + boundary + "--" + CRLF).getBytes();
            byte[] body = new byte[head.length + fileBytes.length + tail.length];
            System.arraycopy(head, 0, body, 0, head.length);
            System.arraycopy(fileBytes, 0, body, head.length, fileBytes.length);
            System.arraycopy(tail, 0, body, head.length + fileBytes.length, tail.length);

            // שליחת הבקשה
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200)
                throw new ProgramLoadException("HTTP " + res.statusCode() + ": " + res.body());

            // ניתוח התשובה

            Resp r = new Gson().fromJson(res.body(), Resp.class);
            if (r.error != null && !r.error.isBlank())
                throw new ProgramLoadException("Server error: " + r.error);
            if (!"ok".equalsIgnoreCase(r.status))
                throw new ProgramLoadException("Unexpected status: " + r.status);

            System.out.println("Loaded successfully!");

        } catch (IOException | InterruptedException e) {
            throw new ProgramLoadException("Failed to send HTTP request", e);
        }
    }

    public int getMaxDegree() {
        program.loadExpansion();
        return program.getMaxDegree();
    }

    public void loadExpasionByDegree(int degree) {
        if (degree > 0) {
            program.setProgramViewToExpanded();
        } else {
            program.setProgramViewToOriginal();
        }
        program.loadExpansionByDegree(degree);
    }

    public boolean hasProgram() { return program != null; }
    public ProgramDTO getProgram() { return program; }
    public EngineDTO getEngine() { return engine; }

    public List<InstructionDTO> getExpansionFor(InstructionDTO parent) {
        return program.getExpandDTO(parent.getDisplayIndex());
    }

    public List<InstructionDTO> getInstructionsDTO() {
        try {
            String url = "http://localhost:8080/web_demo_Web/instructions";


            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                return gson.fromJson(response.body(), new TypeToken<List<InstructionDTO>>() {}.getType());
            } else {
                System.err.println("Server returned error: " + response.statusCode());
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<VarRow> getInputsVars() {
        List<VarRow> rows = new ArrayList<>();
        List<String> inputs = program.getXVariables();
        sortXNumerically(inputs);
        for (String var : inputs) {
            rows.add(new VarRow(var.toUpperCase(), "INPUT", program.getVarValue(var)));
        }
        return rows;
    }

    public List<VarRow> getVarsAtEndRun() {
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

    public List<VarRow> getAllVarsSorted() {
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
        program.loadInputVars(vars);

        history.createHistory(vars);
    }

    public long executeProgram(int degree) {
        if (degree > 0) {
            program.setProgramViewToExpanded();
        } else {
            program.setProgramViewToOriginal();
        }

        long executeOutput = program.runProgramExecutor(degree);

        history.addHistory(program.getProgram(),executeOutput, degree, program.getSumOfCycles(), getVarsAtEndRun());

        return executeOutput;
    }

    public long executeProgramDebugger(int degree, int level) {
        if (degree > 0) {
            program.setProgramViewToExpanded();
        } else {
            program.setProgramViewToOriginal();
        }

        long executeOutput = program.runProgramExecutorDebugger(level);

        if(isFinishedDebugging()){
            history.addHistory(program.getProgram(),executeOutput, degree, program.getSumOfCycles(), getVarsAtEndRun());
        }
        return executeOutput;
    }

    public void addHistory(int degree,long y){
        history.addHistory(program.getProgram(),y, degree, program.getSumOfCycles(), getVarsAtEndRun());
    }


    public int getCurrentInstructionIndex() {
        return program.getCurrentInstructionIndex();
    }

    public Map<String, Long> getVariablesValues() {
        return program.getVariablesValues();
    }


    public void resetMaps() {
        program.resetMapVariables();
    }

    public void resetCycles(){
        program.resetSumOfCycles();
    }

    public int getCycles() {
        return program.getSumOfCycles();
    }

    public int getCyclesDebugger() {
        return program.getSumOfCyclesDebugger();
    }

    public String getProgramName() {
        return program.getProgramName();
    }

    public Boolean isFinishedDebugging() {
        return program.isFinishedDebugging();
    }

    public void resetDebugger() {
        program.resetDebugger();
    }

    public void setHistory(HistoryService history) {
        this.history = history;
    }
    
    public static void sortXNumerically(List<String> items) {
        items.sort(Comparator.comparingInt(s -> Integer.parseInt(s.substring(1))));
    }

    public List<String> getFunctionsNames() {
        return program.getFunctionsNames();
    }

    public void switchToFunction(String functionName) {
        program.switchToFunction(functionName);
    }

    private static class Resp {
        String status;
        String error;
    }
}


