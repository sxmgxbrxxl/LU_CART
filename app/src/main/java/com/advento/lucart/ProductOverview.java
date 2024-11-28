package com.advento.lucart;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityProductOverviewBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class    ProductOverview extends AppCompatActivity {

    private ActivityProductOverviewBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityProductOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.tbOverview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Drawable customBackButton = ContextCompat.getDrawable(this, R.drawable.ic_custom_back_overview);
        if (customBackButton != null) {
            customBackButton.setTint(ContextCompat.getColor(this, R.color.two_green));
            getSupportActionBar().setHomeAsUpIndicator(customBackButton);
        }

        // Retrieve data from intent
        String productId = getIntent().getStringExtra("productId");
        String productImage = getIntent().getStringExtra("productImage");
        String productName = getIntent().getStringExtra("productName");
        String productCategory = getIntent().getStringExtra("productCategory");
        String productPrice = getIntent().getStringExtra("productPrice");
        String productDescription = getIntent().getStringExtra("productDescription");

        //Import Image
        Glide.with(this)
                .load(productImage)
                .into(binding.ivProductImage);

        // Set data to views
        binding.tvProductName.setText(productName);
        binding.tvCategory.setText(productCategory);
        binding.tvProductPrice.setText("₱ " + productPrice);

        // Setup quantity buttons
        binding.buttonIncrease.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
        });

        binding.buttonDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // Setup Add to Cart button
        binding.btnAddToCart.setOnClickListener(v -> {
            // Convert price string to double (remove ₱ and any spaces)
            String priceStr = productPrice.replace("₱", "").replace(" ", "");
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                return;
            }

            addToCart(
                    productId,
                    productName,
                    productCategory,
                    price,
                    productImage,
                    quantity
            );
        });

        checkIfFavorite(productId);

        binding.ivFavorite.setOnClickListener(v -> {
            boolean isSelected = binding.ivFavorite.isSelected();
            binding.ivFavorite.setSelected(!isSelected);

            if (binding.ivFavorite.isSelected()) {
                binding.ivFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.six_green)));
                addProductToFavorites(productId, productName, productCategory, productPrice, productImage, productDescription);
            } else {
                binding.ivFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                removeProductFromFavorites(productId);
            }
        });
    }

    private void checkIfFavorite(String productId) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("favorites")
                .document(userId)
                .collection("items")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        binding.ivFavorite.setSelected(true);
                        binding.ivFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.six_green)));
                    } else {
                        binding.ivFavorite.setSelected(false);
                        binding.ivFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error checking favorite status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addProductToFavorites(String productId, String productName, String productCategory, String productPrice, String productImage, String productDescription) {
        String userId = mAuth.getCurrentUser().getUid();

        // Create a map or model object for favorite product
        Map<String, Object> favoriteItems = new HashMap<>();
        favoriteItems.put("productId", productId);
        favoriteItems.put("productName", productName);
        favoriteItems.put("productCategory", productCategory);
        favoriteItems.put("productPrice", productPrice);
        favoriteItems.put("productImage", productImage);
        favoriteItems.put("productDescription", productDescription);

        // Reference to the favorites collection in Firestore
        db.collection("favorites")
                .document(userId)
                .collection("items")
                .document(productId) // Using productId as document ID to avoid duplicates
                .set(favoriteItems)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProductOverview.this, "Added to favorites!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProductOverview.this, "Failed to add to favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Remove product from Firestore favorites collection
    private void removeProductFromFavorites(String productId) {
        String userId = mAuth.getCurrentUser().getUid();

        // Reference to the favorites collection in Firestore
        db.collection("favorites")
                .document(userId)
                .collection("items")
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(ProductOverview.this, "Removed from favorites!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProductOverview.this, "Failed to remove from favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addToCart(String productId, String name, String productCategory, double price, String imageUrl, int quantity) {
        String userId = mAuth.getCurrentUser().getUid();

        // Reference to the cart items collection in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("carts")
                .document(userId)
                .collection("items")
                .document(productId) // Using productId as document ID to prevent duplicates
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // If the item already exists, update the quantity
                        int existingQuantity = documentSnapshot.getLong("quantity").intValue();
                        int newQuantity = existingQuantity + quantity;

                        // Update the quantity
                        documentSnapshot.getReference().update("quantity", newQuantity)
                                .addOnSuccessListener(aVoid -> Toast.makeText(ProductOverview.this, "Added to cart successfully!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(ProductOverview.this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        // If the item does not exist, create a new cart item
                        CartItem cartItem = new CartItem(
                                productId,
                                name,
                                productCategory,
                                price,
                                imageUrl,
                                quantity,
                                userId
                        );

                        // Add new item to Firestore
                        documentSnapshot.getReference().set(cartItem)
                                .addOnSuccessListener(aVoid -> Toast.makeText(ProductOverview.this, "Added to cart successfully!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(ProductOverview.this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProductOverview.this, "Error checking cart item: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}