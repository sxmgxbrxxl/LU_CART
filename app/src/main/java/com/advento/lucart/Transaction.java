package com.advento.lucart;

import java.io.Serializable;
import java.util.List;

public class Transaction implements Serializable {
    private List<CartItem> cartItems;
    private String userId; // Buyer ID
    private String businessId; // Seller ID
    private String transactionId; // Transaction ID
    private String status; // "To Ship", "To Receive", "Completed", "Cancelled"
    private String deliveryLocation;
    private String paymentMethod;
    private double shippingFee; // Shipping fee for the transaction
    private double totalPrice; // Total price including shipping fee

    // No-argument constructor (required for Firestore)
    public Transaction() {
        // Required for Firestore deserialization
    }

    // Parameterized constructor
    public Transaction(List<CartItem> cartItems, String userId, String businessId, String transactionId, String status,
                       String deliveryLocation, String paymentMethod, double shippingFee) {
        this.cartItems = cartItems;
        this.userId = userId;
        this.businessId = businessId;
        this.transactionId = transactionId;
        this.status = status;
        this.deliveryLocation = deliveryLocation;
        this.paymentMethod = paymentMethod;
        this.shippingFee = shippingFee;
        this.totalPrice = calculateTotalPrice(); // Automatically calculate total price during initialization
    }

    // Calculate total price including shipping fee
    public double calculateTotalPrice() {
        double subtotal = 0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                subtotal += item.getTotalPrice();
            }
        }
        return subtotal + shippingFee;
    }

    // Getters and setters
    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        this.totalPrice = calculateTotalPrice(); // Recalculate total price when cart items change
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
        this.totalPrice = calculateTotalPrice(); // Recalculate total price when shipping fee changes
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }


}
