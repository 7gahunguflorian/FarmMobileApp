package com.example.farmmobileapp.models;

public class AuthResponse {
    private String token;
    private User user;

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + (token != null ? "present" : "null") + '\'' +
                ", user=" + (user != null ? user.toString() : "null") +
                '}';
    }
}
