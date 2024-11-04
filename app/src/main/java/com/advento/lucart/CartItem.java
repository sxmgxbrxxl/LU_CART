package com.advento.lucart;

public class CartItem {
    private String productId;
    private String name;
    private double price;
    private String imageUrl;
    private int quantity;
    private String userId;  // To track which user added this item

    // Empty constructor for Firebase
    public CartItem() {}

    public CartItem(String productId, String name, double price, String imageUrl, int quantity, String userId) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.userId = userId;
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUserId() {
        return userId;
    }

    // Setters
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Optional: Calculate total price for this item
    public double getTotalPrice() {
        return price * quantity;
    }


}
