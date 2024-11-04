package com.advento.lucart;

public class Product {
    private String productId;
    private String productName;
    private String productPrice;
    private String productDescription;
    private String productImage;
    private String status;
    private String userId;
    private String category; // Add this field for the product category

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    public Product(String productName, String productPrice, String productDescription, String productImage, String productId, String status, String userId, String category) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productDescription = productDescription;
        this.productImage = productImage;
        this.productId = productId;
        this.status = status;
        this.userId = userId;
        this.category = category;
    }

    // Getter and Setter methods
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category; // Getter for category
    }

    public void setCategory(String category) {
        this.category = category; // Setter for category
    }
}
