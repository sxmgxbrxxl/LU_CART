package com.advento.lucart;

import java.util.Date;

public class Notification {
    private String title;
    private String message;
    private String userId;
    private String status;
    private Long timestamp;  // Store as Long (milliseconds)

    // No-argument constructor for Firestore
    public Notification() {}

    // Constructor to initialize the notification object
    public Notification(String title, String message, String userId, String status, Long timestamp) {
        this.title = title;
        this.message = message;
        this.userId = userId;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters for each field with default values for null
    public String getTitle() {
        return title != null ? title : "No Title";  // Default to "No Title" if null
    }

    public String getMessage() {
        return message != null ? message : "No Message";  // Default to "No Message" if null
    }

    public String getUserId() {
        return userId != null ? userId : "Unknown User";  // Default to "Unknown User" if null
    }

    public String getStatus() {
        return status != null ? status : "No Status";  // Default to "No Status" if null
    }

    public Long getTimestamp() {
        return timestamp;
    }

    // Converts timestamp (milliseconds) to Date
    public Date getDate() {
        return timestamp != null ? new Date(timestamp) : null;  // Handle null case
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", userId='" + userId + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}