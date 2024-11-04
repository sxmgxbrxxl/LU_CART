package com.advento.lucart;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.advento.lucart.databinding.ActivityProductOverviewBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductOverview extends AppCompatActivity {

    private ActivityProductOverviewBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_overview);

        binding = ActivityProductOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Retrieve data from intent
        String productId = getIntent().getStringExtra("productId"); // Make sure to pass this from previous activity
        String productImage = getIntent().getStringExtra("productImage");
        String productName = getIntent().getStringExtra("productName");
        String productPrice = getIntent().getStringExtra("productPrice");
        String productDescription = getIntent().getStringExtra("productDescription");

        //Import Image
        Glide.with(this)
                .load(productImage)
                .into(binding.ivProductImage);

        // Set data to views
        binding.tvProductName.setText(productName);
        binding.tvProductPrice.setText("₱ " + productPrice);
        binding.tvProductDescription.setText(productDescription);

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
        binding.AddToCart.setOnClickListener(v -> {
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
                    price,
                    productImage,
                    quantity
            );
        });

        // Favorites Animation
        binding.fabFavorite.setOnClickListener(v -> {
            boolean isSelected = binding.fabFavorite.isSelected();
            binding.fabFavorite.setSelected(!isSelected);

            if (binding.fabFavorite.isSelected()) {
                binding.fabFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.main_green)));
            } else {
                binding.fabFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            }
        });
    }

    private void addToCart(String productId, String name, double price, String imageUrl, int quantity) {
        String userId = mAuth.getCurrentUser().getUid();

        // Create cart item object matching your CartItem class
        CartItem cartItem = new CartItem(
                productId,
                name,
                price,
                imageUrl,
                quantity,
                userId
        );

        // Add to Firestore using the same structure as in MyCartFragment
        db.collection("carts")
                .document(userId)
                .collection("items")
                .document(productId) // Using productId as document ID to prevent duplicates
                .set(cartItem)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProductOverview.this, "Added to cart successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProductOverview.this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}