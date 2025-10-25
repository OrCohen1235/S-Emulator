package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.SystemProgramsManager;
import utils.ServletsUtills;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/get-system-programs"})
public class SystemProgramsServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        try {
            List<SystemProgramsManager> systemPrograms = ServletsUtills.getAllSystemPrograms(getServletContext());
            for (SystemProgramsManager systemProgram : systemPrograms) {
                System.out.println(systemProgram.getName());
            }
            System.out.println("System programs loaded: " + systemPrograms.size());

            result.put("status", "ok");
            result.put("count", systemPrograms.size());
//            result.put("programs", systemPrograms);

            response.setStatus(HttpServletResponse.SC_OK);
            writeJsonResponse(response, result);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("message", e.getMessage());
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
