package com.example.skillverse_android.models;
public class User {
    private String userId;
    private String email;
    private String name;
    private String role;
    private long createdAt;
    private long lastLogin;
    private String profileImageUrl;
    private String phoneNumber;
    public User() {
    }
    public User(String userId, String email, String name, String role, long createdAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
    }
    public String getUserId() {
        return userId;
    }
    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getRole() {
        return role;
    }
    public long getCreatedAt() {
        return createdAt;
    }
    public long getLastLogin() {
        return lastLogin;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public boolean isAdmin() {
        return "admin".equals(role);
    }
    public boolean isStudent() {
        return "student".equals(role);
    }
}
