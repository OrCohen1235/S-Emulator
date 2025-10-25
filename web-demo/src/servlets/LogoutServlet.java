package servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import session.SessionManager;
import session.UserSession;
import users.UserManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    private SessionManager sessionManager;
    private UserManager userManager;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        sessionManager = (SessionManager) getServletContext().getAttribute("sessionManager");
        userManager = (UserManager) getServletContext().getAttribute("userManager");

        if (sessionManager == null || userManager == null) {
            throw new ServletException("Managers not initialized");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        HttpSession httpSession = request.getSession(false);

        if (httpSession == null) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("NO_SESSION", "No active session"));
            return;
        }

        String sessionId = httpSession.getId();
        UserSession userSession = sessionManager.getSession(sessionId);

        if (userSession == null || userSession.getUsername() == null) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("NOT_LOGGED_IN", "User not logged in"));
            return;
        }

        String username = userSession.getUsername();

        // 1. הסר מה-UserManager
        userManager.logout(username);

        // 2. הסר מה-SessionManager
        sessionManager.removeSession(sessionId);

        // 3. בטל את ה-HTTP Session
        httpSession.invalidate();

        writeJson(response, HttpServletResponse.SC_OK,
                ApiResponse.ok("Logged out successfully"));

        System.out.println("✓ User logged out: " + username);
    }

    private static class ApiResponse<T> {
        String status;
        String code;
        String error;
        T data;

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