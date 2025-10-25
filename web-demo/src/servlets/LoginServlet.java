package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import session.SessionManager;
import session.UserSession;
import users.UserManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private SessionManager sessionManager;
    private UserManager userManager;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        sessionManager = (SessionManager) getServletContext().getAttribute("sessionManager");
        userManager = (UserManager) getServletContext().getAttribute("userManager");

        if (sessionManager == null || userManager == null) {
            throw new ServletException("Managers not initialized in context");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        LoginRequest loginReq;
        try {
            String body = readBody(request);
            loginReq = gson.fromJson(body, LoginRequest.class);
        } catch (JsonSyntaxException e) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("INVALID_JSON", "Malformed JSON"));
            return;
        }

        if (loginReq == null || loginReq.username == null || loginReq.username.trim().isEmpty()) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("USERNAME_REQUIRED", "Username is required"));
            return;
        }

        final String username = loginReq.username.trim();

        // בדיקה אם המשתמש כבר מחובר
        if (!userManager.tryLogin(username)) {
            writeJson(response, HttpServletResponse.SC_CONFLICT,
                    ApiResponse.error("USERNAME_TAKEN", "Username '" + username + "' is already logged in"));
            return;
        }

        try {
            // יצירת/קבלת סשן HTTP
            HttpSession httpSession = request.getSession(true);
            String sessionId = httpSession.getId();

            // יצירת/קבלת סשן לוגי של המשתמש
            UserSession userSession = sessionManager.getOrCreateSession(sessionId);
            userSession.setUsername(username);

            // החזרה תקינה
            writeJson(response, HttpServletResponse.SC_OK,
                    ApiResponse.ok(new LoginData(username)));

            System.out.println("✓ User logged in: " + username + " (session: " + sessionId + ")");

        } catch (Exception e) {
            // rollback
            userManager.logout(username);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("SERVER_ERROR", e.getMessage()));
        }
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = request.getReader()) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    // DTOs
    private static class LoginRequest {
        String username;
    }
    private static class LoginData {
        String username;
        LoginData(String username) { this.username = username; }
    }
    private static class ApiResponse<T> {
        String status; // "ok" | "error"
        String code;   // error code if error
        String error;  // error message if error
        T data;        // payload if ok

        static <T> ApiResponse<T> ok(T data) {
            ApiResponse<T> r = new ApiResponse<>();
            r.status = "ok";
            r.data = data;
            return r;
        }
        static <T> ApiResponse<T> error(String code, String msg) {
            ApiResponse<T> r = new ApiResponse<>();
            r.status = "error";
            r.code = code;
            r.error = msg;
            return r;
        }
    }

    private void writeJson(HttpServletResponse resp, int status, Object payload) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(payload));
    }
}
