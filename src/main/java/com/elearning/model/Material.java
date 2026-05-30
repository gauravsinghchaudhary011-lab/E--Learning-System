package com.elearning.model;

import java.time.LocalDateTime;

public class Material {
    private int id;
    private int subjectId;
    private String title;
    private String type; // notes, pyq, etc.
    private String filePath;
    private LocalDateTime uploadDate;

    public Material() {}

    public Material(int id, int subjectId, String title, String type, String filePath, LocalDateTime uploadDate) {
        this.id = id;
        this.subjectId = subjectId;
        this.title = title;
        this.type = type;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
}
