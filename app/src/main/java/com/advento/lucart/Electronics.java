package com.advento.lucart;

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

import com.advento.lucart.databinding.ActivityElectronicsBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Electronics extends AppCompatActivity {

    private ActivityElectronicsBinding binding;
    private ProductAdapter productAdapter;
    private List<Product> electronicsProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityElectronicsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Adjust layout for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Setup the toolbar
        setSupportActionBar(binding.tbElectronics);
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
        electronicsProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(this, electronicsProducts, null);
        binding.rvBrowse.setLayoutManager(new GridLayoutManager(this, 2)); // Adjust GridLayoutManager as needed
        binding.rvBrowse.setAdapter(productAdapter);

        // Fetch data from Firestore
        fetchElectronicsProducts();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Fetch electronics products from Firestore
    private void fetchElectronicsProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference productsCollection = db.collection("products"); // Adjust with your collection name

        // Query to get only "Electronics" category products
        productsCollection.whereEqualTo("productCategory", "Electronics")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        electronicsProducts.clear(); // Clear existing data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            electronicsProducts.add(product);
                        }
                        productAdapter.notifyDataSetChanged(); // Notify adapter that data is updated
                    } else {
                        Toast.makeText(Electronics.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
