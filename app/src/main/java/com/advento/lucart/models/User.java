package com.advento.lucart.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String photoUrl; // Add photo URL field

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {}

    // Updated constructor to include photoUrl
    public User(String firstName, String lastName, String email, String password, String photoUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = hashPassword(password);
        this.photoUrl = photoUrl;  // Assign the photo URL
    }

    // Getters and setters
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
        this.password = hashPassword(password);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // Password hashing method
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
