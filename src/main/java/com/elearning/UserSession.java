package com.elearning;

public class UserSession {
    private static int currentUserId = -1;
    private static String currentRole = "";

    public static void setCurrentUser(int userId, String role) {
        currentUserId = userId;
        currentRole = role;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static void logout() {
        currentUserId = -1;
        currentRole = "";
    }
}
