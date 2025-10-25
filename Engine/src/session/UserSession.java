package session;

import engine.Engine;
import logic.dto.ProgramDTO;
import program.ProgramLoadException;

import java.io.InputStream;
import java.util.*;

public class UserSession {

    private final String sessionId;
    private String username;

    // Program management
    private final Map<String, Engine> loadedPrograms;
    private Engine currentEngine;

    public UserSession(String sessionId) {
        this.sessionId = sessionId;
        this.loadedPrograms = new HashMap<>();
    }


    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void loadProgram(String programName, InputStream xmlContent)
            throws ProgramLoadException {

        Objects.requireNonNull(programName, "Program name cannot be null");
        Objects.requireNonNull(xmlContent, "XML content cannot be null");

        Engine engine = new Engine(xmlContent);

        if (!engine.getLoaded()) {
            throw new ProgramLoadException("Failed to load program: " + programName);
        }

        loadedPrograms.put(programName, engine);
        currentEngine = engine;
    }

    public void switchToProgram(String programName) {
        Engine engine = loadedPrograms.get(programName);

        if (engine == null) {
            throw new NoSuchElementException(
                    "Program '" + programName + "' not found. Available programs: "
            );
        }

        currentEngine = engine;
    }


    public int getProgramCount() {
        return loadedPrograms.size();
    }


    public Engine getCurrentEngine() {
        return currentEngine;
    }


}