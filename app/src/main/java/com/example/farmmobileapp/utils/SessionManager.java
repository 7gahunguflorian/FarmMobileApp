package com.example.farmmobileapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.farmmobileapp.models.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "AgriConnectSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String TAG = "SessionManager";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    private static SessionManager instance;

    private SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveAuthToken(String token) {
        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "Saving auth token");
            editor.putString(KEY_TOKEN, token);
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.apply();
        } else {
            Log.e(TAG, "Attempted to save null or empty token");
        }
    }

    public String getAuthToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        if (token == null) {
            Log.d(TAG, "No auth token found");
        }
        return token;
    }

    public String getAuthHeaderValue() {
        String token = getAuthToken();
        if (token == null) {
            Log.d(TAG, "No auth token available for header");
            return null;
        }
        Log.d(TAG, "Using token for auth header");
        return "Bearer " + token;
    }

    public void saveUser(User user) {
        if (user != null) {
            Log.d(TAG, "Saving user data for: " + user.getUsername());
            Gson gson = new Gson();
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER, userJson);
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.apply();
        } else {
            Log.e(TAG, "Attempted to save null user");
        }
    }

    public User getUser() {
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson != null) {
            try {
                Gson gson = new Gson();
                User user = gson.fromJson(userJson, User.class);
                Log.d(TAG, "Retrieved user: " + (user != null ? user.getUsername() : "null"));
                return user;
            } catch (Exception e) {
                Log.e(TAG, "Error parsing user data", e);
                return null;
            }
        }
        Log.d(TAG, "No user data found");
        return null;
    }

    public boolean isLoggedIn() {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        String token = getAuthToken();
        boolean hasToken = token != null && !token.isEmpty();
        boolean hasUser = getUser() != null;
        
        // Only consider logged in if we have all required data
        boolean validLogin = isLoggedIn && hasToken && hasUser;
        
        if (!validLogin && isLoggedIn) {
            Log.w(TAG, "Invalid login state detected, logging out");
            logout();
        }
        
        return validLogin;
    }

    public void logout() {
        Log.d(TAG, "Logging out user");
        editor.clear();
        editor.apply();
    }

    public String getUserProfileImage() {
        User user = getUser();
        if (user != null) {
            return user.getProfileImageUrl();
        }
        return null;
    }
}