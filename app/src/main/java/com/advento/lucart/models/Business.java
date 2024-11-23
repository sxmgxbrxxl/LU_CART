package com.advento.lucart.models;

public class Business {

    private String businessName;
    private String email;
    private String password;  // If this is needed
    private String photoUrl;  // Add photoUrl to store the user's photo URL

    // No-argument constructor required for Firebase
    public Business() {
    }

    public Business(String businessName, String email, String password, String photoUrl) {
        this.businessName = businessName;
        this.email = email;
        this.password = password;
        this.photoUrl = photoUrl;
    }

    // Getters and setters
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
