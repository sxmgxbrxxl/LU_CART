package com.advento.lucart;

public class Shop {
    private String name;
    private String imageUrl;

    public Shop() {
        // Empty constructor required for Firestore
    }

    public Shop(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
