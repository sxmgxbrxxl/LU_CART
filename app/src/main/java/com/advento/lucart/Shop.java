package com.advento.lucart;

public class Shop {
    private String businessId;
    private String businessName;
    private String photoUrl;

    public Shop() {
        // Default constructor required for calls to DataSnapshot.getValue(Shop.class)
    }

    public Shop(String businessId, String businessName, String photoUrl) {
        this.businessId = businessId;
        this.businessName = businessName;
        this.photoUrl = photoUrl;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
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
