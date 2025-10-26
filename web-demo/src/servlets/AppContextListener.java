package servlets;

import session.SessionManager;
import users.ProgramRepository;
import users.UserManager;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * מאתחל את המערכת בהפעלת השרת
 * ומנקה בסגירה
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        System.out.println("==============================================");
        System.out.println("   S-Emulator Server Starting...");
        System.out.println("==============================================");

        UserManager userManager = new UserManager();
        SessionManager sessionManager = new SessionManager();
        ProgramRepository programRepository = new ProgramRepository();

        ctx.setAttribute("userManager", userManager);
        ctx.setAttribute("sessionManager", sessionManager);
        ctx.setAttribute("programRepository", programRepository);

        System.out.println("✓ UserManager initialized");
        System.out.println("✓ SessionManager initialized");
        System.out.println("✓ ProgramRepository initialized");
        System.out.println("==============================================");
        System.out.println("   S-Emulator Server Ready!");
        System.out.println("==============================================");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        System.out.println("==============================================");
        System.out.println("   S-Emulator Server Stopping...");
        System.out.println("==============================================");

        // ניקוי
        SessionManager sessionMgr = (SessionManager) ctx.getAttribute("sessionManager");
        if (sessionMgr != null) {
            sessionMgr.clearAllSessions();
        }

        ProgramRepository progRepo = (ProgramRepository) ctx.getAttribute("programRepository");
        if (progRepo != null) {
            progRepo.clearAllPrograms();
        }

        System.out.println("==============================================");
        System.out.println("   S-Emulator Server Stopped");
        System.out.println("==============================================");
    }
}