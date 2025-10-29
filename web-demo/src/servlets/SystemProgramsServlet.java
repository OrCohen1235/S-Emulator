package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.dto.SystemFunctionDTO;
import logic.dto.SystemProgramDTO; // ✅ הוספה
import session.UserSession;
import users.SystemProgram;
import users.UserManager;
import utils.ServletsUtills;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet(urlPatterns = {
        "/get-system-programs",
        "/get-system-functions-dtos",
        "/update-program-stats",
        "/add-run-when-finish-debugging"
})
public class SystemProgramsServlet extends BaseServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String path = request.getServletPath();

        Map<String, Object> result = new HashMap<>();

        try {
            if ("/get-system-programs".equals(path)) {
                handleGetSystemPrograms(response, result);
            } else if ("/get-system-functions-dtos".equals(path)) {
                handleGetSystemFunctions(response, result);
            }
            else if ("/add-run-when-finish-debugging".equals(path)) {
                handleAddRunWhenFinishDebugging(request,response);
            }
                else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("status", "error");
                result.put("message", "Unknown endpoint: " + path);
                writeJsonResponse(response, result);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", e.getMessage());
            writeJsonResponse(response, result);
        }
    }

    private void handleAddRunWhenFinishDebugging(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {

            UserSession userSession = getUserSession(request);
            String programName = userSession.getCurrentEngine().getProgramDTO().getProgramName();

            SystemProgram systemProgram = ServletsUtills.getSystemProgram(getServletContext(), programName);

            if (systemProgram == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Program not found: " + programName);
                return;
            }
            UserManager userManager = getUserManager();
            // עדכון הרצה ללא עלות (debugging)
            systemProgram.addRun(0);
            userManager.getUser(getUserSession(request).getUsername()).incrementRunsCount();
            System.out.println("[FinishDebugging] ✓ Added debug run for program: " + programName +
                    " | Total runs: " + systemProgram.getRunCount());

            // החזרת ok
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("ok");

        } catch (NoSuchElementException e) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Program not found: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to update program stats: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String path = request.getServletPath();

        Map<String, Object> result = new HashMap<>();

        try {
            if ("/update-program-stats".equals(path)) {
                handleUpdateProgramStats(request, response, result);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("status", "error");
                result.put("message", "Unknown endpoint: " + path);
                writeJsonResponse(response, result);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", e.getMessage());
            writeJsonResponse(response, result);
        }
    }

    // === שליפת תוכניות מערכת ===
    private void handleGetSystemPrograms(HttpServletResponse response, Map<String, Object> result) throws IOException {
        List<SystemProgram> systemPrograms = ServletsUtills.getAllSystemPrograms(getServletContext());

        // ✅ המרה ל-DTOs
        List<SystemProgramDTO> programDTOs = new ArrayList<>();
        for (SystemProgram program : systemPrograms) {
            programDTOs.add(new SystemProgramDTO(program));
        }

        System.out.println("[SystemProgramsServlet] System programs loaded: " + programDTOs.size());

        result.put("status", "ok");
        result.put("count", programDTOs.size());
        result.put("programs", programDTOs); // ✅ מחזיר DTOs

        response.setStatus(HttpServletResponse.SC_OK);
        writeJsonResponse(response, result);
    }

    // === שליפת פונקציות מערכת ===
    private void handleGetSystemFunctions(HttpServletResponse response, Map<String, Object> result) throws IOException {
        List<SystemFunctionDTO> systemFunctions = ServletsUtills.getAllSystemFunctions(getServletContext());

        result.put("status", "ok");
        result.put("count", systemFunctions.size());
        result.put("functions", systemFunctions);

        response.setStatus(HttpServletResponse.SC_OK);
        writeJsonResponse(response, result);
    }

    // ✅ עדכון סטטיסטיקות הרצה של תוכנית
    private void handleUpdateProgramStats(HttpServletRequest request, HttpServletResponse response, Map<String, Object> result) throws IOException {
        // קריאת פרמטרים מה-request
        String programName = request.getParameter("programName");
        String creditsStr = request.getParameter("credits");

        // ולידציה
        if (programName == null || programName.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", "Missing required parameter: programName");
            writeJsonResponse(response, result);
            return;
        }

        if (creditsStr == null || creditsStr.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", "Missing required parameter: credits");
            writeJsonResponse(response, result);
            return;
        }

        double credits;
        try {
            credits = Double.parseDouble(creditsStr);
            if (credits < 0) {
                throw new NumberFormatException("Credits must be non-negative");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", "Invalid credits value: " + creditsStr);
            writeJsonResponse(response, result);
            return;
        }

        // עדכון הסטטיסטיקות
        try {
            SystemProgram program = ServletsUtills.getSystemProgram(getServletContext(), programName);

            if (program == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("status", "error");
                result.put("message", "Program not found: " + programName);
                writeJsonResponse(response, result);
                return;
            }

            // עדכון הסטטיסטיקות
            program.addRun(credits);

            System.out.println("[SystemProgramsServlet] Updated stats for program: " + programName +
                    " | Run #" + program.getRunCount() +
                    " | Cost: " + credits +
                    " | Avg: " + String.format("%.2f", program.getAverageCost()));

            // ✅ יצירת DTO מהתוכנית המעודכנת
            SystemProgramDTO programDTO = new SystemProgramDTO(program);

            // החזרת התוצאה
            result.put("status", "ok");
            result.put("message", "Program statistics updated successfully");
            result.put("program", programDTO); // ✅ מחזיר את ה-DTO המלא

            response.setStatus(HttpServletResponse.SC_OK);
            writeJsonResponse(response, result);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("status", "error");
            result.put("message", "Failed to update program statistics: " + e.getMessage());
            writeJsonResponse(response, result);
        }
    }



    private void writeJsonResponse(HttpServletResponse response, Map<String, Object> result) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
}