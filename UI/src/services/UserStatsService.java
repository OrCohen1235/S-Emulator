package services;

import model.UserViewModel;
import java.util.List;

/**
 * ממשק כללי שמחזיר את רשימת המשתמשים המחוברים והסטטיסטיקות שלהם.
 */
public interface UserStatsService {
    List<UserViewModel> fetchConnectedUsers();
}
