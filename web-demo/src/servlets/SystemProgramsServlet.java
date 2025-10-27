package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.dto.SystemFunctionDTO;
import users.SystemProgramsManager;
import utils.ServletsUtills;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/get-system-programs", "/get-system-functions-dtos"})
public class SystemProgramsServlet extends HttpServlet {

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
        List<SystemProgramsManager> systemPrograms = ServletsUtills.getAllSystemPrograms(getServletContext());
        System.out.println("System programs loaded: " + systemPrograms.size());

        result.put("status", "ok");
        result.put("count", systemPrograms.size());
        result.put("programs", systemPrograms);
        response.setStatus(HttpServletResponse.SC_OK);
        writeJsonResponse(response, result);
    }

    // === שליפת פונקציות מערכת ===
    private void handleGetSystemFunctions(HttpServletResponse response, Map<String, Object> result) throws IOException {
        List<SystemFunctionDTO> systemFunctions = ServletsUtills.getAllSystemFunctions(getServletContext());
        System.out.println("System functions loaded: " + systemFunctions.size());

        result.put("status", "ok");
        result.put("count", systemFunctions.size());
        result.put("functions", systemFunctions);
        response.setStatus(HttpServletResponse.SC_OK);
        writeJsonResponse(response, result);
    }

    private void writeJsonResponse(HttpServletResponse response, Map<String, Object> result) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
}
