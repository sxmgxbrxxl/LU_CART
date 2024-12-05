package com.advento.lucart;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.advento.lucart.databinding.ActivityShopOverviewBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShopOverview extends AppCompatActivity {

    private ActivityShopOverviewBinding binding;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> allProducts;
    private FirebaseFirestore db;
    private String businessId;
    private String businessName;
    private String businessImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityShopOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rvProductsByShop.setLayoutManager(gridLayoutManager);

        productList = new ArrayList<>();
        allProducts = new ArrayList<>();

        productAdapter = new ProductAdapter(this, productList, product -> {
            Intent intent = new Intent(this, ProductOverview.class);
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productPrice", product.getProductPrice());
            intent.putExtra("productDescription", product.getProductDescription());
            intent.putExtra("productCategory", product.getProductCategory());
            intent.putExtra("productImage", product.getProductImage());
            startActivity(intent);
        });
        binding.rvProductsByShop.setAdapter(productAdapter);

        setSupportActionBar(binding.tbShopOverview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Drawable customBackButton = ContextCompat.getDrawable(this, R.drawable.ic_custom_back_activities);
        if (customBackButton != null) {
            customBackButton.setTint(ContextCompat.getColor(this, R.color.two_green));
            getSupportActionBar().setHomeAsUpIndicator(customBackButton);
        }

        businessId = getIntent().getStringExtra("businessId");
        businessName = getIntent().getStringExtra("businessName");
        businessImage = getIntent().getStringExtra("businessImage");

        Glide.with(this)
                .load(businessImage)
                .circleCrop()
                .into(binding.ivDisplayPhoto);

        binding.tvShopName.setText(businessName);
        loadApprovedProducts();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadApprovedProducts() {
        CollectionReference productsRef = db.collection("products");

        productsRef.whereEqualTo("status", "approved")
                .whereEqualTo("businessId", businessId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        productList.clear();
                        allProducts.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            productList.add(product);
                            allProducts.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }
}