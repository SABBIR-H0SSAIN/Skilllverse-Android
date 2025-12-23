package com.example.skillverse_android.models;
import java.util.ArrayList;
import java.util.List;
public class Course {
    private String id;
    private String title;
    private String description;
    private List<String> instructorIds;
    private String category;
    private String difficulty;
    private int duration;
    private int lessonsCount;
    private float rating;
    private String imageUrl;
    private String enrollmentKey;
    private double price;
    private boolean isEnrolled;
    private int progress;
    private boolean isPublished;
    private int enrollmentCount;
    private Long createdAt;
    private Long updatedAt;
    public Course() {
    }
    public Course(String id, String title, String description, List<String> instructorIds,
                  String category, String difficulty, int duration, int lessonsCount,
                  float rating, String imageUrl, String enrollmentKey, double price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructorIds = instructorIds;
        this.category = category;
        this.difficulty = difficulty;
        this.duration = duration;
        this.lessonsCount = lessonsCount;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.enrollmentKey = enrollmentKey;
        this.price = price;
        this.isEnrolled = false;
        this.progress = 0;
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getInstructorIds() {
        return instructorIds != null ? instructorIds : new ArrayList<>();
    }
    public String getCategory() { return category; }
    public String getDifficulty() { return difficulty; }
    public int getDuration() { return duration; }
    public int getLessonsCount() { return lessonsCount; }
    public float getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }
    public String getEnrollmentKey() { return enrollmentKey; }
    public double getPrice() { return price; }
    public boolean isEnrolled() { return isEnrolled; }
    public int getProgress() { return progress; }
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setInstructorIds(List<String> instructorIds) { this.instructorIds = instructorIds; }
    public void setEnrolled(boolean enrolled) { isEnrolled = enrolled; }
    public void setProgress(int progress) { this.progress = progress; }
    public void setPublished(boolean published) { isPublished = published; }
    public void setEnrollmentCount(int enrollmentCount) { this.enrollmentCount = enrollmentCount; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    public void setCategory(String category) { this.category = category; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setLessonsCount(int lessonsCount) { this.lessonsCount = lessonsCount; }
    public void setRating(float rating) { this.rating = rating; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setEnrollmentKey(String enrollmentKey) { this.enrollmentKey = enrollmentKey; }
    public void setPrice(double price) { this.price = price; }
    public boolean isPublished() { return isPublished; }
    public int getEnrollmentCount() { return enrollmentCount; }
    public Long getCreatedAt() { return createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
}
