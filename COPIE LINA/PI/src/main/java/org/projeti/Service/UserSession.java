package org.projeti.Service;

public class UserSession {

    private static int userId; // Add user ID
    private static String email;
    private static String role;
    private static String name;

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int id) {
        UserSession.userId = id;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        UserSession.email = email;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        UserSession.role = role;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        UserSession.name = name;
    }

    // Clear session method
    public static void clearSession() {
        userId = 0; // Reset user ID
        email = null;
        role = null;
        name = null;
    }
}
