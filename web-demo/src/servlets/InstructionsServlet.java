package servlets;

import com.google.gson.Gson;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.dto.InstructionDTO;
import utils.ServletsUtills;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/get-original-instructions-dtos"})
public class InstructionsServlet extends BaseServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        Map<String, Object> result = new HashMap<>();

        try {
            // שליפת ה־engine
            Engine engine = getUserSession(request).getCurrentEngine();
            if (engine == null || engine.getProgramDTO() == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.put("status", "error");
                result.put("data", "Engine or ProgramDTO not initialized");
                writeJsonResponse(response, result);
                return;
            }

            // טוען את ההרחבה
            engine.getProgramDTO().loadExpansion();

            // שליפת רשימת ההוראות
            List<InstructionDTO> dtos = engine.getProgramDTO().getInstructionDTOs();

            // בניית תשובת JSON עקבית
            result.put("status", "ok");
            result.put("data", dtos);

            response.setStatus(HttpServletResponse.SC_OK);
            writeJsonResponse(response, result);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("status", "error");
            result.put("data", e.getMessage());
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
