package com.example.skillverse_android.models;
public class Certificate {
    private String documentId;
    private String id;
    private String userId;
    private String courseId;
    private String courseName;
    private String studentName;
    private String dateCompleted;
    private String certificateId;
    private float score;
    private long createdAt;
    public Certificate() {}
    public Certificate(String id, String courseName, String studentName, String dateCompleted, String certificateId, float score) {
        this.id = id;
        this.courseName = courseName;
        this.studentName = studentName;
        this.dateCompleted = dateCompleted;
        this.certificateId = certificateId;
        this.score = score;
    }
    public Certificate(String userId, String courseId, String courseName, String studentName, String dateCompleted, String certificateId) {
        this.userId = userId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.studentName = studentName;
        this.dateCompleted = dateCompleted;
        this.certificateId = certificateId;
        this.score = 100.0f;
        this.createdAt = System.currentTimeMillis();
    }
    public String getDocumentId() { return documentId; }
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getStudentName() { return studentName; }
    public String getDateCompleted() { return dateCompleted; }
    public String getCertificateId() { return certificateId; }
    public float getScore() { return score; }
    public long getCreatedAt() { return createdAt; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setDateCompleted(String dateCompleted) { this.dateCompleted = dateCompleted; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
    public void setScore(float score) { this.score = score; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
