package utils;

import engine.Engine;
import jakarta.servlet.ServletContext;
import users.SystemProgramsManager;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.*;

public class ServletsUtills {
    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final String ENGINE_MANAGER_ATTRIBUTE_NAME = "engineManager";

    private static final Object userManagerLock = new Object();
    private static final Object engineManagerLock = new Object();

    public static Engine getEngineManager(ServletContext context, String programName) {
        synchronized (engineManagerLock) {
            SystemProgramsManager programsManager = (SystemProgramsManager) context.getAttribute(programName);
            return programsManager.getEngine();
        }
    }

    public static Engine createNewSystemProgram(ServletContext context, InputStream fileContext, String userName) {
        synchronized (engineManagerLock) {
            Engine checkEngine = new Engine(fileContext);
            SystemProgramsManager systemProgram = (SystemProgramsManager) context.getAttribute(checkEngine.getProgramDTO().getProgramName());
            if (fileContext != null) {
                String programName = checkEngine.getProgramDTO().getProgramName();
                systemProgram = new SystemProgramsManager(userName, checkEngine);
                System.out.println("System program created: " + programName);
                context.setAttribute(programName, systemProgram);
            } else if (checkEngine == null) {
                throw new IllegalStateException("Engine not initialized – upload a file first.");
            }
            return checkEngine;
        }
    }

    public static List<SystemProgramsManager> getAllSystemPrograms(ServletContext context) {
        List<SystemProgramsManager> programs = new ArrayList<>();

        Enumeration<String> names = context.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object value = context.getAttribute(name);

            if (value instanceof SystemProgramsManager spm) {
                programs.add(spm);
            }
        }

        return programs;
    }

}







