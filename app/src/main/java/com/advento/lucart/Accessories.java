package com.advento.lucart;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.advento.lucart.databinding.ActivityAccessoriesBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Accessories extends AppCompatActivity {

    private ActivityAccessoriesBinding binding;
    private ProductAdapter productAdapter;
    private List<Product> accessoriesProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityAccessoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Adjust layout for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Setup the toolbar
        setSupportActionBar(binding.tbAccessories);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set custom back button
        Drawable customBackButton = ContextCompat.getDrawable(this, R.drawable.ic_custom_back_activities);
        if (customBackButton != null) {
            customBackButton.setTint(ContextCompat.getColor(this, R.color.eleven_green));
            getSupportActionBar().setHomeAsUpIndicator(customBackButton);
        }

        // Initialize the RecyclerView
        accessoriesProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(this, accessoriesProducts, product -> {
            Intent intent = new Intent(this, ProductOverview.class);
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productPrice", product.getProductPrice());
            intent.putExtra("productDescription", product.getProductDescription());
            intent.putExtra("productCategory", product.getProductCategory());
            intent.putExtra("productImage", product.getProductImage());
            startActivity(intent);
        });
        binding.rvBrowse.setLayoutManager(new GridLayoutManager(this, 2)); // Adjust GridLayoutManager as needed
        binding.rvBrowse.setAdapter(productAdapter);

        // Fetch data from Firestore
        fetchAccessoriesProducts();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Fetch accessories products from Firestore
    private void fetchAccessoriesProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference productsCollection = db.collection("products"); // Adjust with your collection name

        // Query to get only "Accessories" category products
        productsCollection.whereEqualTo("productCategory", "Accessories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        accessoriesProducts.clear(); // Clear existing data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            accessoriesProducts.add(product);
                        }
                        productAdapter.notifyDataSetChanged(); // Notify adapter that data is updated
                    } else {
                        Toast.makeText(Accessories.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
