package servlets;

import com.google.gson.Gson;
import session.SessionManager;
import session.UserSession;
import users.ProgramRepository;
import users.UserManager;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    protected static final Gson GSON = new Gson();

    // ========== Managers ==========

    protected UserManager getUserManager() {
        return (UserManager) getServletContext().getAttribute("userManager");
    }

    protected SessionManager getSessionManager() {
        return (SessionManager) getServletContext().getAttribute("sessionManager");
    }

    protected ProgramRepository getProgramRepository() {
        return (ProgramRepository) getServletContext().getAttribute("programRepository");
    }

    // ========== Session Helpers ==========

    protected UserSession getUserSession(HttpServletRequest req) {
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null) {
            return null;
        }
        String sessionId = httpSession.getId();
        return getSessionManager().getSession(sessionId);
    }

    protected boolean isLoggedIn(HttpServletRequest req) {
        UserSession session = getUserSession(req);
        return session != null && session.getUsername() != null;
    }

    protected String getLoggedInUsername(HttpServletRequest req) {
        UserSession session = getUserSession(req);
        return (session != null) ? session.getUsername() : null;
    }

    // ========== Response Helpers ==========

    protected void sendJsonResponse(HttpServletResponse resp, Object data)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(GSON.toJson(data));
    }

    protected void sendError(HttpServletResponse resp, int status, String message)
            throws IOException {
        resp.setStatus(status);
        sendJsonResponse(resp, new ErrorResponse(message));
    }

    protected void sendSuccess(HttpServletResponse resp, Object data)
            throws IOException {
        sendJsonResponse(resp, new SuccessResponse(data));
    }

    // ========== Response Classes ← כאן החסר! ==========

    /**
     * מחלקה לתשובת שגיאה
     */
    protected static class ErrorResponse {
        public String status = "error";
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    /**
     * מחלקה לתשובת הצלחה
     */
    protected static class SuccessResponse {
        public String status = "ok";
        public Object data;

        public SuccessResponse(Object data) {
            this.data = data;
        }
    }
}