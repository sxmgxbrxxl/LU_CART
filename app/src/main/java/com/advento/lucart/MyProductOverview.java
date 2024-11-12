package com.advento.lucart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class MyProductOverview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_product_overview);

        // Initialize views
        ImageView ivProductImage = findViewById(R.id.ivProductImage);
        TextView tvProductName = findViewById(R.id.tvProductName);
        TextView tvProductPrice = findViewById(R.id.tvProductPrice);
        TextView tvProductDescription = findViewById(R.id.tvProductDescription);
        TextView tvProductCategory = findViewById(R.id.tvProductCategory); // Corrected initialization

        // Get data from Intent
        Intent intent = getIntent();
        String productId = intent.getStringExtra("productId");
        String productName = intent.getStringExtra("productName");
        String productPrice = intent.getStringExtra("productPrice");
        String productDescription = intent.getStringExtra("productDescription");
        String productCategory = intent.getStringExtra("productCategory");
        String productImage = intent.getStringExtra("productImage");

        // Set data to views with null checks
        if (productName != null) {
            tvProductName.setText(productName);
        }

        if (productPrice != null) {
            tvProductPrice.setText(String.format("Price: %s", productPrice));
        }

        if (productDescription != null) {
            tvProductDescription.setText(productDescription);
        }

        if (productCategory != null) {
            tvProductCategory.setText(String.format("Category: %s", productCategory));
        }

        // Load image using Glide with null check
        if (productImage != null) {
            Glide.with(this)
                    .load(productImage)
                    .into(ivProductImage);
        }
    }
}