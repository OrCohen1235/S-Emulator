package servlets;

import com.google.gson.Gson;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.dto.InstructionDTO;
import utils.ServletsUtills;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.ServletsUtills.getEngineManager;


@WebServlet(urlPatterns = {"/load-expansion-by-degree"})
public class ExpandServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json; charset=UTF-8");
        String path = req.getServletPath();

        if ("/get-max-degree".equals(path)) {
            handleGetMaxDegree(req, res);
        } else {
            sendJson(res, HttpServletResponse.SC_NOT_FOUND, Map.of(
                    "status", "error",
                    "message", "Unknown GET path"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json; charset=UTF-8");
        String path = req.getServletPath();

        if ("/load-expansion-by-degree".equals(path)) {
            handleLoadExpansionByDegree(req, res);
        } else {
            sendJson(res, HttpServletResponse.SC_NOT_FOUND, Map.of(
                    "status", "error",
                    "message", "Unknown POST path"
            ));
        }
    }

    private void handleGetMaxDegree(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Engine engine = getEngineManager(getServletContext(), null);
            if (engine == null || engine.getProgramDTO() == null) {
                sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of(
                        "status", "error",
                        "message", "Engine or ProgramDTO not initialized"
                ));
                return;
            }

            engine.getProgramDTO().loadExpansion(); // במידת הצורך לטעון לפני חישוב
            int maxDegree = engine.getProgramDTO().getMaxDegree();

            sendJson(response, HttpServletResponse.SC_OK, Map.of(
                    "status", "ok",
                    "data", maxDegree
            ));
        } catch (Exception e) {
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    private void handleLoadExpansionByDegree(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            Integer currentDegree;
            try (BufferedReader br = request.getReader()) {
                currentDegree = gson.fromJson(br, Integer.class);
            }

            if (currentDegree == null) {
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, Map.of(
                        "status", "error",
                        "message", "Body must be a plain integer (e.g. 2) or a JSON number"
                ));
                return;
            }

            Engine engine = getEngineManager(getServletContext(), null);
            if (engine == null || engine.getProgramDTO() == null) {
                sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of(
                        "status", "error",
                        "message", "Engine or ProgramDTO not initialized"
                ));
                return;
            }

            if (currentDegree > 0) {
                engine.getProgramDTO().setProgramViewToExpanded();
            } else {
                engine.getProgramDTO().setProgramViewToOriginal();
            }

            engine.getProgramDTO().loadExpansionByDegree(currentDegree);

            sendJson(response, HttpServletResponse.SC_OK, Map.of(
                    "status", "ok",
                    "degreeLoaded", currentDegree
            ));
        } catch (Exception e) {
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } finally {
            // RW_LOCK.writeLock().unlock();
        }
    }

    private void sendJson(HttpServletResponse response, int status, Map<String, ?> data) throws IOException {
        response.setStatus(status);
        response.getWriter().write(gson.toJson(data));
    }

}
