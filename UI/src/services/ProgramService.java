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
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import logic.dto.EngineDTO;
import logic.dto.InstructionDTO;
import logic.dto.ProgramDTO;
import program.ProgramLoadException;
import model.VarRow;

import java.util.List;

import static services.Constants.*;


public class ProgramService {
    private HistoryService history;

    public void startProgram(String programName) {
        try {
            String url = SERVER_URL + "start-program";
            String jsonBody = GSON.toJson(programName);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).
                    header("Content-Type", "application/json").
                    POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ProgramService.Response resp = GSON.fromJson(response.body(), ProgramService.Response.class);
            } else {
                System.err.println("HTTP error: " + response.statusCode() + "from: executProgram");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    //public final static Gson gson = new Gson();
    //private final HttpClient httpClient = HttpClient.newHttpClient();

    private static class UploadResponse {
        String status;
        String error;
        String message;
        String programName;
        int maxDegree;
        int instructionCount;
        String fileName;
    }

    public void loadXml(Path xmlPath) throws ProgramLoadException {
        try {
            String url = SERVER_URL + "load-file";
            String boundary = "----JavaBoundary" + UUID.randomUUID();
            String CRLF = "\r\n";

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

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                if (res.statusCode() == 409) {
                    throw new IllegalStateException("Program is already loaded");
                }
                throw new ProgramLoadException("HTTP " + res.statusCode() + ": " + res.body());
            }


            UploadResponse r = GSON.fromJson(res.body(), UploadResponse.class);

            // בדיקת שגיאה
            if (r.error != null && !r.error.isBlank()) {
                throw new ProgramLoadException("Server error: " + r.error);
            }

            // בדיקת סטטוס
            if (r.status == null || !r.status.equalsIgnoreCase("ok")) {
                throw new ProgramLoadException("Unexpected status: " + r.status);
            }

            // הצלחה!
            System.out.println("✅ Program uploaded successfully: " + r.programName);
            System.out.println("   Max Degree: " + r.maxDegree);
            System.out.println("   Instructions: " + r.instructionCount);

        } catch (IOException | InterruptedException e) {
            throw new ProgramLoadException("Failed to send HTTP request", e);
        }
    }


    public int getMaxDegree() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "get-max-degree")).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200){
                JsonObject root = GSON.fromJson(response.body(), JsonObject.class);
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)){
                    return ((Number) resp.data).intValue();
                } else {
                    throw new ProgramLoadException("Server error: " + resp.status + " from: getMaxDegree");
                }
            }  else {
                throw new ProgramLoadException("HTTP error: " + response.statusCode() + " from: getMaxDegree");
            }
        } catch (IOException | InterruptedException e) {
            throw new ProgramLoadException("Failed to get max degree", e);
        }
    }

    public void loadExpansionByDegree(int degree) {
        try{
            String url = SERVER_URL + "load-expansion-by-degree";
            String jsonBody = GSON.toJson(degree);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).
                    header("Content-Type", "application/json").
                    POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                System.err.println("Server error: " + response.statusCode() + " from: loadExpansionByDegree");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public boolean hasProgram() { return program != null; }


    public List<InstructionDTO> getExpansionFor(InstructionDTO parent) {
        try {
            String url = SERVER_URL + "get-expansion-for?displayindex=" + parent.getDisplayIndex();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200){
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    String dataJson = GSON.toJson(resp.data);
                    return GSON.fromJson(dataJson, new TypeToken<List<InstructionDTO>>(){}.getType());
                } else {
                    System.err.println("Server error: " + resp.error + " from: getExpansionFor");
                    return List.of();
                }
            } else {
                System.err.println("Server returned error: " + response.statusCode());
                return List.of();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<InstructionDTO> getInstructionsDTO() {
        try {
            String url = SERVER_URL + "get-original-instructions-dtos";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    String dataJson = GSON.toJson(resp.data);
                    return GSON.fromJson(dataJson, new TypeToken<List<InstructionDTO>>(){}.getType());
                } else {
                    System.err.println("Server error: " + resp.error + " from: getInstructionsDTO");
                    return List.of();
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: getInstructionsDTO");
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<VarRow> getInputsVars() {
        try {
            String url = SERVER_URL + "get-inputs-vars";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    String dataJson = GSON.toJson(resp.data);
                    List<String> inputs = GSON.fromJson(dataJson, new TypeToken<List<String>>(){}.getType());

                    sortXNumerically(inputs);
                    Map<String, String> inputsVarsValues = getInputsVarsValues();
                    List<VarRow> rows = new ArrayList<>();
                    for (String var : inputs) {
                        String value = inputsVarsValues.getOrDefault(var, "0");
                        rows.add(new VarRow(var.toUpperCase(), "INPUT", value));
                    }
                    return rows;

                } else {
                    System.err.println("Server error: " + resp.error + " from: getInputsVars");
                    return List.of();
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: getInputsVars");
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }


    private Map<String, String> getInputsVarsValues() {
        try {
            String url = SERVER_URL + "get-inputs-vars-values";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> response = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    String dataJson = GSON.toJson(resp.data);
                    return GSON.fromJson(dataJson, new TypeToken<Map<String, String>>(){}.getType());
                } else {
                    System.err.println("Server error: " + resp.error + " from: getInputsVarsValues");
                    return Map.of();
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: getInputsVarsValues");
                return Map.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public List<VarRow> getVarsAtEndRun() {
        List<VarRow> rows = new ArrayList<>();
        Map<String, Long> values = getVariablesValues();

        for (Map.Entry<String, Long> entry : values.entrySet()) {
            String name = entry.getKey();
            String varName = name.toUpperCase();
            String type;

            if (!name.isEmpty() && Character.toLowerCase(name.charAt(0)) == 'x') {
                type = "INPUT";
            } else if (Character.toLowerCase(name.charAt(0)) == 'y') {
                type = "OUTPUT";
            } else {
                type = "WORK";
            }

            String valueStr = String.valueOf(entry.getValue());
            rows.add(new VarRow(varName, type, valueStr));

        }
        return rows;
    }

    private Map<String, Long> getVariablesValues() {
        try {
            String url = SERVER_URL + "get-variables-values";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    String dataJson = GSON.toJson(resp.data);
                    return GSON.fromJson(dataJson, new TypeToken<Map<String, Long>>(){}.getType());
                } else {
                    System.err.println("Server error: " + resp.error + " from: getVariables-values");
                    return Map.of();
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: getInputsVarsValues");
                return Map.of();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Map.of();
        }
    }


    public List<String> getAllVarsAndLables(){
        List<String> vars = new ArrayList<>();
        vars.addAll(getAllVariablesOrLabels("get-all-variables"));
        vars = sortListFromYtoZ(vars);
        vars.addAll(getAllVariablesOrLabels("get-all-labels"));
        return vars;
    }

    private List<String> getAllVariablesOrLabels(String str) {
        try {
            String url = SERVER_URL + str;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    String dataJson = GSON.toJson(resp.data);
                    return GSON.fromJson(dataJson, new TypeToken<List<String>>() {
                    }.getType());
                } else {
                    System.err.println("Server error: " + resp.error +  " from: getAllVariablesOrLabels");
                    return List.of();
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: getAllVariablesOrLabels");
                return List.of();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }


    public List<VarRow> getAllVarsSorted() {
        List<VarRow> rows = new ArrayList<>();
        List<String> vars = getAllVariablesOrLabels("get-all-variables");
        Map<String, Long> values = getVariablesValues();
        for (String var : sortListFromYtoZ(vars)) {
            switch (var.charAt(0)) {
                case 'X':
                    rows.add(new VarRow(var, "INPUT", values.get(var).toString()));
                    break;
                case 'Y':
                    rows.add(new VarRow(var, "OUTPUT", values.get(var).toString()));
                    break;
                case 'Z':
                    rows.add(new VarRow(var, "WORK", values.get(var).toString()));
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
        try{
            String url = SERVER_URL + "load-input-vars";
            String jsonBody = GSON.toJson(vars);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).
                    header("Content-Type", "application/json").
                    POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    history.createHistory(vars);
                } else {
                    System.err.println("Server error: " + resp.error + "from: loadVars");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: loadVars");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public long executeProgram(int degree) {
        long executeOutput = 0;

        try {
            String url = SERVER_URL + "execute-program?degree=" + degree;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ExecuteProgramResponse resp = GSON.fromJson(response.body(), ExecuteProgramResponse.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    executeOutput = resp.result;
                    history.addHistory(resp.programName,executeOutput, degree, resp.cycles, getVarsAtEndRun());
                } else {
                    return -1;
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: executeProgram");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return executeOutput;
    }

    public long executeProgramDebugger(int degree, int level) {
        long executeOutput = 0;

        try {
            String url = SERVER_URL + "execute-program-debugger?degree=" + degree + "&level=" + level;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ExecuteProgramResponse resp = GSON.fromJson(response.body(), ExecuteProgramResponse.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    executeOutput = resp.result;
                    if (isFinishedDebugging()){
                        history.addHistory(resp.programName,executeOutput, degree, resp.cycles, getVarsAtEndRun());
                    }
                } else {
                    return -1;
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: executeProgramDebugger");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return executeOutput;
    }

    public void addHistory(int degree, long y){
        try {
            String url = SERVER_URL + "get-program-name-and-cycles";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ExecuteProgramResponse resp = GSON.fromJson(response.body(), ExecuteProgramResponse.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    history.addHistory(resp.programName,y, degree, resp.cycles, getVarsAtEndRun());

                } else {
                    System.err.println("Server error: " + resp.error + " from: addHistory");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: addHistory");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getCurrentInstructionIndex() {
        int index = 0;

        try {
            String url = SERVER_URL + "get-instruction-index";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    index = ((Number) resp.data).intValue();
                } else {
                    System.err.println("Server error: " + resp.error + " from: getCurrentInstructionIndex");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: getCurrentInstructionIndex");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return index;
    }

    public void resetMaps() {
        try {
            String url = SERVER_URL + "reset-maps";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody()).build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if (!("ok".equalsIgnoreCase(resp.status))) {
                    System.err.println("Server error: " + resp.error + " from: resetMaps");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: resetMaps");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resetCycles() {
        try {
            String url = SERVER_URL + "reset-cycles";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody()).build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if (!("ok".equalsIgnoreCase(resp.status))) {
                    System.err.println("Server error: " + resp.error + " from: resetCycles");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: resetMaps");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getCycles() {
        int cycles = 0;

        try {
            String url = SERVER_URL + "get-cycles";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    cycles = ((Number) resp.data).intValue();
                } else {
                    System.err.println("Server error: " + resp.error + " from: getCycles");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: getCycles");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return cycles;
    }

    public String getProgramName() {
        String programName = "";

        try {
            String url = SERVER_URL + "get-program-name";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    programName = resp.data.toString();
                } else {
                    System.err.println("Server error: " + resp.error + " from: getProgramName");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: getProgramName");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return programName;
    }

    public Boolean isFinishedDebugging() {
        Boolean isFinishedDebugging = false;

        try {
            String url = SERVER_URL + "is-finished-debugging";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    isFinishedDebugging = Boolean.parseBoolean(resp.data.toString());
                } else {
                    System.err.println("Server error: " + resp.error +  " from: isFinishedDebugging");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: isFinishedDebugging");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return isFinishedDebugging;
    }

    public void resetDebugger() {
        try {
            String url = SERVER_URL + "reset-debugger";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody()).build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if (!("ok".equalsIgnoreCase(resp.status))) {
                    System.err.println("Server error: " + resp.error +  " from: resetDebugger");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + " from: resetDebugger");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setHistory(HistoryService history) {
        this.history = history;
    }

    public static void sortXNumerically(List<String> items) {
        items.sort(Comparator.comparingInt(s -> Integer.parseInt(s.substring(1))));
    }

    public List<String> getFunctionsNames() {
        try {
            String url = SERVER_URL + "get-functions-names";
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if ("ok".equalsIgnoreCase(resp.status)) {
                    String dataJson = GSON.toJson(resp.data);
                    return GSON.fromJson(dataJson, new TypeToken<List<String>>(){}.getType());
                } else {
                    System.err.println("Server error: " + resp.error +  " from: getFunctionsNames");
                    return List.of();
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + "from: getFunctionsNames");
                return List.of();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void switchToFunction(String functionName) {
        try{
            String url = SERVER_URL + "switch-to-function";
            String jsonBody = GSON.toJson(functionName);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).
                    header("Content-Type", "application/json").
                    POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Response resp = GSON.fromJson(response.body(), Response.class);

                if (!("ok".equalsIgnoreCase(resp.status))) {
                    System.err.println("Server error: " + resp.error + " from: switchToFunction");
                }
            } else {
                System.err.println("HTTP error: " + response.statusCode() + "from: switchToFunction");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class Response {
        String status;
        String error;
        Object data;
        String message;
    }

    private static class ExecuteProgramResponse {
        String status;
        String error;
        long result;
        String programName;
        int cycles;
    }
}