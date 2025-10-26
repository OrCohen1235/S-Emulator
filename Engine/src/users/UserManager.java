package users;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * מנהל את כל המשתמשים המחוברים למערכת
 */
public class UserManager {

    // מפה: username → User
    private final Map<String, User> connectedUsers = new ConcurrentHashMap<>();

    /**
     * ניסיון התחברות
     * @return true אם הצליח, false אם המשתמש כבר מחובר
     */
    public synchronized boolean tryLogin(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        username = username.trim();

        if (connectedUsers.containsKey(username)) {
            return false; // כבר מחובר
        }

        User newUser = new User(username);
        connectedUsers.put(username, newUser);

        System.out.println("[UserManager] User logged in: " + username);
        return true;
    }

    /**
     * התנתקות
     */
    public synchronized void logout(String username) {
        User removed = connectedUsers.remove(username);
        if (removed != null) {
            System.out.println("[UserManager] User logged out: " + username);
        }
    }

    /**
     * קבלת משתמש לפי שם
     */
    public User getUser(String username) {
        return connectedUsers.get(username);
    }

    /**
     * האם משתמש מחובר
     */
    public boolean isUserConnected(String username) {
        return connectedUsers.containsKey(username);
    }

    /**
     * כל המשתמשים המחוברים
     */
    public Collection<User> getConnectedUsers() {
        return Collections.unmodifiableCollection(connectedUsers.values());
    }

    /**
     * מספר משתמשים מחוברים
     */
    public int getConnectedUsersCount() {
        return connectedUsers.size();
    }

    /**
     * הדפסת כל המשתמשים (debug)
     */
    public void printAllUsers() {
        System.out.println("=== Connected Users ===");
        for (User user : connectedUsers.values()) {
            System.out.println("  " + user);
        }
        System.out.println("Total: " + connectedUsers.size());
    }
}