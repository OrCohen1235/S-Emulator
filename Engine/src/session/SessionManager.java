package session;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active user sessions.
 * Thread-safe for concurrent access.
 */
public class SessionManager {

    private final Map<String, UserSession> activeSessions;

    public SessionManager() {
        this.activeSessions = new ConcurrentHashMap<>();
    }

    // ========== Session CRUD ==========

    public synchronized UserSession getOrCreateSession(String sessionId) {
        Objects.requireNonNull(sessionId, "Session ID cannot be null");
        return activeSessions.computeIfAbsent(sessionId, UserSession::new);
    }

    public UserSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public boolean sessionExists(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    public synchronized UserSession removeSession(String sessionId) {
        return activeSessions.remove(sessionId);
    }

    public Set<String> getAllSessionIds() {
        return Collections.unmodifiableSet(activeSessions.keySet());
    }

    public Collection<UserSession> getAllSessions() {
        return Collections.unmodifiableCollection(activeSessions.values());
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    // ========== Cleanup Operations ==========


    public synchronized void clearAllSessions() {
        activeSessions.clear();
    }

    // ========== Query Operations ==========

    public UserSession findSessionByUsername(String username) {
        if (username == null) {
            return null;
        }

        return activeSessions.values().stream()
                .filter(session -> username.equals(session.getUsername()))
                .findFirst()
                .orElse(null);
    }

    public Set<String> getAllLoggedInUsernames() {
        Set<String> usernames = new HashSet<>();
        for (UserSession session : activeSessions.values()) {
            String username = session.getUsername();
            if (username != null) {
                usernames.add(username);
            }
        }
        return usernames;
    }

    @Override
    public String toString() {
        return "SessionManager{" +
                "activeSessionCount=" + activeSessions.size() +
                '}';
    }
}