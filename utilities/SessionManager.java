// manages the current user session in the application
package utilities;
import models.User;

public class SessionManager {
    private static User currentUser = null;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "Admin".equals(currentUser.getRole());
    }

    public static boolean isCashier() {
        return currentUser != null && "Cashier".equals(currentUser.getRole());
    }
}
