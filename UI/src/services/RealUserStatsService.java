//package services;
//
//import model.UserViewModel;
//import users.User;
//import users.UserManager;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * שירות שמספק את רשימת המשתמשים המחוברים וסטטיסטיקותיהם
// * באמצעות ה-UserManager.
// */
//public class RealUserStatsService implements UserStatsService {
//
//    private final UserManager userManager;
//
//    public RealUserStatsService(UserManager userManager) {
//        this.userManager = userManager;
//    }
//
//    /**
//     * מחזיר צילום מצב של כלל המשתמשים המחוברים למערכת כרגע.
//     */
//    @Override
//    public List<UserViewModel> fetchConnectedUsers() {
//        return userManager.getConnectedUsers().stream()
//                .map(this::mapToStats)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * ממיר אובייקט User (מה-UserManager) לאובייקט UserStats
//     * שמוצג בטבלת ה-JavaFX.
//     */
//    private UserViewModel mapToStats(User user) {
//        return new UserViewModel(
//                user.getUsername(),
//                user.getMainProgramsUploaded(),
//                user.getFunctionsContributed(),
//                user.getCreditsCurrent(),
//                user.getCreditsUsed(),
//                user.getRunsCount()
//        );
//    }
//}
