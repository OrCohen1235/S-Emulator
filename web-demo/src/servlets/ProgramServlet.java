package servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.dto.InstructionDTO;
import utils.ServletsUtills;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@WebServlet(urlPatterns = {
        "/get-inputs-vars",
        "/get-inputs-vars-values",
        "/get-variables-values",
        "/get-all-variables",
        "/get-all-labels",
        "/execute-program",
        "/load-input-vars",
        "/reset-maps",
        "/execute-program-debugger",
        "/get-program-name-and-cycles",
        "/get-instruction-index",
        "/reset-cycles",
        "/get-cycles",
        "/get-program-name",
        "/is-finished-debugging",
        "/reset-debugger",
        "/get-functions-names",
        "/switch-to-function",
        "/get-expansion-for",
        "/get-max-degree"


})
public class ProgramServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private static final ReentrantReadWriteLock RW_LOCK = new ReentrantReadWriteLock();

    private static final String P_GET_INPUT_VARS        = "/get-inputs-vars";
    private static final String P_GET_INPUT_VARS_VALUES = "/get-inputs-vars-values";
    private static final String P_GET_VARIABLES_VALUES  = "/get-variables-values";
    private static final String P_GET_ALL_VARIABLES     = "/get-all-variables";
    private static final String P_GET_ALL_LABELS        = "/get-all-labels";
    private static final String P_EXECUTE_PROGRAM       = "/execute-program";
    private static final String P_LOAD_INPUT_VARS       = "/load-input-vars";
    private static final String P_RESET_MAPS            = "/reset-maps";
    private static final String P_EXECUTE_PROGRAM_DEBUGGER = "/execute-program-debugger";
    private static final String P_GET_PROGRAM_NAME_AND_CYCLES      = "/get-program-name-and-cycles";
    private static final String P_GET_INSTRUCTION_INDEX      = "/get-instruction-index";
    private static final String P_RESET_CYCLES              = "/reset-cycles";
    private static final String P_GET_CYCLES                 = "/get-cycles";
    private static final String P_GET_PROGRAM_NAME          = "/get-program-name";
    private static final String P_IS_FINISHED_DEBUGGING              = "/is-finished-debugging";
    private static final String P_RESET_DEBUGGER                = "/reset-debugger";
    private static final String P_GET_FUNCTIONS_NAMES          = "/get-functions-names";
    private static final String P_SWITCH_TO_FUNCTION          = "/switch-to-function";
    private static final String P_GET_EXPANSION_FOR_INSTRUCTION = "/get-expansion-for";
    private static final String P_GET_MAX_DEGREE              = "/get-max-degree";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        final String path = req.getServletPath();
        switch (path) {
            case P_GET_INPUT_VARS        -> getInputVars(resp);
            case P_GET_INPUT_VARS_VALUES -> getInputVarsValues(resp);
            case P_GET_VARIABLES_VALUES  -> getVariablesValues(resp);
            case P_GET_ALL_VARIABLES     -> getAllVariables(resp);
            case P_GET_ALL_LABELS        -> getAllLabels(resp);
            case P_EXECUTE_PROGRAM       -> executeProgram(req, resp);
            case P_EXECUTE_PROGRAM_DEBUGGER -> executeProgramDebugger(req,resp);
            case P_GET_PROGRAM_NAME_AND_CYCLES -> getProgramNameAndCyclesToHistory(req,resp);
            case P_GET_INSTRUCTION_INDEX -> getInstructionIndex(resp);
            case P_GET_CYCLES -> getCycles(resp);
            case P_GET_PROGRAM_NAME -> getProgramName(resp);
            case P_IS_FINISHED_DEBUGGING -> isFinishedDubugging(resp);
            case P_GET_FUNCTIONS_NAMES -> getFunctionsNames(resp);
            case P_GET_EXPANSION_FOR_INSTRUCTION -> getExpansionByIndex(req,resp);
            case P_GET_MAX_DEGREE -> getMaxDegree(resp);
            default -> writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    new ErrorResp("Unknown GET path"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        final String path = req.getServletPath();
        switch (path) {
            case P_RESET_MAPS      -> resetMaps(resp);
            case P_LOAD_INPUT_VARS -> loadInputVars(req, resp);
            case P_RESET_CYCLES -> resetCycles(resp);
            case P_RESET_DEBUGGER -> resetDebugger(resp);
            case P_SWITCH_TO_FUNCTION -> switchToFunction(req,resp);
            default -> writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    new ErrorResp("Unknown POST path"));
        }
    }

    private void getInstructionIndex(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            int currentIndex = engine.getProgramDTO().getCurrentInstructionIndex();
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(currentIndex));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void getFunctionsNames(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;
            List<String> names = engine.getProgramDTO().getFunctionsNames();
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(names));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void getCycles(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            int cycles = engine.getProgramDTO().getSumOfCycles();
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(cycles));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void isFinishedDubugging(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            boolean isFinishedDebugging = engine.getProgramDTO().isFinishedDebugging();
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(isFinishedDebugging));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void getProgramName(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            String programName = engine.getProgramDTO().getProgramName();
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(programName));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void getMaxDegree(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            int maxDegree = engine.getProgramDTO().getMaxDegree();
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(maxDegree));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }




    /** ---- Helpers ---- */

    private Engine getEngineOrError(HttpServletResponse response) throws IOException {
        Engine engine = ServletsUtills.getEngineManager(getServletContext(), "Divide");
        if (engine == null || engine.getProgramDTO() == null) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorResp("Engine or ProgramDTO not initialized"));
            return null;
        }
        return engine;
    }

    private void getExpansionByIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            String index = request.getParameter("displayindex");
            if (index == null || index.isBlank()) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        Response.error("Missing 'index' query parameter"));
                return;
            }

            int indexInt;
            try {
                indexInt = Integer.parseInt(index);
            } catch (NumberFormatException nfe) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        ProgramServlet.Response.error("'index' must be an integer"));
                return;
            }

            List<InstructionDTO> expandInstruction = engine.getProgramDTO().getExpandDTO(indexInt);

            // מחזיר את הרשימה במבנה {status: "ok", error: null, data: [...]}
            writeJson(response, HttpServletResponse.SC_OK, ProgramServlet.Response.ok(expandInstruction));

        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ProgramServlet.Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(GSON.toJson(body));
    }

    /** ---- GET Handlers ---- */

    private void getInputVars(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            List<String> inputVars = new ArrayList<>(engine.getProgramDTO().getXVariables());
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(inputVars));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void getAllLabels(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            List<String> labels = new ArrayList<>(engine.getProgramDTO().getLabels());
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(labels));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void getAllVariables(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            List<String> variables = new ArrayList<>(engine.getProgramDTO().getAllVariables());
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(variables));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void getVariablesValues(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            Map<String, Long> values = new LinkedHashMap<>(engine.getProgramDTO().getVariablesValues());
            writeJson(response, HttpServletResponse.SC_OK, Response.ok(values));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void getInputVarsValues(HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            List<String> xVariables = engine.getProgramDTO().getXVariables();
            Map<String, Long> values = new LinkedHashMap<>(engine.getProgramDTO().getVariablesValues());
            Map<String, String> newValues = new LinkedHashMap<>();
            for (String xVariable : xVariables) {
                String newXVarString = "X"+xVariable.substring(1);
                long newValue = values.get(newXVarString);
                newValues.put(xVariable, Long.toString(newValue));
            }

            writeJson(response, HttpServletResponse.SC_OK, Response.ok(newValues));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Response.error(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void executeProgram(HttpServletRequest req, HttpServletResponse response) throws IOException {
        RW_LOCK.writeLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            String degreeStr = req.getParameter("degree");
            if (degreeStr == null || degreeStr.isBlank()) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResp("Missing 'degree' query parameter"));
                return;
            }
            int degree = Integer.parseInt(degreeStr);
            if (degree > 0) engine.getProgramDTO().setProgramViewToExpanded();
            else            engine.getProgramDTO().setProgramViewToOriginal();


            long result = engine.getProgramDTO().runProgramExecutor(degree);
            System.out.println(engine.getProgramDTO().getVariablesValues());
            String programName = engine.getProgramDTO().getProgramName();
            int cycles = engine.getProgramDTO().getSumOfCycles();

            writeJson(response, HttpServletResponse.SC_OK, new OkExecute(result, programName, cycles));
        } catch (NumberFormatException nfe) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResp("'degree' must be an integer"));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResp(e.getMessage()));
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }


    private void getProgramNameAndCyclesToHistory(HttpServletRequest req, HttpServletResponse response) throws IOException {
        RW_LOCK.readLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;
            String programName = engine.getProgramDTO().getProgramName();
            int cycles = engine.getProgramDTO().getSumOfCycles();

            writeJson(response, HttpServletResponse.SC_OK, new programNameAndCycles(programName, cycles));
        } catch (NumberFormatException nfe) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResp("'degree' must be an integer"));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResp(e.getMessage()));
        } finally {
            RW_LOCK.readLock().unlock();
        }
    }

    private void executeProgramDebugger(HttpServletRequest req, HttpServletResponse response) throws IOException {
        RW_LOCK.writeLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            String degreeStr = req.getParameter("degree");
            String levelStr  = req.getParameter("level");
            if (degreeStr == null || degreeStr.isBlank()) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResp("Missing 'degree' query parameter"));
                return;
            }
            if (levelStr == null || levelStr.isBlank()) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResp("Missing 'level' query parameter"));
                return;
            }

            int degree = Integer.parseInt(degreeStr);
            int level  = Integer.parseInt(levelStr);

            if (degree > 0) engine.getProgramDTO().setProgramViewToExpanded();
            else            engine.getProgramDTO().setProgramViewToOriginal();

            engine.getProgramDTO().loadExpansionByDegree(degree);
            long result = engine.getProgramDTO().runProgramExecutorDebugger(level);
            String programName = engine.getProgramDTO().getProgramName();
            int cycles = engine.getProgramDTO().getSumOfCycles();

            writeJson(response, HttpServletResponse.SC_OK, new OkExecute(result, programName, cycles));
        } catch (NumberFormatException nfe) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResp("'degree' and 'level' must be integers"));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResp(e.getMessage()));
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }


    /** ---- POST Handlers ---- */

    private void resetMaps(HttpServletResponse response) throws IOException {
        RW_LOCK.writeLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            engine.getProgramDTO().resetMapVariables();
            writeJson(response, HttpServletResponse.SC_OK, new OkOnly());
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResp(e.getMessage()));
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    private void resetCycles(HttpServletResponse response) throws IOException {
        RW_LOCK.writeLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            engine.getProgramDTO().resetSumOfCycles();
            writeJson(response, HttpServletResponse.SC_OK, new OkOnly());
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResp(e.getMessage()));
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    private void resetDebugger(HttpServletResponse response) throws IOException {
        RW_LOCK.writeLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            engine.getProgramDTO().resetDebugger();
            writeJson(response, HttpServletResponse.SC_OK, new OkOnly());
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResp(e.getMessage()));
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    private void loadInputVars(HttpServletRequest req, HttpServletResponse response) throws IOException {
        RW_LOCK.writeLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            // קורא את ה-JSON מה-body: צפוי מערך של מספרים, למשל [10,20,30]
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
            }

            Type listType = new TypeToken<List<Long>>() {}.getType();
            List<Long> values = GSON.fromJson(body.toString(), listType);

            if (values == null) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        new ErrorResp("Request body must contain a JSON array of numbers"));
                return;
            }

            engine.getProgramDTO().loadInputVars(values);

            writeJson(response, HttpServletResponse.SC_OK, new OkLongs(values));
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResp(e.getMessage()));
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    private void switchToFunction(HttpServletRequest req, HttpServletResponse response) throws IOException {
        RW_LOCK.writeLock().lock();
        try {
            Engine engine = getEngineOrError(response);
            if (engine == null) return;

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line; while ((line = reader.readLine()) != null) body.append(line);
            }
            String raw = body.toString().trim();

            String functionName;
            try {
                // אם הגיע JSON: "Foo" או {"name":"Foo"}
                var el = com.google.gson.JsonParser.parseString(raw);
                if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                    functionName = el.getAsString();
                } else if (el.isJsonObject() && el.getAsJsonObject().has("name")) {
                    functionName = el.getAsJsonObject().get("name").getAsString();
                } else {
                    // לא JSON פרימיטיבי/אובייקט, ננסה כטקסט פשוט
                    functionName = raw;
                }
            } catch (Exception ignore) {
                // לא JSON — טקסט פשוט
                functionName = raw;
            }

            if (functionName == null || functionName.isBlank()) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResp("Function name is required in body"));
                return;
            }

            engine.getProgramDTO().switchToFunction(functionName);
            writeJson(response, HttpServletResponse.SC_OK, new OkOnly());
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResp(e.getMessage()));
        } finally {
            RW_LOCK.writeLock().unlock();
        }
    }


    /** ---- Response DTOs ---- */

    static final class Response {
        final String status;
        final String error;
        final Object data;

        private Response(String status, String error, Object data) {
            this.status = status;
            this.error = error;
            this.data = data;
        }

        static Response ok(Object data) {
            return new Response("ok", null, data);
        }

        static Response error(String errorMsg) {
            return new Response("error", errorMsg, null);
        }
    }

    static final class OkOnly {
        final String status = "ok";
    }

    static final class ErrorResp {
        final String status = "error";
        final String message;
        ErrorResp(String message) { this.message = message; }
    }

    /** החזרת רשימת מחרוזות "שטוחה": { "status":"ok", "values":[...] } */
    static final class OkStrings {
        final String status = "ok";
        final List<String> values;
        OkStrings(List<String> values) { this.values = values; }
    }

    /** החזרת רשימת Long "שטוחה": { "status":"ok", "values":[...] } */
    static final class OkLongs {
        final String status = "ok";
        final List<Long> values;
        OkLongs(List<Long> values) { this.values = values; }
    }

    /** החזרת תוצאות ביצוע "שטוחות": { "status":"ok", "result":..., "programname":"...", "cycles":... } */
    static final class OkExecute {
        final String status = "ok";
        final long result;
        final String programName;
        final int cycles;
        OkExecute(long result, String programname, int cycles) {
            this.result = result;
            this.programName = programname;
            this.cycles = cycles;
        }
    }

    static final class programNameAndCycles {
        final String status = "ok";
        final String programName;
        final int cycles;
        programNameAndCycles(String programname, int cycles) {
            this.programName = programname;
            this.cycles = cycles;
        }
    }

    /** החזרת map של שם->ערך בלי עטיפה כללית: { "status":"ok", "variablesValues": { "X1":1, ... } } */
    static final class OkVariablesValues {
        final String status = "ok";
        final Map<String, Long> variablesValues;
        OkVariablesValues(Map<String, Long> variablesValues) { this.variablesValues = variablesValues; }
    }

    /** החזרת map של Xn->string value: { "status":"ok", "inputVarsValues": { "X1":"...", ... } } */
    static final class OkInputVarsValues {
        final String status = "ok";
        final Map<String, String> inputVarsValues;
        OkInputVarsValues(Map<String, String> m) { this.inputVarsValues = m; }
    }
}