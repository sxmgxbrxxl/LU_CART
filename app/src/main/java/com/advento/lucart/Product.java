package com.advento.lucart;

public class Product {
    private String productId;
    private String productName;
    private String productCategory;
    private String productPrice;
    private String productDescription;
    private String productImage;
    private String status;
    private String userId;// Add this field for the product category

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    public Product(String productName, String productCategory, String productPrice, String productDescription, String productImage, String productId, String status, String userId) {
        this.productName = productName;
        this.productCategory = productCategory;
        this.productPrice = productPrice;
        this.productDescription = productDescription;
        this.productImage = productImage;
        this.productId = productId;
        this.status = status;
        this.userId = userId;
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

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
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
}
