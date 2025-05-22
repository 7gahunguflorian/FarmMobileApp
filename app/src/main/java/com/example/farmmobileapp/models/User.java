package com.example.farmmobileapp.models;

public class User {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String role;
    private String profileImageUrl;

    // Default constructor
    public User() {}

    // Constructor
    public User(String name, String username, String email, String role) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isFarmer() {
        return "FARMER".equals(role);
    }

    public boolean isClient() {
        return "CLIENT".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}