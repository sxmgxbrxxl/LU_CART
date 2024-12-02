package com.advento.lucart;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String productId;
    private String name;
    private String productCategory;
    private double price;
    private String imageUrl;
    private int quantity;
    private String userId; // Buyer ID
    private String sellerId; // Seller ID

    // No-argument constructor for Firebase
    public CartItem() {}

    // Parameterized constructor
    public CartItem(String productId, String name, String productCategory, double price, String imageUrl, int quantity, String userId, String sellerId) {
        this.productId = productId;
        this.name = name;
        this.productCategory = productCategory;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.userId = userId;
        this.sellerId = sellerId;
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getProductCategory() {
        return productCategory;
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

    public String getSellerId() {
        return sellerId;
    }

    // Setters
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
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

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    // Optional: Calculate total price for this item
    public double getTotalPrice() {
        return price * quantity;
    }
}
