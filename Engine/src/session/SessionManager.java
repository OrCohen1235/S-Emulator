package session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * מנהל את כל הסשנים הפעילים במערכת
 */
public class SessionManager {

    // מפה: sessionId → UserSession
    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * קבלת/יצירת Session
     * אם ה-Session לא קיים - יוצר חדש
     */
    public UserSession getOrCreateSession(String sessionId) {
        Objects.requireNonNull(sessionId, "Session ID cannot be null");
        return activeSessions.computeIfAbsent(sessionId, UserSession::new);
    }

    /**
     * קבלת Session קיים (ללא יצירה)
     */
    public UserSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * האם Session קיים
     */
    public boolean sessionExists(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    /**
     * מחיקת Session
     */
    public UserSession removeSession(String sessionId) {
        UserSession removed = activeSessions.remove(sessionId);
        if (removed != null) {
            System.out.println("[SessionManager] Removed session: " + sessionId);
        }
        return removed;
    }

    /**
     * כל הסשנים הפעילים
     */
    public Collection<UserSession> getAllSessions() {
        return Collections.unmodifiableCollection(activeSessions.values());
    }

    /**
     * מספר סשנים פעילים
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * חיפוש Session לפי username
     */
    public UserSession findSessionByUsername(String username) {
        if (username == null) {
            return null;
        }

        return activeSessions.values().stream()
                .filter(session -> username.equals(session.getUsername()))
                .findFirst()
                .orElse(null);
    }

    /**
     * קבלת כל ה-usernames המחוברים
     */
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

    /**
     * ניקוי כל הסשנים (shutdown)
     */
    public void clearAllSessions() {
        activeSessions.clear();
        System.out.println("[SessionManager] Cleared all sessions");
    }

    @Override
    public String toString() {
        return "SessionManager{activeSessionCount=" + activeSessions.size() + "}";
    }
}