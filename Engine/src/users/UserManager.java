package users;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * מנהל את המשתמשים המחוברים למערכת.
 */
public class UserManager {

    private final Map<String, User> connectedUsers = new ConcurrentHashMap<>();

    public synchronized boolean tryLogin(String username) {
        if (connectedUsers.containsKey(username)) {
            return false; // כבר מחובר
        }
        connectedUsers.put(username, new User(username));
        System.out.println("User logged in: " + username);
        return true;
    }

    public synchronized void logout(String username) {
        if (connectedUsers.remove(username) != null) {
            System.out.println("User logged out: " + username);
        }
    }

    public Collection<User> getConnectedUsers() {
        return connectedUsers.values();
    }

    public User getUser(String username) {
        return connectedUsers.get(username);
    }

    public void chargeCredits(String username, int amount) {
        User u = connectedUsers.get(username);
        if (u != null) {
            u.addCredits(amount);
        }
    }

    public boolean useCredits(String username, int amount) {
        User u = connectedUsers.get(username);
        return u != null && u.useCredits(amount);
    }


    public void addMainProgram(String username) {
        User u = connectedUsers.get(username);
        if (u != null) {
            u.addMainProgram();
        }
    }

    public void addFunctionContribution(String username) {
        User u = connectedUsers.get(username);
        if (u != null) {
            u.addFunctionContribution();
        }
    }

    public void incrementRunCount(String username) {
        User u = connectedUsers.get(username);
        if (u != null) {
            u.incrementRunsCount();
        }
    }

    // Debug utility
    public void printAllUsers() {
        System.out.println("Connected users:");
        for (User u : connectedUsers.values()) {
            System.out.println("  " + u);
        }
    }
}
