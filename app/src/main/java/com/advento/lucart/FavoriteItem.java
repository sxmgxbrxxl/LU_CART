package com.advento.lucart;

public class FavoriteItem {
    private String productId;
    private String productName;
    private String productCategory;
    private String productPrice;
    private String productImage;
    private String productDescription;

    public FavoriteItem() {
    }

    public FavoriteItem(String productId, String productName, String productCategory, String productPrice, String productImage, String productDescription) {
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.productDescription = productDescription;
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public String getProductImage() {
        return productImage;
    }
    public String getProductDescription() {
        return productDescription;
    }

    // Setters
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
}
