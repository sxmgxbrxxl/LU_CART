package com.advento.lucart;

public class Shop {
    private String businessName;
    private String photoUrl;

    public Shop() {
        // Default constructor required for calls to DataSnapshot.getValue(Shop.class)
    }

    public Shop(String businessName, String photoUrl) {
        this.businessName = businessName;
        this.photoUrl = photoUrl;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
