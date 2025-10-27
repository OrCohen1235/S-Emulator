// web-demo/src/main/java/servlets/ConnectedUsersServlet.java
package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import users.ConnectedUserMapper;
import users.User;
import users.UserManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "ConnectedUsersServlet", urlPatterns = {
        "/api/connected-users",
        "/api/update-credits",
        "/api/update-main-programs",
        "/api/update-functions",
        "/api/update-credits-used",
        "/api/update-runs"
})
public class ConnectedUsersServlet extends HttpServlet {
    private UserManager userManager;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        userManager = (UserManager) getServletContext().getAttribute("userManager");
        if (userManager == null) throw new ServletException("userManager missing in context");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        var dtoList = ConnectedUserMapper.fromUsers(userManager.getConnectedUsers());
        resp.getWriter().write(gson.toJson(dtoList));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();

        switch (path) {
            case "/api/update-credits":
                handleUpdateCredits(req, resp);
                break;
            case "/api/update-main-programs":
                handleUpdateMainPrograms(req, resp);
                break;
            case "/api/update-functions":
                handleUpdateFunctions(req, resp);
                break;
            case "/api/update-credits-used":
                handleUpdateCreditsUsed(req, resp);
                break;
            case "/api/update-runs":
                handleUpdateRuns(req, resp);
                break;
            default:
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendErrorResponse(resp, "NOT_FOUND", "Endpoint not found");
        }
    }

    private void handleUpdateCredits(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        try {
            JsonObject payload = readJsonPayload(req);

            if (payload == null || !payload.has("username") || !payload.has("credits")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(resp, "INVALID_REQUEST", "Missing username or credits");
                return;
            }

            String username = payload.get("username").getAsString();
            int credits = payload.get("credits").getAsInt();

            User user = findUser(username);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendErrorResponse(resp, "USER_NOT_FOUND", "User not connected: " + username);
                return;
            }

            user.setCreditsCurrent(credits);
            sendSuccessResponse(resp, username, "credits", credits);

        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    private void handleUpdateMainPrograms(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        try {
            JsonObject payload = readJsonPayload(req);

            if (payload == null || !payload.has("username") || !payload.has("mainPrograms")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(resp, "INVALID_REQUEST", "Missing username or mainPrograms");
                return;
            }

            String username = payload.get("username").getAsString();
            int mainPrograms = payload.get("mainPrograms").getAsInt();

            User user = findUser(username);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendErrorResponse(resp, "USER_NOT_FOUND", "User not connected: " + username);
                return;
            }

            //user.setMainProgramsUploaded(mainPrograms);
            sendSuccessResponse(resp, username, "mainPrograms", mainPrograms);

        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    private void handleUpdateFunctions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        try {
            JsonObject payload = readJsonPayload(req);

            if (payload == null || !payload.has("username") || !payload.has("functions")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(resp, "INVALID_REQUEST", "Missing username or functions");
                return;
            }

            String username = payload.get("username").getAsString();
            int functions = payload.get("functions").getAsInt();

            User user = findUser(username);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendErrorResponse(resp, "USER_NOT_FOUND", "User not connected: " + username);
                return;
            }

            //user.setFunctionsContributed(functions);
            sendSuccessResponse(resp, username, "functions", functions);

        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    private void handleUpdateCreditsUsed(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        try {
            JsonObject payload = readJsonPayload(req);

            if (payload == null || !payload.has("username") || !payload.has("creditsUsed")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(resp, "INVALID_REQUEST", "Missing username or creditsUsed");
                return;
            }

            String username = payload.get("username").getAsString();
            int creditsUsed = payload.get("creditsUsed").getAsInt();

            User user = findUser(username);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendErrorResponse(resp, "USER_NOT_FOUND", "User not connected: " + username);
                return;
            }

            //user.setCreditsUsed(creditsUsed);
            sendSuccessResponse(resp, username, "creditsUsed", creditsUsed);

        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    private void handleUpdateRuns(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        try {
            JsonObject payload = readJsonPayload(req);

            if (payload == null || !payload.has("username") || !payload.has("runs")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(resp, "INVALID_REQUEST", "Missing username or runs");
                return;
            }

            String username = payload.get("username").getAsString();
            int runs = payload.get("runs").getAsInt();

            User user = findUser(username);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendErrorResponse(resp, "USER_NOT_FOUND", "User not connected: " + username);
                return;
            }

            //user.setRunsCount(runs);
            sendSuccessResponse(resp, username, "runs", runs);

        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    // ===== Helper Methods =====

    private JsonObject readJsonPayload(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), JsonObject.class);
    }

    private User findUser(String username) {
        return userManager.getConnectedUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    private void sendSuccessResponse(HttpServletResponse resp, String username, String field, int value) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        JsonObject response = new JsonObject();
        response.addProperty("status", "ok");
        response.addProperty("message", field + " updated successfully");

        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty(field, value);
        response.add("data", data);

        resp.getWriter().write(gson.toJson(response));
    }

    private void sendErrorResponse(HttpServletResponse resp, String code, String error) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("status", "error");
        response.addProperty("code", code);
        response.addProperty("error", error);
        resp.getWriter().write(gson.toJson(response));
    }

    private void handleError(HttpServletResponse resp, Exception e) throws IOException {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        sendErrorResponse(resp, "INTERNAL_ERROR", "Failed to update: " + e.getMessage());
        e.printStackTrace();
    }
}