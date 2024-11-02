package com.advento.lucart;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityProductOverviewBinding;
import com.bumptech.glide.Glide;

public class ProductOverview extends AppCompatActivity {

    private ActivityProductOverviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityProductOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Retrieve data from intent
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

        // Favorites Animation
        binding.fabFavorite.setOnClickListener(v -> {
            boolean isSelected = binding.fabFavorite.isSelected();
            binding.fabFavorite.setSelected(!isSelected); // Toggle selected state

            if (binding.fabFavorite.isSelected()) {
                binding.fabFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.main_green)));
            } else {
                binding.fabFavorite.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}