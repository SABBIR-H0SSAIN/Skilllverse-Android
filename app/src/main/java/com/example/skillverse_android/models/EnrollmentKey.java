package com.example.skillverse_android.models;
public class EnrollmentKey {
    private String id;
    private String key;
    private String courseId;
    private String courseTitle;
    private boolean isUsed;
    private String usedBy;
    private Long usedAt;
    private long createdAt;
    private String createdBy;
    private Long expiresAt;
    public EnrollmentKey() {
    }
    public EnrollmentKey(String id, String key, String courseId, boolean isUsed,
                        String usedBy, Long usedAt, long createdAt,
                        String createdBy, Long expiresAt) {
        this.id = id;
        this.key = key;
        this.courseId = courseId;
        this.isUsed = isUsed;
        this.usedBy = usedBy;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
    public String getUsedBy() { return usedBy; }
    public void setUsedBy(String usedBy) { this.usedBy = usedBy; }
    public Long getUsedAt() { return usedAt; }
    public void setUsedAt(Long usedAt) { this.usedAt = usedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public boolean isExpired() {
        return expiresAt != null && expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }
}
