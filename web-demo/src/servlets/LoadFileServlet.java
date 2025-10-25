package servlets;

import com.google.gson.Gson;
import engine.Engine;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import session.SessionManager;
import session.UserSession;
import users.User;
import users.UserManager;
import utils.ServletsUtills;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@WebServlet(name = "LoadFileServlet", urlPatterns = "/load-file")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024L * 1024L * 100L,
        maxRequestSize = 1024L * 1024L * 120L
)
public class LoadFileServlet extends HttpServlet {
    private SessionManager sessionManager;
    private UserManager userManager;
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        String ctype = req.getContentType();
        if (ctype == null || !ctype.toLowerCase().startsWith("multipart/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Content-Type must be multipart/form-data\"}");
            return;
        }

        try {
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Missing file part named 'file'\"}");
                return;
            }
            HttpSession session = req.getSession(false);
            String username = (String) session.getAttribute("username");

            String submittedName = getSubmittedFileName(filePart);
            if (submittedName == null || submittedName.isBlank()) submittedName = "uploaded";
            String safeSuffix = "-" + submittedName.replaceAll("[^a-zA-Z0-9._-]", "_");

            Path tempFile = Files.createTempFile("upload-", safeSuffix);
            try (InputStream in = filePart.getInputStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 2) פתיחת Stream חדש מהקובץ הזמני, ובניית Engine חדש *עבור הבקשה הזו*
            int maxDegree = 0 ;
            try (InputStream in2 = Files.newInputStream(tempFile)) {
                Engine engine = ServletsUtills.createNewSystemProgram(getServletContext(), in2, username);
                engine.getProgramDTO().loadExpansion();
                maxDegree = engine.getProgramDTO().getMaxDegree();
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"status\":\"ok\",\"data\":\"" + escapeJson(String.valueOf(maxDegree)) + "\"}");

        } catch (ServletException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid multipart request: " + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private static String getSubmittedFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        for (String token : cd.split(";")) {
            String trimmedLower = token.trim().toLowerCase();
            if (trimmedLower.startsWith("filename=")) {
                String name = token.substring(token.indexOf('=') + 1).trim();
                if (name.startsWith("\"") && name.endsWith("\"") && name.length() >= 2) {
                    name = name.substring(1, name.length() - 1);
                }
                int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                return (slash >= 0) ? name.substring(slash + 1) : name;
            }
        }
        return null;
    }

    private static String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
