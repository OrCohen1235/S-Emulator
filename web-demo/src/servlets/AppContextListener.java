package servlets;

import engine.Engine;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import session.SessionManager;

import users.UserManager;



/**
 * Initializes the application context.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // Create singleton instances
        SessionManager sessionManager = new SessionManager();
        UserManager userManager = new UserManager();

        // Store in context (available to all servlets)
        context.setAttribute("sessionManager", sessionManager);
        context.setAttribute("userManager", userManager);


    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("✓ Application shutdown complete");
    }
}