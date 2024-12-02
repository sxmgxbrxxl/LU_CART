package com.advento.lucart;

import android.annotation.SuppressLint;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductOverview extends AppCompatActivity {

    private ActivityProductOverviewBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private int quantity = 1;
    private int stockQuantity = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityProductOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

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

        String productId = getIntent().getStringExtra("productId");
        String productImage = getIntent().getStringExtra("productImage");
        String productName = getIntent().getStringExtra("productName");
        String productCategory = getIntent().getStringExtra("productCategory");
        String productPrice = getIntent().getStringExtra("productPrice");
        String productDescription = getIntent().getStringExtra("productDescription");

        Glide.with(this)
                .load(productImage)
                .into(binding.ivProductImage);

        binding.tvProductName.setText(productName);
        binding.tvCategory.setText(productCategory);
        binding.tvProductPrice.setText("₱ " + productPrice);
        binding.totalP.setText("Total Price: ₱ " + productPrice);

        List<String> descriptions = Arrays.asList(productDescription.split("\n\n"));
        DescriptionPagerAdapter adapter = new DescriptionPagerAdapter(this, descriptions);
        binding.vpProductDescription.setAdapter(adapter);

        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String stock = documentSnapshot.getString("stock"); // Assuming 'stock' field exists
                        stockQuantity = Integer.parseInt(stock); // Store the available stock in the variable
                        binding.tvAvailableStock.setText("Stock: " + stock); // Set the stock number in the TextView
                    } else {
                        binding.tvAvailableStock.setText("Stock: 0"); // Default if not found
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProductOverview.this, "Error retrieving stock: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.tvAvailableStock.setText("Stock: 0");
                });

        // Setup quantity buttons with stock limit
        binding.buttonIncrease.setOnClickListener(v -> {
            if (quantity < stockQuantity) { // Limit quantity to available stock
                quantity++;
                binding.tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice(Double.parseDouble(productPrice));
            } else {
                Toast.makeText(this, "Cannot exceed available stock.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice(Double.parseDouble(productPrice));
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

    private void updateTotalPrice(double price) {
        double totalPrice = price * quantity;
        binding.totalP.setText("Total Price: ₱ " + String.format("%.2f", totalPrice));
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

        // First, retrieve the sellerId of the product
        db.collection("products") // Assuming the products collection is named "products"
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String sellerId = documentSnapshot.getString("sellerId"); // Adjust the field name if it's different in Firestore

                        if (sellerId == null) {
                            Toast.makeText(this, "Seller information not found.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Proceed to add the item to the cart
                        db.collection("carts")
                                .document(userId)
                                .collection("items")
                                .document(productId)
                                .get()
                                .addOnSuccessListener(cartSnapshot -> {
                                    if (cartSnapshot.exists()) {
                                        int newQuantity = cartSnapshot.getLong("quantity") != null ? cartSnapshot.getLong("quantity").intValue() + quantity : quantity;
                                        // Update existing cart item
                                        db.collection("carts")
                                                .document(userId)
                                                .collection("items")
                                                .document(productId)
                                                .update("quantity", newQuantity)
                                                .addOnSuccessListener(aVoid -> Toast.makeText(ProductOverview.this, "Item added to cart", Toast.LENGTH_SHORT).show())
                                                .addOnFailureListener(e -> Toast.makeText(ProductOverview.this, "Failed to update cart", Toast.LENGTH_SHORT).show());
                                    } else {
                                        // Add new item to the cart
                                        Map<String, Object> cartItem = new HashMap<>();
                                        cartItem.put("productId", productId);
                                        cartItem.put("name", name);
                                        cartItem.put("productCategory", productCategory);
                                        cartItem.put("price", price);
                                        cartItem.put("imageUrl", imageUrl);
                                        cartItem.put("quantity", quantity);
                                        cartItem.put("sellerId", sellerId);

                                        db.collection("carts")
                                                .document(userId)
                                                .collection("items")
                                                .document(productId)
                                                .set(cartItem)
                                                .addOnSuccessListener(aVoid -> Toast.makeText(ProductOverview.this, "Item added to cart", Toast.LENGTH_SHORT).show())
                                                .addOnFailureListener(e -> Toast.makeText(ProductOverview.this, "Failed to add to cart", Toast.LENGTH_SHORT).show());
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(ProductOverview.this, "Error checking cart: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Product not found in the database", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error retrieving sellerId: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

