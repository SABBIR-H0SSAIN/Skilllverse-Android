package com.example.skillverse_android.models;
public class ModuleResource {
    private int id;
    private String documentId;
    private String title;
    private String type;
    private String url;
    private String size;
    public ModuleResource() {
    }
    public ModuleResource(int id, String title, String type, String url, String size) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.url = url;
        this.size = size;
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
    public String getType() {
        return type;
    }
    public String getUrl() {
        return url;
    }
    public String getSize() {
        return size;
    }
    public String getIcon() {
        switch (type != null ? type : "") {
            case "PDF":
                return "📄";
            case "Video":
                return "🎥";
            case "Notes":
                return "📝";
            case "Quiz":
                return "📋";
            default:
                return "📎";
        }
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
    public void setType(String type) {
        this.type = type;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setSize(String size) {
        this.size = size;
    }
}
