package session;

import engine.Engine;
import users.SystemProgram;


public class UserSession {

    private final String sessionId;
    private String username;
    private Engine currentEngine;

    public UserSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setCurrentEngine(Engine currentEngine) {
        this.currentEngine = currentEngine;
    }

    public Engine getCurrentEngine() {
        return currentEngine;
    }

    // ========== Getters/Setters ==========

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        System.out.println("[UserSession] " + sessionId + " → " + username);
    }

    /**
     * האם המשתמש מחובר
     */
    public boolean isLoggedIn() {
        return username != null;
    }

    @Override
    public String toString() {
        return String.format("UserSession{id=%s, user=%s}",
                sessionId, username != null ? username : "not logged in");
    }
}