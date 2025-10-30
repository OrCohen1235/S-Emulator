package servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import logic.dto.SystemProgramDTO;
import users.ProgramRepository;
import users.SystemProgram;
import users.User;
import users.UserManager;

import java.io.PrintWriter;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;

@WebServlet(name = "LoadFileServlet", urlPatterns = {"/load-file","/get-all-programs-dtos"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,           // 1 MB
        maxFileSize = 1024L * 1024L * 100L,        // 100 MB
        maxRequestSize = 1024L * 1024L * 120L      // 120 MB
)
public class LoadFileServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            ProgramRepository repo = getProgramRepository();
            Collection<SystemProgram> systemPrograms = repo.getAllPrograms();
            List<SystemProgramDTO> dtos = new ArrayList<>();

            for (SystemProgram systemProgram : systemPrograms) {
                dtos.add(new SystemProgramDTO(systemProgram));
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = resp.getWriter()) {
                Gson gson = new Gson();
                out.print(gson.toJson(dtos));
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve programs: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        // 1. בדיקת Content-Type
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Content-Type must be multipart/form-data");
            return;
        }

        // 2. בדיקת התחברות
        if (!isLoggedIn(req)) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "Not logged in");
            return;
        }

        String username = getLoggedInUsername(req);

        try {
            // 3. קבלת הקובץ
            Part filePart = req.getPart("file");

            if (filePart == null || filePart.getSize() == 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing file part named 'file'");
                return;
            }

            // 4. קבלת שם הקובץ
            String fileName = getSubmittedFileName(filePart);
            if (fileName == null || fileName.isBlank()) {
                fileName = "uploaded.xml";
            }

            System.out.println("[LoadFileServlet] Uploading file: " + fileName +
                    " by user: " + username);

            ProgramRepository repo = getProgramRepository();

            try (InputStream xmlStream = filePart.getInputStream()) {

                SystemProgram program = repo.uploadProgram(username, xmlStream);

                program.getProgramDTO().loadExpansion();

                UserManager userMgr = getUserManager();
                User user = userMgr.getUser(username);
                if (user != null) {
                    user.addMainProgram();
                }

                SystemProgramDTO dto = new SystemProgramDTO(program);

                UploadResponse uploadResp = new UploadResponse(
                        dto.getName(),
                        dto.getMaxDegree(),
                        dto.getInstructionCount(),
                        fileName
                );

                resp.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter out = resp.getWriter()) {
                    Gson gson = new Gson();
                    out.print(gson.toJson(uploadResp));
                    out.flush();
                }

            } catch (IllegalArgumentException e) {
                System.out.println("=== DEBUG: IllegalArgumentException message = " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());

            } catch (Exception e) {
                System.out.println("=== DEBUG: Other Exception message = " + e.getMessage());
                e.printStackTrace();
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Error uploading program: " + e.getMessage());
            }

        } catch (ServletException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid multipart request: " + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to upload program: " + e.getMessage());
        }
    }

    private static String getSubmittedFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");

        if (contentDisposition == null) {
            return null;
        }

        for (String token : contentDisposition.split(";")) {
            String trimmed = token.trim().toLowerCase();

            if (trimmed.startsWith("filename=")) {
                String fileName = token.substring(token.indexOf('=') + 1).trim();

                // הסרת גרשיים
                if (fileName.startsWith("\"") && fileName.endsWith("\"") &&
                        fileName.length() >= 2) {
                    fileName = fileName.substring(1, fileName.length() - 1);
                }

                // הסרת נתיב (אם יש)
                int lastSlash = Math.max(
                        fileName.lastIndexOf('/'),
                        fileName.lastIndexOf('\\')
                );

                if (lastSlash >= 0) {
                    fileName = fileName.substring(lastSlash + 1);
                }

                return fileName;
            }
        }

        return null;
    }

    /**
     * מחלקת תשובה להעלאה מוצלחת
     */
    private static class UploadResponse {
        String status;
        String error;
        String programName;
        int maxDegree;
        int instructionCount;
        String fileName;
        String message;

        UploadResponse(String programName, int maxDegree, int instructionCount,
                       String fileName) {
            this.status = "ok";
            this.error = null;
            this.programName = programName;
            this.maxDegree = maxDegree;
            this.instructionCount = instructionCount;
            this.fileName = fileName;
            this.message = "Program uploaded successfully";
        }
    }
}