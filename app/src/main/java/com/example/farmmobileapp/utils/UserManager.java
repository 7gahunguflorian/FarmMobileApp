package com.example.farmmobileapp.utils;

import android.content.Context;
import android.content.Intent;

import com.example.farmmobileapp.activities.LoginActivity;
import com.example.farmmobileapp.models.User;

public class UserManager {
    private static UserManager instance;
    private SessionManager sessionManager;

    // Singleton pattern
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() {
        // Private constructor
    }

    /**
     * Initialize with application context
     *
     * @param context Application context
     */
    public void init(Context context) {
        sessionManager = SessionManager.getInstance(context);
    }

    /**
     * Get current user
     *
     * @return Current user data
     */
    public User getCurrentUser() {
        if (sessionManager != null) {
            return sessionManager.getUser();
        }
        return null;
    }

    /**
     * Check if user is logged in
     *
     * @return True if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return sessionManager != null && sessionManager.isLoggedIn();
    }

    /**
     * Check if user is a farmer
     *
     * @return True if user is a farmer, false otherwise
     */
    public boolean isFarmer() {
        User user = getCurrentUser();
        return user != null && "FARMER".equals(user.getRole());
    }

    /**
     * Check if user is a client
     *
     * @return True if user is a client, false otherwise
     */
    public boolean isClient() {
        User user = getCurrentUser();
        return user != null && "CLIENT".equals(user.getRole());
    }

    /**
     * Check if user is an admin
     *
     * @return True if user is an admin, false otherwise
     */
    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && "ADMIN".equals(user.getRole());
    }

    /**
     * Logout current user and redirect to login
     *
     * @param context Current context
     */
    public void logout(Context context) {
        if (sessionManager != null) {
            sessionManager.logout();
        }

        // Redirect to login activity
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Simple logout without redirect
     */
    public void logout() {
        if (sessionManager != null) {
            sessionManager.logout();
        }
    }

    /**
     * Save user to session
     *
     * @param user User to save
     */
    public void saveUser(User user) {
        if (sessionManager != null) {
            sessionManager.saveUser(user);
        }
    }

    /**
     * Get user's role
     *
     * @return User role or null if not logged in
     */
    public String getUserRole() {
        User user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * Get user's ID
     *
     * @return User ID or null if not logged in
     */
    public Long getUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Get user's name
     *
     * @return User name or null if not logged in
     */
    public String getUserName() {
        User user = getCurrentUser();
        return user != null ? user.getName() : null;
    }
}