package utils;

import engine.Engine;
import jakarta.servlet.ServletContext;
import logic.dto.SystemFunctionDTO;
import users.ProgramRepository;
import users.SystemFunction;
import users.SystemProgram; // ✅ שינוי מ-SystemProgramsManager

import java.io.InputStream;
import java.util.*;

public class ServletsUtills {
    private static final String PROGRAM_REPOSITORY_ATTRIBUTE = "programRepository";

    private static final Object repositoryLock = new Object();


    /**
     * יוצר תוכנית חדשה ומוסיף אותה ל-Repository
     */
    public static SystemProgram createNewSystemProgram(ServletContext context, InputStream fileContext, String userName) { // ✅ שינוי
        synchronized (repositoryLock) {
            if (fileContext == null) {
                throw new IllegalArgumentException("File context cannot be null");
            }

            ProgramRepository repository = getProgramRepository(context);

            try {
                // ✅ השתמש ב-uploadProgram של ה-Repository
                SystemProgram newProgram = repository.uploadProgram(userName, fileContext);

                System.out.println("✓ System program created: " + newProgram.getName() + " by user: " + userName);

                return newProgram;

            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create program: " + e.getMessage(), e);
            }
        }
    }

    /**
     * מחזיר את כל התוכניות מה-Repository
     */
    public static List<SystemProgram> getAllSystemPrograms(ServletContext context) { // ✅ שינוי
        synchronized (repositoryLock) {
            ProgramRepository repository = getProgramRepository(context);
            return new ArrayList<>(repository.getAllPrograms());
        }
    }

    /**
     * מחזיר את כל הפונקציות מכל התוכניות
     */
    public static List<SystemFunctionDTO> getAllSystemFunctions(ServletContext context) {
        synchronized (repositoryLock) {
            ProgramRepository repository = getProgramRepository(context);
            List<SystemFunctionDTO> systemFunctions = new ArrayList<>();

            for (SystemFunction systemFunction : repository.getSystemFunctions()) {
                systemFunctions.add(new SystemFunctionDTO(systemFunction));
            }

            return systemFunctions;
        }
    }

    /**
     * Helper method - מחזיר את ה-ProgramRepository
     */
    private static ProgramRepository getProgramRepository(ServletContext context) {
        ProgramRepository repository = (ProgramRepository) context.getAttribute(PROGRAM_REPOSITORY_ATTRIBUTE);
        if (repository == null) {
            throw new IllegalStateException("ProgramRepository not initialized in ServletContext");
        }
        return repository;
    }

    /**
     * מחזיר תוכנית ספציפית לפי שם
     */
    public static SystemProgram getSystemProgram(ServletContext context, String programName) { // ✅ שינוי
        synchronized (repositoryLock) {
            ProgramRepository repository = getProgramRepository(context);
            SystemProgram p = repository.getProgram(programName);
            return p;
        }
    }

    /**
     * מוחק תוכנית לפי שם
     */
    public static boolean removeSystemProgram(ServletContext context, String programName) {
        synchronized (repositoryLock) {
            ProgramRepository repository = getProgramRepository(context);
            SystemProgram removed = repository.removeProgram(programName); // ✅ שינוי
            return removed != null;
        }
    }
}