package com.example.skillverse_android.models;
public class Module {
    private int id;
    private String documentId;
    private String title;
    private String description;
    private int duration;
    private boolean completed;
    private int order;
    private String youtubeVideoId;
    public Module() {
    }
    public Module(int id, String title, String description, int duration, boolean completed, int order, String youtubeVideoId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.completed = completed;
        this.order = order;
        this.youtubeVideoId = youtubeVideoId;
    }
    public int getId() {
        return id;
    }
    public String getDocumentId() {
        return documentId;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String description() {
        return description;
    }
    public int getDuration() {
        return duration;
    }
    public boolean isCompleted() {
        return completed;
    }
    public int getOrder() {
        return order;
    }
    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    public void setOrder(int order) {
        this.order = order;
    }
    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }
}
