package com.example.urbanroots;

public class CareTask {
    private String taskId;
    private String type;
    private String cropId;
    private String userId;
    private String dueDate; // Format: yyyy-MM-dd
    private String postalCode;
    private String currentStatus;

    public CareTask() {
        // Empty constructor for Firebase
    }

    public CareTask(String taskId, String type, String cropId, String userId, String dueDate, String postalCode, String currentStatus) {
        this.taskId = taskId;
        this.type = type;
        this.cropId = cropId;
        this.userId = userId;
        this.dueDate = dueDate;
        this.postalCode = postalCode;
        this.currentStatus = currentStatus;
    }

    // Getters and setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCropId() {
        return cropId;
    }

    public void setCropId(String cropId) {
        this.cropId = cropId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}