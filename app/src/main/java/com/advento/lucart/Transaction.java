package com.advento.lucart;

import java.io.Serializable;
import java.util.List;

public class Transaction implements Serializable {
    private List<CartItem> cartItems;
    private String userId; // Buyer ID
    private String sellerId; // Seller ID
    private String status; // "To Ship", "To Receive", "Completed", "Cancelled"
    private String deliveryLocation;
    private String paymentMethod;

    // No-argument constructor (required for Firestore)
    public Transaction() {
        // Required for Firestore deserialization
    }

    // Parameterized constructor
    public Transaction(List<CartItem> cartItems, String userId, String sellerId, String status, String deliveryLocation, String paymentMethod) {
        this.cartItems = cartItems;
        this.userId = userId;
        this.sellerId = sellerId;
        this.status = status;
        this.deliveryLocation = deliveryLocation;
        this.paymentMethod = paymentMethod;
    }

    // Getters and setters
    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
