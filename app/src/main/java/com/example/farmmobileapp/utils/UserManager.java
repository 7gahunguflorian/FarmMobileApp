package com.example.farmmobileapp.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.farmmobileapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserManager {
    private static UserManager instance;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private User currentUser;

    // Singleton pattern
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Initialize with application context
     *
     * @param context Application context
     */
    public void init(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        loadUserFromPreferences();
    }

    /**
     * Load user data from SharedPreferences
     */
    private void loadUserFromPreferences() {
        String userId = sharedPreferences.getString(Constants.PREF_USER_ID, null);
        if (!TextUtils.isEmpty(userId)) {
            currentUser = new User();
            currentUser.setId(userId);
            currentUser.setName(sharedPreferences.getString(Constants.PREF_USER_NAME, ""));
            currentUser.setEmail(sharedPreferences.getString(Constants.PREF_USER_EMAIL, ""));
            currentUser.setUserType(sharedPreferences.getString(Constants.PREF_USER_TYPE, ""));
            currentUser.setProfileImageUrl(sharedPreferences.getString(Constants.PREF_USER_PROFILE_IMAGE, ""));
        }
    }

    /**
     * Save user data to SharedPreferences
     *
     * @param user User data to save
     */
    public void saveUserToPreferences(User user) {
        if (user != null && sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.PREF_USER_ID, user.getId());
            editor.putString(Constants.PREF_USER_NAME, user.getName());
            editor.putString(Constants.PREF_USER_EMAIL, user.getEmail());
            editor.putString(Constants.PREF_USER_TYPE, user.getUserType());
            editor.putString(Constants.PREF_USER_PROFILE_IMAGE, user.getProfileImageUrl());
            editor.apply();

            currentUser = user;
        }
    }

    /**
     * Get current user
     *
     * @return Current user data
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     *
     * @return True if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null && !TextUtils.isEmpty(currentUser.getId());
    }

    /**
     * Check if user is a farmer
     *
     * @return True if user is a farmer, false otherwise
     */
    public boolean isFarmer() {
        return isLoggedIn() && Constants.USER_TYPE_FARMER.equals(currentUser.getUserType());
    }

    /**
     * Check if user is a client
     *
     * @return True if user is a client, false otherwise
     */
    public boolean isClient() {
        return isLoggedIn() && Constants.USER_TYPE_CLIENT.equals(currentUser.getUserType());
    }

    /**
     * Fetch user data from Firestore
     *
     * @param userId User ID to fetch
     * @param callback Callback for fetch result
     */
    public void fetchUserData(String userId, final UserCallback callback) {
        if (TextUtils.isEmpty(userId)) {
            if (callback != null) {
                callback.onFailure("User ID is empty");
            }
            return;
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId());
                            saveUserToPreferences(user);
                            if (callback != null) {
                                callback.onSuccess(user);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure("Failed to parse user data");
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onFailure("User not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Logout current user
     */
    public void logout() {
        firebaseAuth.signOut();

        // Clear SharedPreferences
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }

        currentUser = null;
    }

    /**
     * Update user profile
     *
     * @param name New name
     * @param profileImageUrl New profile image URL
     * @param callback Callback for update result
     */
    public void updateUserProfile(String name, String profileImageUrl, final UserCallback callback) {
        if (!isLoggedIn()) {
            if (callback != null) {
                callback.onFailure("User not logged in");
            }
            return;
        }

        // Create update data
        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setName(name);
        updatedUser.setEmail(currentUser.getEmail());
        updatedUser.setUserType(currentUser.getUserType());
        updatedUser.setProfileImageUrl(profileImageUrl);

        // Update in Firestore
        db.collection(Constants.COLLECTION_USERS)
                .document(currentUser.getId())
                .update(
                        "name", name,
                        "profileImageUrl", profileImageUrl
                )
                .addOnSuccessListener(aVoid -> {
                    saveUserToPreferences(updatedUser);
                    if (callback != null) {
                        callback.onSuccess(updatedUser);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Callback for user operations
     */
    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    /**
     * User model for app usage
     */
    public static class User {
        private String id;
        private String name;
        private String email;
        private String userType;
        private String profileImageUrl;

        public User() {
            // Empty constructor needed for Firestore
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public void setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
        }
    }
}