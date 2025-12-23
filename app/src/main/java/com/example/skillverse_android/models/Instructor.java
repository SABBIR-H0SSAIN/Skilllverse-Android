package com.example.skillverse_android.models;
import java.util.List;
public class Instructor {
    private String id;
    private String name;
    private String title;
    private String bio;
    private String profileImageUrl;
    private String email;
    private List<String> expertise;
    private float rating;
    private int coursesCount;
    private Long createdAt;
    public Instructor() {}
    public Instructor(String id, String name, String title, String bio,
                     String profileImageUrl, String email, List<String> expertise,
                     float rating, int coursesCount, long createdAt) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.email = email;
        this.expertise = expertise;
        this.rating = rating;
        this.coursesCount = coursesCount;
        this.createdAt = createdAt;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getBio() { return bio; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getEmail() { return email; }
    public List<String> getExpertise() { return expertise; }
    public float getRating() { return rating; }
    public int getCoursesCount() { return coursesCount; }
    public Long getCreatedAt() { return createdAt; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setTitle(String title) { this.title = title; }
    public void setBio(String bio) { this.bio = bio; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setEmail(String email) { this.email = email; }
    public void setExpertise(List<String> expertise) { this.expertise = expertise; }
    public void setRating(float rating) { this.rating = rating; }
    public void setCoursesCount(int coursesCount) { this.coursesCount = coursesCount; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
